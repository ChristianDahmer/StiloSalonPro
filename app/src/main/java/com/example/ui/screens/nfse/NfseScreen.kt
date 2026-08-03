package com.example.ui.screens.nfse

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NfseConfigEntity
import com.example.data.model.NfseEntity
import com.example.ui.SalonViewModel
import com.example.ui.components.StatusBadgeChip
import com.example.utils.MaskUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfseScreen(
    viewModel: SalonViewModel,
    nfseList: List<NfseEntity>,
    nfseConfig: NfseConfigEntity,
    isGridView: Boolean,
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 = Notas Emitidas, 1 = Configuração Fiscal
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) }
    var previewNfse by remember { mutableStateOf<NfseEntity?>(null) }
    var nfseToCancel by remember { mutableStateOf<NfseEntity?>(null) }

    val filteredNfseList = remember(nfseList, searchQuery, selectedStatusFilter) {
        nfseList.filter { nfse ->
            val matchesSearch = searchQuery.isBlank() ||
                    nfse.clientName.contains(searchQuery, ignoreCase = true) ||
                    nfse.number.contains(searchQuery) ||
                    nfse.serviceName.contains(searchQuery, ignoreCase = true)

            val matchesStatus = selectedStatusFilter == null || nfse.status == selectedStatusFilter

            matchesSearch && matchesStatus
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Notas Fiscais (${nfseList.size})") },
                icon = { Icon(Icons.Outlined.ReceiptLong, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Configuração Fiscal") },
                icon = { Icon(Icons.Outlined.Settings, contentDescription = null) }
            )
        }

        if (selectedTab == 0) {
            // Status Filters Row
            Surface(
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Status:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    listOf(null to "Todas", "EMITIDA" to "Emitidas", "CANCELADA" to "Canceladas").forEach { (st, label) ->
                        FilterChip(
                            selected = selectedStatusFilter == st,
                            onClick = { selectedStatusFilter = st },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }
            }

            if (filteredNfseList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nenhuma NFS-e emitida",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "As notas emitidas ao concluir agendamentos aparecerão aqui.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredNfseList, key = { it.id }) { nfse ->
                            NfseGridCard(
                                nfse = nfse,
                                onPreview = { previewNfse = nfse },
                                onCancel = { nfseToCancel = nfse }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredNfseList, key = { it.id }) { nfse ->
                            NfseListItem(
                                nfse = nfse,
                                onPreview = { previewNfse = nfse },
                                onDownloadXmlPdf = {
                                    Toast.makeText(context, "Download simulação XML e PDF da nota #${nfse.number} concluído!", Toast.LENGTH_LONG).show()
                                },
                                onResend = {
                                    Toast.makeText(context, "NFS-e #${nfse.number} reenviada para o e-mail/WhatsApp do cliente!", Toast.LENGTH_SHORT).show()
                                },
                                onCancel = { nfseToCancel = nfse }
                            )
                        }
                    }
                }
            }
        } else {
            NfseConfigForm(
                config = nfseConfig,
                onSave = { updatedConfig ->
                    viewModel.saveNfseConfig(updatedConfig)
                }
            )
        }
    }

    // Preview NFS-e Dialog
    if (previewNfse != null) {
        NfsePreviewDialog(
            nfse = previewNfse!!,
            onDismiss = { previewNfse = null }
        )
    }

    // Cancel NFS-e Justification Dialog
    if (nfseToCancel != null) {
        NfseCancelDialog(
            nfse = nfseToCancel!!,
            onDismiss = { nfseToCancel = null },
            onConfirmCancel = { reason ->
                viewModel.cancelNfse(nfseToCancel!!, reason)
                nfseToCancel = null
            }
        )
    }
}

