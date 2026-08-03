package com.example.ui.screens.salon_settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.SalonSettingsEntity
import com.example.ui.SalonViewModel
import com.example.ui.components.MaskedOutlinedTextField
import com.example.ui.theme.getPaletteColors

@Composable
fun SalonSettingsScreen(
    viewModel: SalonViewModel,
    settings: SalonSettingsEntity,
    modifier: Modifier = Modifier
) {
    var razaoSocial by remember(settings) { mutableStateOf(settings.razaoSocial) }
    var nomeFantasia by remember(settings) { mutableStateOf(settings.nomeFantasia) }
    var cnpj by remember(settings) { mutableStateOf(settings.cnpj) }
    var inscricaoEstadual by remember(settings) { mutableStateOf(settings.inscricaoEstadual) }
    var defaultStartScreen by remember(settings) { mutableStateOf(settings.defaultStartScreen) }
    var selectedPalette by remember(settings) { mutableStateOf(settings.themePalette) }
    var darkModeOption by remember(settings) { mutableStateOf(settings.darkModeOption) }
    var useDynamicColor by remember(settings) { mutableStateOf(settings.useDynamicColor) }
    var masterPassword by remember(settings) { mutableStateOf(settings.masterPassword) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Banner Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_salon_1785407233613),
                    contentDescription = "Salon Hero",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column {
                        Text(
                            text = nomeFantasia.ifBlank { "Seu Salão" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Personalize as preferências, tema visual e dados fiscais do salão.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Section 1: Business Details
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dados da Empresa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = nomeFantasia,
                    onValueChange = { nomeFantasia = it },
                    label = { Text("Nome Fantasia (exibido na TopBar) *") },
                    modifier = Modifier.fillMaxWidth().testTag("nome_fantasia_input")
                )

                OutlinedTextField(
                    value = razaoSocial,
                    onValueChange = { razaoSocial = it },
                    label = { Text("Razão Social *") },
                    modifier = Modifier.fillMaxWidth()
                )

                MaskedOutlinedTextField(
                    value = cnpj,
                    onValueChange = { cnpj = it },
                    label = "CNPJ da Empresa",
                    maskType = "CNPJ",
                    isRequired = true
                )

                MaskedOutlinedTextField(
                    value = inscricaoEstadual,
                    onValueChange = { inscricaoEstadual = it },
                    label = "Inscrição Estadual (IE)",
                    maskType = "IE"
                )

                OutlinedTextField(
                    value = masterPassword,
                    onValueChange = { masterPassword = it },
                    label = { Text("Senha do Usuário Master (Desbloqueio)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("master_password_input")
                )
            }
        }

        // Section 2: General Personalization & Start Screen
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Personalização de Tela Inicial", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Selecione qual módulo será aberto ao iniciar o aplicativo:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                val screens = listOf(
                    "AGENDAMENTOS" to "Agendamentos (Agenda)",
                    "SERVICOS" to "Cadastro de Serviços",
                    "PROFISSIONAIS" to "Profissionais",
                    "CLIENTES" to "Gestão de Clientes",
                    "NFSE" to "Plataforma NFS-e",
                    "WHATSAPP" to "Mensagens WhatsApp"
                )

                screens.forEach { (key, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { defaultStartScreen = key }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = defaultStartScreen == key, onClick = { defaultStartScreen = key })
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Section 3: Visual Themes & Palettes
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gestão do Tema e Paleta de Cores", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Text("Selecione a paleta de cores do seu salão:", style = MaterialTheme.typography.bodySmall)

                val palettes = listOf(
                    "ROSE_GOLD" to "Rose Gold Glam",
                    "ROYAL_VIOLET" to "Royal Violet Spa",
                    "EMERALD_GLAM" to "Emerald Elegance",
                    "GOLDEN_LUXURY" to "Golden Luxury",
                    "SUNSET_COPPER" to "Sunset Copper",
                    "OCEAN_BREEZE" to "Azul Oceano",
                    "MIDNIGHT_LUX" to "Noite Estrelada",
                    "SAGE_MINDFUL" to "Verde Sálvia Zen"
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    palettes.forEach { (key, name) ->
                        val (p, s, a) = getPaletteColors(key)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedPalette = key
                                    viewModel.saveSettings(settings.copy(themePalette = key, darkModeOption = darkModeOption, useDynamicColor = useDynamicColor))
                                }
                                .border(
                                    width = if (selectedPalette == key) 2.dp else 0.dp,
                                    color = if (selectedPalette == key) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            tonalElevation = if (selectedPalette == key) 4.dp else 1.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(p)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(s)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(a)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                }

                                if (selectedPalette == key) {
                                    Icon(Icons.Default.Check, contentDescription = "Selecionado", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Modo de Exibição:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = darkModeOption == "SYSTEM",
                        onClick = {
                            darkModeOption = "SYSTEM"
                            viewModel.saveSettings(settings.copy(themePalette = selectedPalette, darkModeOption = "SYSTEM", useDynamicColor = useDynamicColor))
                        },
                        label = { Text("Sistema") }
                    )
                    FilterChip(
                        selected = darkModeOption == "LIGHT",
                        onClick = {
                            darkModeOption = "LIGHT"
                            viewModel.saveSettings(settings.copy(themePalette = selectedPalette, darkModeOption = "LIGHT", useDynamicColor = useDynamicColor))
                        },
                        label = { Text("Modo Claro") }
                    )
                    FilterChip(
                        selected = darkModeOption == "DARK",
                        onClick = {
                            darkModeOption = "DARK"
                            viewModel.saveSettings(settings.copy(themePalette = selectedPalette, darkModeOption = "DARK", useDynamicColor = useDynamicColor))
                        },
                        label = { Text("Modo Escuro") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Suporte a Dynamic Color (Material You)", style = MaterialTheme.typography.bodyMedium)
                        Text("Adota as cores do papel de parede no Android 12+", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = useDynamicColor,
                        onCheckedChange = {
                            useDynamicColor = it
                            viewModel.saveSettings(settings.copy(themePalette = selectedPalette, darkModeOption = darkModeOption, useDynamicColor = it))
                        },
                        modifier = Modifier.testTag("dynamic_color_switch")
                    )
                }
            }
        }

        // Save Button
        Button(
            onClick = {
                val updated = settings.copy(
                    razaoSocial = razaoSocial,
                    nomeFantasia = nomeFantasia,
                    cnpj = cnpj,
                    inscricaoEstadual = inscricaoEstadual,
                    defaultStartScreen = defaultStartScreen,
                    themePalette = selectedPalette,
                    darkModeOption = darkModeOption,
                    useDynamicColor = useDynamicColor,
                    masterPassword = masterPassword
                )
                viewModel.saveSettings(updated)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("save_settings_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Salvar Todas as Configurações", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
