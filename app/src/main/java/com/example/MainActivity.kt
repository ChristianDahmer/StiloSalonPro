package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.SalonViewModel
import com.example.ui.components.ConfirmationAlertDialog
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
            var showQuickThemeDialog by remember { mutableStateOf(false) }

            // Sync initial default screen once loaded
            LaunchedEffect(settings.defaultStartScreen) {
                if (currentNavTab.isEmpty()) {
                    currentNavTab = settings.defaultStartScreen
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
                    else -> "Gestão de Salão"
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
                            onOpenThemeSelector = { showQuickThemeDialog = true }
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("bottom_navigation_bar")
                        ) {
                            NavigationBarItem(
                                selected = currentNavTab == "AGENDAMENTOS",
                                onClick = { currentNavTab = "AGENDAMENTOS" },
                                icon = {
                                    Icon(
                                        imageVector = if (currentNavTab == "AGENDAMENTOS") Icons.Default.CalendarMonth else Icons.Outlined.CalendarMonth,
                                        contentDescription = "Agendamentos"
                                    )
                                },
                                label = { Text("Agenda") },
                                modifier = Modifier.testTag("nav_item_agendamentos")
                            )

                            NavigationBarItem(
                                selected = currentNavTab == "CLIENTES",
                                onClick = { currentNavTab = "CLIENTES" },
                                icon = {
                                    Icon(
                                        imageVector = if (currentNavTab == "CLIENTES") Icons.Default.People else Icons.Outlined.People,
                                        contentDescription = "Clientes"
                                    )
                                },
                                label = { Text("Clientes") },
                                modifier = Modifier.testTag("nav_item_clientes")
                            )

                            NavigationBarItem(
                                selected = currentNavTab == "FINANCEIRO",
                                onClick = { currentNavTab = "FINANCEIRO" },
                                icon = {
                                    Icon(
                                        imageVector = if (currentNavTab == "FINANCEIRO") Icons.Default.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                                        contentDescription = "Financeiro"
                                    )
                                },
                                label = { Text("Financeiro") },
                                modifier = Modifier.testTag("nav_item_financeiro")
                            )

                            NavigationBarItem(
                                selected = currentNavTab == "WHATSAPP",
                                onClick = { currentNavTab = "WHATSAPP" },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_whatsapp),
                                        contentDescription = "WhatsApp",
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = { Text("WhatsApp") },
                                modifier = Modifier.testTag("nav_item_whatsapp")
                            )

                            NavigationBarItem(
                                selected = currentNavTab == "NFSE",
                                onClick = { currentNavTab = "NFSE" },
                                icon = {
                                    Icon(
                                        imageVector = if (currentNavTab == "NFSE") Icons.Default.ReceiptLong else Icons.Outlined.ReceiptLong,
                                        contentDescription = "NFS-e"
                                    )
                                },
                                label = { Text("NFS-e") },
                                modifier = Modifier.testTag("nav_item_nfse")
                            )

                            NavigationBarItem(
                                selected = currentNavTab in listOf("SERVICOS", "PROFISSIONAIS", "CONFIGURANCOES"),
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
                                            DropdownMenuItem(
                                                text = { Text("Agenda & Atendimentos") },
                                                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                                                onClick = {
                                                    currentNavTab = "AGENDAMENTOS"
                                                    showMenuDropdown = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Gestão Financeira / Caixa") },
                                                leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                                                onClick = {
                                                    currentNavTab = "FINANCEIRO"
                                                    showMenuDropdown = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Serviços") },
                                                leadingIcon = { Icon(Icons.Default.ContentCut, contentDescription = null) },
                                                onClick = {
                                                    currentNavTab = "SERVICOS"
                                                    showMenuDropdown = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Equipe / Profissionais") },
                                                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                                                onClick = {
                                                    currentNavTab = "PROFISSIONAIS"
                                                    showMenuDropdown = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Gestão de Clientes") },
                                                leadingIcon = { Icon(Icons.Default.People, contentDescription = null) },
                                                onClick = {
                                                    currentNavTab = "CLIENTES"
                                                    showMenuDropdown = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("WhatsApp & Mensagens") },
                                                leadingIcon = {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.ic_whatsapp),
                                                        contentDescription = null,
                                                        tint = Color.Unspecified,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                },
                                                onClick = {
                                                    currentNavTab = "WHATSAPP"
                                                    showMenuDropdown = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Plataforma NFS-e") },
                                                leadingIcon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                                                onClick = {
                                                    currentNavTab = "NFSE"
                                                    showMenuDropdown = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Configurações do Salão") },
                                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                                onClick = {
                                                    currentNavTab = "CONFIGURANCOES"
                                                    showMenuDropdown = false
                                                }
                                            )
                                        }
                                    }
                                },
                                label = { Text("Mais") },
                                modifier = Modifier.testTag("nav_item_mais")
                            )
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
                                settings = settings
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

                // Quick Theme Picker Dialog
                if (showQuickThemeDialog) {
                    com.example.ui.components.QuickThemePickerDialog(
                        currentPalette = settings.themePalette,
                        currentDarkMode = settings.darkModeOption,
                        onDismiss = { showQuickThemeDialog = false },
                        onSelectTheme = { selectedPalette, selectedDarkMode ->
                            viewModel.saveSettings(settings.copy(themePalette = selectedPalette, darkModeOption = selectedDarkMode))
                        }
                    )
                }
            }
        }
    }
}
