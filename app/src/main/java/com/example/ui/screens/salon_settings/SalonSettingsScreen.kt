package com.example.ui.screens.salon_settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.unit.sp
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

    val isConnected by viewModel.gdriveConnected.collectAsState()
    val backups by viewModel.gdriveBackups.collectAsState()
    val isLoading by viewModel.gdriveLoading.collectAsState()
    val statusMessage by viewModel.gdriveStatusMessage.collectAsState()
    val gdriveEmail by viewModel.gdriveEmail.collectAsState()

    val scheduleInterval by viewModel.gdriveScheduleInterval.collectAsState()
    val scheduleHour by viewModel.gdriveScheduleHour.collectAsState()
    val scheduleMinute by viewModel.gdriveScheduleMinute.collectAsState()

    var tempInterval by remember(scheduleInterval) { mutableStateOf(scheduleInterval) }
    var tempHour by remember(scheduleHour) { mutableStateOf(scheduleHour) }
    var tempMinute by remember(scheduleMinute) { mutableStateOf(scheduleMinute) }

    var showAuthDialog by remember { mutableStateOf(false) }
    var authUrl by remember { mutableStateOf("") }

    var selectedBackupForRestore by remember { mutableStateOf<com.example.data.GoogleDriveBackupManager.DriveFile?>(null) }
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var showRestoreSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(isConnected) {
        if (isConnected) {
            viewModel.loadGDriveBackups()
        }
    }

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

                Text("Modo de Exibição:", style = MaterialTheme.typography.labelMedium, maxLines = 1, softWrap = false)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = darkModeOption == "SYSTEM",
                        onClick = {
                            darkModeOption = "SYSTEM"
                            viewModel.saveSettings(settings.copy(themePalette = selectedPalette, darkModeOption = "SYSTEM", useDynamicColor = useDynamicColor))
                        },
                        label = { Text("Sistema", maxLines = 1, softWrap = false) }
                    )
                    FilterChip(
                        selected = darkModeOption == "LIGHT",
                        onClick = {
                            darkModeOption = "LIGHT"
                            viewModel.saveSettings(settings.copy(themePalette = selectedPalette, darkModeOption = "LIGHT", useDynamicColor = useDynamicColor))
                        },
                        label = { Text("Modo Claro", maxLines = 1, softWrap = false) }
                    )
                    FilterChip(
                        selected = darkModeOption == "DARK",
                        onClick = {
                            darkModeOption = "DARK"
                            viewModel.saveSettings(settings.copy(themePalette = selectedPalette, darkModeOption = "DARK", useDynamicColor = useDynamicColor))
                        },
                        label = { Text("Modo Escuro", maxLines = 1, softWrap = false) }
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

        // Section 4: Backup & Restore (Google Drive)
        ElevatedCard(
            modifier = Modifier.fillMaxWidth().testTag("gdrive_backup_card"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Backup no Google Drive",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isConnected) {
                        TextButton(
                            onClick = { viewModel.disconnectGDrive() },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.testTag("gdrive_disconnect_button")
                        ) {
                            Text("Desconectar")
                        }
                    }
                }

                Text(
                    text = "Faça backup de todos os agendamentos, clientes, serviços, faturamento e configurações de forma segura diretamente na sua conta do Google Drive.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isConnected && gdriveEmail != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Conta cadastrada: $gdriveEmail",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                if (statusMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = statusMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                if (!isConnected) {
                    Button(
                        onClick = {
                            authUrl = com.example.data.GoogleDriveBackupManager.getOAuthLoginUrl()
                            showAuthDialog = true
                        },
                        modifier = Modifier.fillMaxWidth().testTag("gdrive_connect_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CloudQueue, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Conectar Conta do Google")
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.uploadBackupToGDrive() },
                            modifier = Modifier.weight(1f).testTag("gdrive_backup_now_button"),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Fazer Backup Agora")
                            }
                        }

                        OutlinedButton(
                            onClick = { viewModel.loadGDriveBackups() },
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading,
                            modifier = Modifier.testTag("gdrive_refresh_backups_button")
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = "Atualizar")
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "Agendamento Automático de Backup",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "disabled" to "Desativado",
                            "daily" to "Diário",
                            "weekly" to "Semanal"
                        ).forEach { (value, label) ->
                            val selected = tempInterval == value
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clickable { tempInterval = value },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    if (tempInterval != "disabled") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = tempHour.toString(),
                                onValueChange = { newValue ->
                                    val hour = newValue.toIntOrNull() ?: 0
                                    if (hour in 0..23) tempHour = hour
                                },
                                label = { Text("Hora (0-23)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = tempMinute.toString(),
                                onValueChange = { newValue ->
                                    val min = newValue.toIntOrNull() ?: 0
                                    if (min in 0..59) tempMinute = min
                                },
                                label = { Text("Minuto (0-59)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.saveBackupSchedule(tempInterval, tempHour, tempMinute) },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Salvar Agendamento", fontSize = 14.sp)
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "Histórico de Backups Disponíveis",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    if (backups.isEmpty()) {
                        Text(
                            text = "Nenhum backup encontrado no Google Drive. Clique em 'Fazer Backup Agora' para criar o primeiro.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            backups.forEach { backup ->
                                val readableName = backup.name
                                    .replace("salon_backup_", "Backup: ")
                                    .replace(".sqlite", "")
                                    .replace("_", " às ")
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = readableName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "ID: ${backup.id.take(8)}...",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                selectedBackupForRestore = backup
                                                showRestoreConfirm = true
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondary
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp).testTag("gdrive_restore_button_${backup.id}"),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Restaurar", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
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

    // Google Drive Dialogs & Overlays
    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = { showAuthDialog = false },
            title = { Text("Conectar Google Drive") },
            text = {
                Box(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { ctx ->
                            android.webkit.WebView(ctx).apply {
                                this.getSettings().javaScriptEnabled = true
                                this.getSettings().domStorageEnabled = true
                                this.getSettings().useWideViewPort = true
                                this.getSettings().loadWithOverviewMode = true
                                webViewClient = object : android.webkit.WebViewClient() {
                                    override fun shouldOverrideUrlLoading(
                                        view: android.webkit.WebView,
                                        request: android.webkit.WebResourceRequest
                                    ): Boolean {
                                        val url = request.url.toString()
                                        if (url.startsWith("http://localhost")) {
                                            val code = request.url.getQueryParameter("code")
                                            if (code != null) {
                                                viewModel.connectGDrive(code) { success ->
                                                    if (success) {
                                                        showAuthDialog = false
                                                    }
                                                }
                                                return true
                                            }
                                        }
                                        return false
                                    }

                                    override fun onPageStarted(
                                        view: android.webkit.WebView?,
                                        url: String?,
                                        favicon: android.graphics.Bitmap?
                                    ) {
                                        super.onPageStarted(view, url, favicon)
                                        if (url != null && url.startsWith("http://localhost")) {
                                            val uri = android.net.Uri.parse(url)
                                            val code = uri.getQueryParameter("code")
                                            if (code != null) {
                                                viewModel.connectGDrive(code) { success ->
                                                    if (success) {
                                                        showAuthDialog = false
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        update = { webView ->
                            webView.loadUrl(authUrl)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showAuthDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showRestoreConfirm && selectedBackupForRestore != null) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false },
            title = { Text("Confirmar Restauração") },
            text = {
                Text("ATENÇÃO: Você está prestes a restaurar o backup [${selectedBackupForRestore?.name?.replace("salon_backup_", "")?.replace(".sqlite", "")?.replace("_", " às ")}].\n\nIsso irá SUBSTITUIR COMPLETAMENTE todas as informações atuais (agendamentos, clientes, faturamento, configurações) do seu salão de forma irreversível. Deseja continuar?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreConfirm = false
                        selectedBackupForRestore?.let { backup ->
                            viewModel.restoreBackupFromGDrive(backup.id) { success ->
                                if (success) {
                                    showRestoreSuccess = true
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Substituir Dados e Restaurar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRestoreConfirm = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showRestoreSuccess) {
        AlertDialog(
            onDismissRequest = { /* force action */ },
            title = { Text("Restauração Concluída") },
            text = {
                Text("O banco de dados foi restaurado com sucesso!\n\nO aplicativo precisa ser reiniciado para carregar os novos dados com total segurança.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreSuccess = false
                        viewModel.restartApp()
                    }
                ) {
                    Text("Reiniciar Aplicativo")
                }
            }
        )
    }
}
