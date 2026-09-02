package com.example.ui.screens.salon_settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.SalonSettingsEntity
import com.example.ui.SalonViewModel
import com.example.ui.components.MaskedOutlinedTextField
import com.example.ui.theme.getPaletteColors
import com.example.utils.NavigationConfig

@Composable
fun SalonHeaderBackground(
    imageUriOrPreset: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    when {
        imageUriOrPreset == "preset:luxury_spa" -> {
            Image(
                painter = painterResource(id = R.drawable.img_banner_luxury_spa_1788265661724),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        }
        imageUriOrPreset == "preset:modern_studio" -> {
            Image(
                painter = painterResource(id = R.drawable.img_banner_modern_studio_1788265675431),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        }
        imageUriOrPreset == "preset:hero_salon" || imageUriOrPreset.isBlank() -> {
            Image(
                painter = painterResource(id = R.drawable.img_hero_salon_1785407233613),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        }
        else -> {
            AsyncImage(
                model = imageUriOrPreset,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.img_hero_salon_1785407233613),
                error = painterResource(id = R.drawable.img_hero_salon_1785407233613)
            )
        }
    }
}

@Composable
fun SalonSettingsScreen(
    viewModel: SalonViewModel,
    settings: SalonSettingsEntity,
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {}
) {
    val context = LocalContext.current

    var razaoSocial by remember(settings) { mutableStateOf(settings.razaoSocial) }
    var nomeFantasia by remember(settings) { mutableStateOf(settings.nomeFantasia) }
    var cnpj by remember(settings) { mutableStateOf(settings.cnpj) }
    var inscricaoEstadual by remember(settings) { mutableStateOf(settings.inscricaoEstadual) }
    var defaultStartScreen by remember(settings) { mutableStateOf(settings.defaultStartScreen) }
    var selectedPalette by remember(settings) { mutableStateOf(settings.themePalette) }
    var darkModeOption by remember(settings) { mutableStateOf(settings.darkModeOption) }
    var useDynamicColor by remember(settings) { mutableStateOf(settings.useDynamicColor) }
    var masterPassword by remember(settings) { mutableStateOf(settings.masterPassword) }
    var demoModeEnabled by remember(settings) { mutableStateOf(settings.demoModeEnabled) }
    var demoSpeed by remember(settings) { mutableStateOf(settings.demoSpeed) }
    var headerBackgroundUri by remember(settings) {
        mutableStateOf(settings.headerBackgroundUri.ifBlank { "preset:hero_salon" })
    }
    var bottomNavOrderList by remember(settings.bottomNavOrder) {
        mutableStateOf(NavigationConfig.parseNavOrder(settings.bottomNavOrder))
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            headerBackgroundUri = uri.toString()
            Toast.makeText(context, "Imagem de fundo do cabeçalho selecionada!", Toast.LENGTH_SHORT).show()
        }
    }

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
    var inputAuthCode by remember { mutableStateOf("") }
    var customClientId by remember { mutableStateOf(com.example.data.GoogleDriveBackupManager.getClientId(context)) }
    var customClientSecret by remember { mutableStateOf(com.example.data.GoogleDriveBackupManager.getClientSecret(context)) }
    var showAdvancedOAuth by remember { mutableStateOf(false) }

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
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Hero Banner Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth().testTag("salon_hero_banner_card"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                SalonHeaderBackground(
                    imageUriOrPreset = headerBackgroundUri,
                    contentDescription = "Salon Hero",
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                        .padding(14.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = nomeFantasia.ifBlank { "Seu Salão" },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Personalize o cabeçalho, preferências visuais e dados do salão.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        FilledTonalButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color.White.copy(alpha = 0.9f),
                                contentColor = Color.Black
                            ),
                            modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp).testTag("quick_change_banner_button")
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Trocar Foto", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section 1: Business Details (Compact Spacings)
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Dados da Empresa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = nomeFantasia,
                    onValueChange = { nomeFantasia = it },
                    label = { Text("Nome Fantasia (exibido na TopBar) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("nome_fantasia_input")
                )

                OutlinedTextField(
                    value = razaoSocial,
                    onValueChange = { razaoSocial = it },
                    label = { Text("Razão Social *") },
                    singleLine = true,
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

        // Section: Modo Demonstrativo com IA & Voz
        ElevatedCard(
            modifier = Modifier.fillMaxWidth().testTag("demo_mode_card"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Modo Demonstrativo com IA & Voz",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Switch(
                        checked = demoModeEnabled,
                        onCheckedChange = { demoModeEnabled = it },
                        modifier = Modifier.testTag("demo_mode_switch")
                    )
                }

                Text(
                    text = "Ative o modo demonstrativo guiado por voz e inteligência artificial para apresentar os recursos do Gold Salão passo a passo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (demoModeEnabled) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Face,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Voz Feminina Natural ativada por padrão com controles de tom e cadência.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Velocidade da Voz e Demonstração: ${demoSpeed}x",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                            FilterChip(
                                selected = demoSpeed == speed,
                                onClick = { demoSpeed = speed },
                                label = { Text("${speed}x") }
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Sequência dos Botões do Menu Inferior & Definição da Tela Inicial (Unificados)
        ElevatedCard(
            modifier = Modifier.fillMaxWidth().testTag("bottom_nav_order_card"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ViewAgenda, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sequência dos Botões & Tela Inicial",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(
                        onClick = {
                            val defaultList = NavigationConfig.parseNavOrder(NavigationConfig.DEFAULT_NAV_ORDER)
                            bottomNavOrderList = defaultList
                            val serialized = NavigationConfig.serializeNavOrder(defaultList)
                            viewModel.saveSettings(
                                settings.copy(
                                    razaoSocial = razaoSocial,
                                    nomeFantasia = nomeFantasia,
                                    cnpj = cnpj,
                                    inscricaoEstadual = inscricaoEstadual,
                                    defaultStartScreen = defaultStartScreen,
                                    themePalette = selectedPalette,
                                    darkModeOption = darkModeOption,
                                    useDynamicColor = useDynamicColor,
                                    masterPassword = masterPassword,
                                    bottomNavOrder = serialized,
                                    headerBackgroundUri = headerBackgroundUri
                                )
                            )
                        }
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Padrão", fontSize = 12.sp)
                    }
                }

                Text(
                    text = "Ordene a prioridade dos botões na barra inferior (os 4 primeiros ficam visíveis no rodapé) e marque o botão de opção (★) para definir qual módulo será a Tela Inicial ao abrir o aplicativo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Live Preview
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Prévia da Barra Inferior:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            val startScreenName = bottomNavOrderList.find { it.id == defaultStartScreen }?.shortLabel ?: "Agendamentos"
                            Text(
                                text = "★ Início: $startScreenName",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val previewPrimary = bottomNavOrderList.take(4)
                            previewPrimary.forEach { item ->
                                val isStart = item.id == defaultStartScreen
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(contentAlignment = Alignment.TopEnd) {
                                        if (item.customDrawableRes != null) {
                                            Icon(
                                                painter = painterResource(id = item.customDrawableRes),
                                                contentDescription = null,
                                                tint = Color.Unspecified,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = item.iconFilled ?: Icons.Default.Dashboard,
                                                contentDescription = null,
                                                tint = if (isStart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        if (isStart) {
                                            Text("★", color = MaterialTheme.colorScheme.primary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(
                                        text = item.shortLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        maxLines = 1,
                                        fontWeight = if (isStart) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isStart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreHoriz,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Mais (+${bottomNavOrderList.size - 4})",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }

                // Reordering items with Integrated Initial Screen Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    bottomNavOrderList.forEachIndexed { index, item ->
                        val isDirectNav = index < 4
                        val isStartScreen = defaultStartScreen == item.id

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isStartScreen) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            } else if (isDirectNav) {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            border = BorderStroke(
                                width = if (isStartScreen) 1.5.dp else 1.dp,
                                color = if (isStartScreen) {
                                    MaterialTheme.colorScheme.primary
                                } else if (isDirectNav) {
                                    MaterialTheme.colorScheme.outlineVariant
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            defaultStartScreen = item.id
                                            viewModel.saveSettings(
                                                settings.copy(
                                                    razaoSocial = razaoSocial,
                                                    nomeFantasia = nomeFantasia,
                                                    cnpj = cnpj,
                                                    inscricaoEstadual = inscricaoEstadual,
                                                    defaultStartScreen = item.id,
                                                    themePalette = selectedPalette,
                                                    darkModeOption = darkModeOption,
                                                    useDynamicColor = useDynamicColor,
                                                    masterPassword = masterPassword,
                                                    bottomNavOrder = NavigationConfig.serializeNavOrder(bottomNavOrderList)
                                                )
                                            )
                                        },
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RadioButton(
                                        selected = isStartScreen,
                                        onClick = {
                                            defaultStartScreen = item.id
                                            viewModel.saveSettings(
                                                settings.copy(
                                                    razaoSocial = razaoSocial,
                                                    nomeFantasia = nomeFantasia,
                                                    cnpj = cnpj,
                                                    inscricaoEstadual = inscricaoEstadual,
                                                    defaultStartScreen = item.id,
                                                    themePalette = selectedPalette,
                                                    darkModeOption = darkModeOption,
                                                    useDynamicColor = useDynamicColor,
                                                    masterPassword = masterPassword,
                                                    bottomNavOrder = NavigationConfig.serializeNavOrder(bottomNavOrderList)
                                                )
                                            )
                                        },
                                        modifier = Modifier.size(24.dp)
                                    )

                                    Surface(
                                        shape = CircleShape,
                                        color = if (isStartScreen) MaterialTheme.colorScheme.primary else if (isDirectNav) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${index + 1}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                fontWeight = FontWeight.Bold,
                                                color = if (isStartScreen) MaterialTheme.colorScheme.onPrimary else if (isDirectNav) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    if (item.customDrawableRes != null) {
                                        Icon(
                                            painter = painterResource(id = item.customDrawableRes),
                                            contentDescription = null,
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = item.iconFilled ?: Icons.Default.Dashboard,
                                            contentDescription = null,
                                            tint = if (isStartScreen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = item.fullTitle,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isStartScreen) FontWeight.Bold else FontWeight.SemiBold,
                                                color = if (isStartScreen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1
                                            )
                                            if (isStartScreen) {
                                                SuggestionChip(
                                                    onClick = {},
                                                    label = { Text("★ Tela Inicial", fontSize = 8.sp, fontWeight = FontWeight.Bold) },
                                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                                        containerColor = MaterialTheme.colorScheme.primary,
                                                        labelColor = MaterialTheme.colorScheme.onPrimary
                                                    ),
                                                    modifier = Modifier.height(18.dp)
                                                )
                                            } else if (isDirectNav) {
                                                SuggestionChip(
                                                    onClick = {},
                                                    label = { Text("Barra", fontSize = 8.sp) },
                                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                    ),
                                                    modifier = Modifier.height(18.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = item.description,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            if (index > 0) {
                                                val mutable = bottomNavOrderList.toMutableList()
                                                val temp = mutable[index]
                                                mutable[index] = mutable[index - 1]
                                                mutable[index - 1] = temp
                                                bottomNavOrderList = mutable
                                                val serialized = NavigationConfig.serializeNavOrder(mutable)
                                                viewModel.saveSettings(
                                                    settings.copy(
                                                        razaoSocial = razaoSocial,
                                                        nomeFantasia = nomeFantasia,
                                                        cnpj = cnpj,
                                                        inscricaoEstadual = inscricaoEstadual,
                                                        defaultStartScreen = defaultStartScreen,
                                                        themePalette = selectedPalette,
                                                        darkModeOption = darkModeOption,
                                                        useDynamicColor = useDynamicColor,
                                                        masterPassword = masterPassword,
                                                        bottomNavOrder = serialized
                                                    )
                                                )
                                            }
                                        },
                                        enabled = index > 0,
                                        modifier = Modifier.size(28.dp).testTag("move_up_${item.id.lowercase()}")
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowUpward,
                                            contentDescription = "Mover para cima",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            if (index < bottomNavOrderList.size - 1) {
                                                val mutable = bottomNavOrderList.toMutableList()
                                                val temp = mutable[index]
                                                mutable[index] = mutable[index + 1]
                                                mutable[index + 1] = temp
                                                bottomNavOrderList = mutable
                                                val serialized = NavigationConfig.serializeNavOrder(mutable)
                                                viewModel.saveSettings(
                                                    settings.copy(
                                                        razaoSocial = razaoSocial,
                                                        nomeFantasia = nomeFantasia,
                                                        cnpj = cnpj,
                                                        inscricaoEstadual = inscricaoEstadual,
                                                        defaultStartScreen = defaultStartScreen,
                                                        themePalette = selectedPalette,
                                                        darkModeOption = darkModeOption,
                                                        useDynamicColor = useDynamicColor,
                                                        masterPassword = masterPassword,
                                                        bottomNavOrder = serialized
                                                    )
                                                )
                                            }
                                        },
                                        enabled = index < bottomNavOrderList.size - 1,
                                        modifier = Modifier.size(28.dp).testTag("move_down_${item.id.lowercase()}")
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowDownward,
                                            contentDescription = "Mover para baixo",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
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
                            authUrl = com.example.data.GoogleDriveBackupManager.getOAuthLoginUrl(context)
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

        // Bottom Action Buttons: Save (Closes screen) & Close/Cancel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onClose,
                modifier = Modifier
                    .weight(0.35f)
                    .height(50.dp)
                    .testTag("cancel_settings_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Fechar", fontWeight = FontWeight.SemiBold)
            }

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
                        masterPassword = masterPassword,
                        bottomNavOrder = NavigationConfig.serializeNavOrder(bottomNavOrderList),
                        headerBackgroundUri = headerBackgroundUri,
                        demoModeEnabled = demoModeEnabled,
                        demoSpeed = demoSpeed
                    )
                    viewModel.saveSettings(updated)
                    Toast.makeText(context, "Configurações do salão salvas com sucesso!", Toast.LENGTH_SHORT).show()
                    onClose()
                },
                modifier = Modifier
                    .weight(0.65f)
                    .height(50.dp)
                    .testTag("save_settings_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Salvar Configurações", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Google Drive Dialogs & Overlays
    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = { showAuthDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Conectar Google Drive", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Para conectar a sua conta do Google Drive com segurança, siga os passos abaixo:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "1. Autorizar no Navegador Externo",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                "Clique abaixo para abrir a autorização oficial do Google no seu navegador.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Button(
                                onClick = {
                                    val loginUrl = com.example.data.GoogleDriveBackupManager.getOAuthLoginUrl(context)
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse(loginUrl)
                                    )
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Abrir Autorização no Google")
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Se aparecer 'App Não Verificado' do Google:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            }
                            Text(
                                "1. Clique no link 'Avançado' na tela do Google.\n2. Em seguida, clique em 'Ir para [Nome do App] (não seguro)'.\n3. Conceda as permissões solicitadas.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "2. Cole a URL de Retorno, Código ou Access Token",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                "Copie o link completo (URL) da página final de sucesso, o código gerado ou o Access Token (ex: ya29...) e cole abaixo:",
                                style = MaterialTheme.typography.bodySmall
                            )
                            OutlinedTextField(
                                value = inputAuthCode,
                                onValueChange = { inputAuthCode = it },
                                placeholder = { Text("Cole aqui o código, URL ou token ya29...") },
                                modifier = Modifier.fillMaxWidth().testTag("gdrive_auth_code_input"),
                                singleLine = false,
                                maxLines = 3,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    TextButton(
                        onClick = { showAdvancedOAuth = !showAdvancedOAuth },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(if (showAdvancedOAuth) "Ocultar Credenciais OAuth" else "Configurar OAuth Customizado")
                    }

                    if (showAdvancedOAuth) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = customClientId,
                                onValueChange = { customClientId = it },
                                label = { Text("Client ID do Google Cloud") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = customClientSecret,
                                onValueChange = { customClientSecret = it },
                                label = { Text("Client Secret (Opcional)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    com.example.data.GoogleDriveBackupManager.saveClientCredentials(context, customClientId, customClientSecret)
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Salvar Credenciais")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputAuthCode.isNotBlank()) {
                            viewModel.connectGDrive(inputAuthCode) { success ->
                                if (success) {
                                    showAuthDialog = false
                                    inputAuthCode = ""
                                }
                            }
                        }
                    },
                    enabled = inputAuthCode.isNotBlank() && !isLoading,
                    modifier = Modifier.testTag("gdrive_confirm_auth_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text("Conectar")
                }
            },
            dismissButton = {
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
