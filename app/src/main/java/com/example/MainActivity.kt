package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.SalonViewModel
import com.example.ui.components.AiDemoTourOverlay
import com.example.ui.components.ConfirmationAlertDialog
import com.example.ui.components.FirstUseSetupWizardDialog
import com.example.ui.components.SalonTopAppBar
import com.example.ui.screens.appointments.AppointmentsScreen
import com.example.ui.screens.clients.ClientsScreen
import com.example.ui.screens.finance.FinanceScreen
import com.example.ui.screens.nfse.NfseScreen
import com.example.ui.screens.professionals.ProfessionalsScreen
import com.example.ui.screens.salon_settings.SalonSettingsScreen
import com.example.ui.screens.services.ServicesScreen
import com.example.ui.screens.whatsapp.WhatsAppScreen
import com.example.ui.theme.DynamicSalonTheme
import com.example.utils.NavigationConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: SalonViewModel = viewModel()

            val settings by viewModel.settingsState.collectAsStateWithLifecycle()
            val clients by viewModel.clientsState.collectAsStateWithLifecycle()
            val services by viewModel.servicesState.collectAsStateWithLifecycle()
            val professionals by viewModel.professionalsState.collectAsStateWithLifecycle()
            val appointments by viewModel.appointmentsState.collectAsStateWithLifecycle()
            val nfseList by viewModel.nfseListState.collectAsStateWithLifecycle()
            val nfseConfig by viewModel.nfseConfigState.collectAsStateWithLifecycle()
            val whatsappTemplates by viewModel.whatsappTemplatesState.collectAsStateWithLifecycle()
            val whatsappLogs by viewModel.whatsappLogsState.collectAsStateWithLifecycle()
            val financialTransactions by viewModel.financialTransactionsState.collectAsStateWithLifecycle()
            val typeFilter by viewModel.selectedFinanceTypeFilter.collectAsStateWithLifecycle()
            val periodFilter by viewModel.selectedFinancePeriodFilter.collectAsStateWithLifecycle()

            val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
            val viewModeGrid by viewModel.viewModeGrid.collectAsStateWithLifecycle()
            val confirmationDialogText by viewModel.confirmationDialogText.collectAsStateWithLifecycle()

            var currentNavTab by remember { mutableStateOf(settings.defaultStartScreen) }
            var showMenuDropdown by remember { mutableStateOf(false) }

            // Sync initial default screen once loaded
            LaunchedEffect(settings.defaultStartScreen) {
                if (currentNavTab.isEmpty()) {
                    currentNavTab = settings.defaultStartScreen
                }
            }

            // Notification permission request for Android 13+
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { /* ignore or handle */ }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                    if (!hasPermission) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            DynamicSalonTheme(
                paletteKey = settings.themePalette,
                darkModeOption = settings.darkModeOption,
                useDynamicColor = settings.useDynamicColor
            ) {
                val screenTitle = when (currentNavTab) {
                    "AGENDAMENTOS" -> "Agendamentos & Agenda"
                    "SERVICOS" -> "Gestão de Serviços"
                    "PROFISSIONAIS" -> "Profissionais & Escala"
                    "CLIENTES" -> "Gestão de Clientes"
                    "FINANCEIRO" -> "Gestão Financeira & Caixa"
                    "NFSE" -> "Plataforma NFS-e"
                    "WHATSAPP" -> "WhatsApp & Mensagens"
                    "CONFIGURANCOES" -> "Configurações do Salão"
                    else -> "Gold Salão"
                }

                val isGridView = viewModeGrid[currentNavTab] ?: false

                Scaffold(
                    topBar = {
                        SalonTopAppBar(
                            salonName = settings.nomeFantasia,
                            currentScreenTitle = screenTitle,
                            isGridView = isGridView,
                            onToggleViewMode = { viewModel.toggleViewMode(currentNavTab) },
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            onOpenSettings = { currentNavTab = "CONFIGURANCOES" },
                            onOpenWhatsApp = { currentNavTab = "WHATSAPP" },
                            onNavigateBack = if (currentNavTab == "CONFIGURANCOES") {
                                { currentNavTab = settings.defaultStartScreen.ifEmpty { "AGENDAMENTOS" } }
                            } else null
                        )
                    },
                    bottomBar = {
                        val orderedModules = remember(settings.bottomNavOrder) {
                            NavigationConfig.parseNavOrder(settings.bottomNavOrder)
                        }
                        val primaryNavItems = orderedModules.take(4)
                        val secondaryNavItems = orderedModules.drop(4)

                        NavigationBar(
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("bottom_navigation_bar")
                        ) {
                            primaryNavItems.forEach { item ->
                                val isSelected = currentNavTab == item.id
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentNavTab = item.id },
                                    icon = {
                                        if (item.customDrawableRes != null) {
                                            Icon(
                                                painter = painterResource(id = item.customDrawableRes),
                                                contentDescription = item.shortLabel,
                                                tint = Color.Unspecified,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = if (isSelected) (item.iconFilled ?: Icons.Default.Dashboard) else (item.iconOutlined ?: item.iconFilled ?: Icons.Default.Dashboard),
                                                contentDescription = item.shortLabel
                                            )
                                        }
                                    },
                                    label = { Text(item.shortLabel, style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp), maxLines = 1, softWrap = false) },
                                    alwaysShowLabel = true,
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }

                            if (secondaryNavItems.isNotEmpty()) {
                                val isSecondarySelected = currentNavTab in secondaryNavItems.map { it.id }
                                NavigationBarItem(
                                    selected = isSecondarySelected,
                                    onClick = { showMenuDropdown = true },
                                    icon = {
                                        Box {
                                            Icon(
                                                imageVector = Icons.Default.MoreHoriz,
                                                contentDescription = "Mais Módulos"
                                            )
                                            DropdownMenu(
                                                expanded = showMenuDropdown,
                                                onDismissRequest = { showMenuDropdown = false }
                                            ) {
                                                secondaryNavItems.forEach { secItem ->
                                                    DropdownMenuItem(
                                                        text = { Text(secItem.fullTitle) },
                                                        leadingIcon = {
                                                            if (secItem.customDrawableRes != null) {
                                                                Icon(
                                                                    painter = painterResource(id = secItem.customDrawableRes),
                                                                    contentDescription = null,
                                                                    tint = Color.Unspecified,
                                                                    modifier = Modifier.size(20.dp)
                                                                )
                                                            } else {
                                                                Icon(
                                                                    imageVector = secItem.iconFilled ?: Icons.Default.Dashboard,
                                                                    contentDescription = null
                                                                )
                                                            }
                                                        },
                                                        onClick = {
                                                            currentNavTab = secItem.id
                                                            showMenuDropdown = false
                                                        },
                                                        modifier = Modifier.testTag("dropdown_item_${secItem.id.lowercase()}")
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    label = { Text("Mais", style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp), maxLines = 1, softWrap = false) },
                                    alwaysShowLabel = true,
                                    modifier = Modifier.testTag("nav_item_mais")
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentNavTab) {
                            "AGENDAMENTOS" -> AppointmentsScreen(
                                viewModel = viewModel,
                                appointments = appointments,
                                clients = clients,
                                services = services,
                                professionals = professionals,
                                nfseList = nfseList,
                                masterPassword = settings.masterPassword,
                                isGridView = isGridView,
                                searchQuery = searchQuery
                            )

                            "SERVICOS" -> ServicesScreen(
                                viewModel = viewModel,
                                services = services,
                                professionals = professionals,
                                nfseList = nfseList,
                                masterPassword = settings.masterPassword,
                                isGridView = isGridView,
                                searchQuery = searchQuery
                            )

                            "PROFISSIONAIS" -> ProfessionalsScreen(
                                viewModel = viewModel,
                                professionals = professionals,
                                isGridView = isGridView,
                                searchQuery = searchQuery
                            )

                            "CLIENTES" -> ClientsScreen(
                                viewModel = viewModel,
                                clients = clients,
                                isGridView = isGridView,
                                searchQuery = searchQuery
                            )

                            "FINANCEIRO" -> FinanceScreen(
                                viewModel = viewModel,
                                transactions = financialTransactions,
                                typeFilter = typeFilter,
                                periodFilter = periodFilter,
                                isGridView = isGridView,
                                searchQuery = searchQuery
                            )

                            "NFSE" -> NfseScreen(
                                viewModel = viewModel,
                                nfseList = nfseList,
                                nfseConfig = nfseConfig,
                                isGridView = isGridView,
                                searchQuery = searchQuery
                            )

                            "WHATSAPP" -> WhatsAppScreen(
                                viewModel = viewModel,
                                templates = whatsappTemplates,
                                logs = whatsappLogs,
                                clients = clients
                            )

                            "CONFIGURANCOES" -> SalonSettingsScreen(
                                viewModel = viewModel,
                                settings = settings,
                                onClose = {
                                    currentNavTab = settings.defaultStartScreen.ifBlank { "AGENDAMENTOS" }
                                }
                            )

                            else -> AppointmentsScreen(
                                viewModel = viewModel,
                                appointments = appointments,
                                clients = clients,
                                services = services,
                                professionals = professionals,
                                nfseList = nfseList,
                                masterPassword = settings.masterPassword,
                                isGridView = isGridView,
                                searchQuery = searchQuery
                            )
                        }
                    }
                }

                // Action Security Confirmation Dialog
                if (confirmationDialogText != null) {
                    ConfirmationAlertDialog(
                        title = "Confirmação Obrigatória",
                        message = confirmationDialogText!!,
                        onConfirm = { viewModel.confirmAction() },
                        onDismiss = { viewModel.dismissConfirmation() }
                    )
                }

                AiDemoTourOverlay(
                    isDemoActive = settings.demoModeEnabled,
                    demoSpeed = settings.demoSpeed,
                    onSpeedChange = { newSpeed ->
                        val updated = settings.copy(demoSpeed = newSpeed)
                        viewModel.saveSettings(updated)
                    },
                    onCloseDemo = {
                        val updated = settings.copy(demoModeEnabled = false)
                        viewModel.saveSettings(updated)
                    },
                    onNavigateTab = { tab ->
                        currentNavTab = tab
                    }
                )

                if (!settings.isFirstUseCompleted) {
                    FirstUseSetupWizardDialog(
                        settings = settings,
                        onCompleteSetup = { updatedSettings ->
                            viewModel.saveSettings(updatedSettings)
                        }
                    )
                }
            }
        }
    }
}