@Composable
fun NfseListItem(
    nfse: NfseEntity,
    onPreview: () -> Unit,
    onDownloadXmlPdf: () -> Unit,
    onResend: () -> Unit,
    onCancel: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(nfse.id) {
                detectTapGestures(
                    onDoubleTap = { onPreview() }
                )
            },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NFS-e #${nfse.number}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                StatusBadgeChip(status = nfse.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Cliente: ${nfse.clientName} (${nfse.clientCpf})",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Serviço: ${nfse.serviceName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = MaskUtils.formatCurrency(nfse.price),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Emissão: ${nfse.issueDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (nfse.status == "CANCELADA" && !nfse.cancellationReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Motivo Cancelamento: ${nfse.cancellationReason}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onPreview) {
                    Icon(Icons.Outlined.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Preview")
                }

                TextButton(onClick = onDownloadXmlPdf) {
                    Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("XML/PDF")
                }

                TextButton(onClick = onResend) {
                    Icon(Icons.Outlined.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reenviar")
                }

                if (nfse.status == "EMITIDA") {
                    TextButton(onClick = onCancel, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Icon(Icons.Outlined.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}

@Composable
fun NfsePreviewDialog(
    nfse: NfseEntity,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("NFS-e #${nfse.number} - Visualização", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("PREFEITURA MUNICIPAL - NOTA FISCAL DE SERVIÇOS ELETRÔNICA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Divider()
                Text("Número da Nota: ${nfse.number}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text("Código de Verificação: ${nfse.verificationCode}", style = MaterialTheme.typography.bodySmall)
                Text("Data/Hora Emissão: ${nfse.issueDate}", style = MaterialTheme.typography.bodySmall)
                Divider()
                Text("TOMADOR DO SERVIÇO:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text("Nome: ${nfse.clientName}", style = MaterialTheme.typography.bodySmall)
                Text("CPF/CNPJ: ${nfse.clientCpf}", style = MaterialTheme.typography.bodySmall)
                Divider()
                Text("DISCRIMINAÇÃO DOS SERVIÇOS:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text("Serviço prestado: ${nfse.serviceName}", style = MaterialTheme.typography.bodySmall)
                Text("VALOR TOTAL DA NOTA: ${MaskUtils.formatCurrency(nfse.price)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

@Composable
fun NfseCancelDialog(
    nfse: NfseEntity,
    onDismiss: () -> Unit,
    onConfirmCancel: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cancelar NFS-e #${nfse.number}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Informe a justificativa de cancelamento obrigatória perante a Prefeitura:")
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Justificativa de Cancelamento *") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmCancel(reason) },
                enabled = reason.length >= 5,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("confirm_cancel_nfse_button")
            ) {
                Text("Confirmar Cancelamento")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Voltar")
            }
        }
    )
}

@Composable
fun NfseConfigForm(
    config: NfseConfigEntity,
    onSave: (NfseConfigEntity) -> Unit
) {
    var certStatus by remember(config) { mutableStateOf(config.certificadoStatus) }
    var apiToken by remember(config) { mutableStateOf(config.apiToken) }
    var inscricaoMunicipal by remember(config) { mutableStateOf(config.inscricaoMunicipal) }
    var regimeEspecial by remember(config) { mutableStateOf(config.regimeEspecial) }
    var aliquotaIss by remember(config) { mutableStateOf(config.aliquotaIss.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ElevatedCard(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Credenciais & Certificado Digital A1", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = certStatus,
                    onValueChange = { certStatus = it },
                    label = { Text("Status do Certificado Digital A1") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = apiToken,
                    onValueChange = { apiToken = it },
                    label = { Text("Token de API da Plataforma Fiscal") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = inscricaoMunicipal,
                    onValueChange = { inscricaoMunicipal = it },
                    label = { Text("Inscrição Municipal") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = regimeEspecial,
                    onValueChange = { regimeEspecial = it },
                    label = { Text("Regime Especial de Tributação") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = aliquotaIss,
                    onValueChange = { aliquotaIss = it },
                    label = { Text("Alíquota do ISS (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Button(
            onClick = {
                val updated = config.copy(
                    certificadoStatus = certStatus,
                    apiToken = apiToken,
                    inscricaoMunicipal = inscricaoMunicipal,
                    regimeEspecial = regimeEspecial,
                    aliquotaIss = aliquotaIss.toDoubleOrNull() ?: 2.5
                )
                onSave(updated)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("save_nfse_config_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Salvar Configurações Fiscais", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NfseGridCard(
    nfse: NfseEntity,
    onPreview: () -> Unit,
    onCancel: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(nfse.id) {
                detectTapGestures(
                    onDoubleTap = { onPreview() }
                )
            },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NFS-e #${nfse.number}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                StatusBadgeChip(status = nfse.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = nfse.clientName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = nfse.serviceName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = MaskUtils.formatCurrency(nfse.price),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onPreview, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Outlined.Visibility, contentDescription = "Visualizar Nota", modifier = Modifier.size(16.dp))
                }
                if (nfse.status == "EMITIDA") {
                    IconButton(onClick = onCancel, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Outlined.Cancel, contentDescription = "Cancelar Nota", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
