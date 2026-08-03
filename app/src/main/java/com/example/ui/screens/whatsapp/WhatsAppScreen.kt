package com.example.ui.screens.whatsapp

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClientEntity
import com.example.data.model.WhatsAppLogEntity
import com.example.data.model.WhatsAppTemplateEntity
import com.example.ui.SalonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppScreen(
    viewModel: SalonViewModel,
    templates: List<WhatsAppTemplateEntity>,
    logs: List<WhatsAppLogEntity>,
    clients: List<ClientEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 = Modelos, 1 = Disparo Promocional, 2 = Relatórios/Logs
    var templateToEdit by remember { mutableStateOf<WhatsAppTemplateEntity?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_whatsapp),
                    contentDescription = "WhatsApp Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Central WhatsApp",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Envio de confirmações e mensagens diretas aos clientes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Modelos (${templates.size})") },
                icon = { Icon(Icons.Outlined.TextSnippet, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Campanhas") },
                icon = { Icon(Icons.Outlined.Campaign, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Logs / Histórico") },
                icon = { Icon(Icons.Outlined.History, contentDescription = null) }
            )
        }

        when (selectedTab) {
            0 -> WhatsAppTemplatesTab(
                templates = templates,
                onEditTemplate = { templateToEdit = it }
            )
            1 -> WhatsAppCampaignTab(
                clients = clients,
                onSendCampaign = { title, msgText, targetList ->
                    viewModel.sendPromotionalCampaign(title, msgText, targetList, context)
                }
            )
            2 -> WhatsAppLogsTab(logs = logs)
        }
    }

    if (templateToEdit != null) {
        WhatsAppTemplateEditDialog(
            template = templateToEdit!!,
            onDismiss = { templateToEdit = null },
            onSave = { updated ->
                viewModel.updateWhatsAppTemplate(updated)
                templateToEdit = null
            }
        )
    }
}

@Composable
fun WhatsAppTemplatesTab(
    templates: List<WhatsAppTemplateEntity>,
    onEditTemplate: (WhatsAppTemplateEntity) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Variáveis Dinâmicas Disponíveis:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    Text(
                        "{nome_cliente}, {data}, {horario}, {servico}, {nome_profissional}, {nome_salao}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        items(templates, key = { it.id }) { template ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(template.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { onEditTemplate(template) }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Editar Modelo")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = template.templateText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WhatsAppTemplateEditDialog(
    template: WhatsAppTemplateEntity,
    onDismiss: () -> Unit,
    onSave: (WhatsAppTemplateEntity) -> Unit
) {
    var templateText by remember { mutableStateOf(template.templateText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Modelo: ${template.title}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Edite o texto do modelo mantendo as variáveis entre chaves:")
                OutlinedTextField(
                    value = templateText,
                    onValueChange = { templateText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    label = { Text("Texto da Mensagem") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(template.copy(templateText = templateText))
                },
                modifier = Modifier.testTag("save_template_button")
            ) {
                Text("Salvar Modelo")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun WhatsAppCampaignTab(
    clients: List<ClientEntity>,
    onSendCampaign: (String, String, List<ClientEntity>) -> Unit
) {
    var campaignTitle by remember { mutableStateOf("Promoção Mês do Aniversário") }
    var segmentFilter by remember { mutableStateOf("ANIVERSARIANTES") } // ANIVERSARIANTES, TODOS
    var campaignMessage by remember { mutableStateOf("Olá {nome_cliente}! No seu mês especial, o {nome_salao} preparou um presente exclusivo de 20% OFF para você! Venha comemorar conosco.") }

    val targetClients = remember(clients, segmentFilter) {
        if (segmentFilter == "ANIVERSARIANTES") {
            clients.filter {
                val month = it.birthDate.split("-").getOrNull(1)?.toIntOrNull()
                month == 7 // July sample match
            }
        } else clients
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ElevatedCard(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Disparo de Campanha Promocional", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = campaignTitle,
                    onValueChange = { campaignTitle = it },
                    label = { Text("Nome da Campanha") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Segmentação de Clientes:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = segmentFilter == "ANIVERSARIANTES",
                        onClick = { segmentFilter = "ANIVERSARIANTES" },
                        label = { Text("Aniversariantes do Mês (${targetClients.size})") }
                    )
                    FilterChip(
                        selected = segmentFilter == "TODOS",
                        onClick = { segmentFilter = "TODOS" },
                        label = { Text("Todos os Clientes (${clients.size})") }
                    )
                }

                OutlinedTextField(
                    value = campaignMessage,
                    onValueChange = { campaignMessage = it },
                    label = { Text("Texto da Mensagem Promocional") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Público Alvo Selecionado: ${targetClients.size} cliente(s)",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        Button(
            onClick = {
                onSendCampaign(campaignTitle, campaignMessage, targetClients)
            },
            enabled = targetClients.isNotEmpty() && campaignMessage.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("send_campaign_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Send, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Disparar Campanha para ${targetClients.size} Cliente(s)", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun WhatsAppLogsTab(logs: List<WhatsAppLogEntity>) {
    if (logs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Nenhum histórico de envio registrado ainda.", style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(logs, key = { it.id }) { log ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Para: ${log.recipientName} (${log.recipientPhone})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Badge(containerColor = Color(0xFFE8F5E9)) {
                                Text(log.status, color = Color(0xFF2E7D32), modifier = Modifier.padding(2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = log.messageText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (!log.campaignName.isNullOrBlank()) {
                            Text(
                                text = "Campanha: ${log.campaignName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}
