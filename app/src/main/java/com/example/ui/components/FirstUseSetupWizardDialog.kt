package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SalonSettingsEntity

@Composable
fun FirstUseSetupWizardDialog(
    settings: SalonSettingsEntity,
    onCompleteSetup: (SalonSettingsEntity) -> Unit
) {
    var nomeFantasia by remember { mutableStateOf(settings.nomeFantasia.ifBlank { "Gold Salão" }) }
    var razaoSocial by remember { mutableStateOf(settings.razaoSocial.ifBlank { "Gold Salão Ltda" }) }
    var cnpj by remember { mutableStateOf(settings.cnpj.ifBlank { "12.345.678/0001-90" }) }
    var themePalette by remember { mutableStateOf(settings.themePalette.ifBlank { "ROSE_GOLD" }) }

    Dialog(
        onDismissRequest = {}, // Cannot be dismissed without finishing setup
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.925f)
                .fillMaxHeight(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Column {
                        Text(
                            text = "Bem-vindo ao Gold Salão!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Assistente de Configuração Inicial",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                Text(
                    text = "Configure os dados principais do seu salão para personalizar o recibos, notas fiscais (NFS-e), mensagens do WhatsApp e a identidade visual do sistema.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = nomeFantasia,
                    onValueChange = { nomeFantasia = it },
                    label = { Text("Nome Fantasia do Salão *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = razaoSocial,
                    onValueChange = { razaoSocial = it },
                    label = { Text("Razão Social *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = cnpj,
                    onValueChange = { cnpj = it },
                    label = { Text("CNPJ *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Escolha a Paleta de Cores do Tema:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                val themes = listOf(
                    "ROSE_GOLD" to "Rose Gold (Elegante & Delicado)",
                    "ROYAL_VIOLET" to "Royal Violet (Moderno & Luxuoso)",
                    "EMERALD_GLAM" to "Emerald Elegance (Sofisticado & Vivo)",
                    "GOLDEN_LUXURY" to "Golden Luxury (Clássico & Dourado)",
                    "SUNSET_COPPER" to "Sunset Copper (Acolhedor & Quente)"
                )

                themes.forEach { (key, label) ->
                    Surface(
                        selected = themePalette == key,
                        onClick = { themePalette = key },
                        shape = RoundedCornerShape(12.dp),
                        color = if (themePalette == key) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = if (themePalette == key) FontWeight.Bold else FontWeight.Normal)
                            RadioButton(
                                selected = themePalette == key,
                                onClick = { themePalette = key }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val updated = settings.copy(
                            nomeFantasia = nomeFantasia.ifBlank { "Gold Salão" },
                            razaoSocial = razaoSocial.ifBlank { "Gold Salão Ltda" },
                            cnpj = cnpj.ifBlank { "12.345.678/0001-90" },
                            themePalette = themePalette,
                            isFirstUseCompleted = true
                        )
                        onCompleteSetup(updated)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Concluir Configuração e Iniciar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
