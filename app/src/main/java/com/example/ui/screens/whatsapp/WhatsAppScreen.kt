package com.example.ui.screens.whatsapp

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.*
import com.example.ui.SalonViewModel
import com.example.utils.MaskUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WhatsAppScreen(
    viewModel: SalonViewModel,
    templates: List<WhatsAppTemplateEntity>,
    logs: List<WhatsAppLogEntity>,
    clients: List<ClientEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 = Monitor IA, 1 = Notificações Equipe, 2 = Modelos, 3 = Campanhas, 4 = Logs

    val inboundMessages by viewModel.inboundMessagesState.collectAsStateWithLifecycle()
    val notifications by viewModel.professionalNotificationsState.collectAsStateWithLifecycle()
    val aiConfig by viewModel.aiMonitoringConfigState.collectAsStateWithLifecycle()
    val isAiProcessing by viewModel.isAiProcessing.collectAsStateWithLifecycle()
    val pendingInboundCount by viewModel.pendingInboundCount.collectAsStateWithLifecycle()
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()

    var showSimulateDialog by remember { mutableStateOf(false) }
    var templateToEdit by remember { mutableStateOf<WhatsAppTemplateEntity?>(null) }
    var showAiConfigDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // Header Banner (Compact & Consistent Height)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF25D366).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_whatsapp),
                            contentDescription = "WhatsApp Logo",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Central WhatsApp & IA",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color(0xFF2E7D32), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "IA Ativa",
                                        color = Color(0xFF2E7D32),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Monitoramento inteligente de clientes",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = { showAiConfigDialog = true },
                    modifier = Modifier.testTag("ai_config_button"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Configurações IA",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Configurar IA", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Tab Navigation
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Monitor IA (Entrada)")
                        if (pendingInboundCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Badge(containerColor = MaterialTheme.colorScheme.error) {
                                Text("$pendingInboundCount")
                            }
                        }
                    }
                },
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) }
            )

            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Notificações Equipe")
                        if (unreadNotificationsCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                Text("$unreadNotificationsCount")
                            }
                        }
                    }
                },
                icon = { Icon(Icons.Default.NotificationsActive, contentDescription = null) }
            )

            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Modelos (${templates.size})") },
                icon = { Icon(Icons.Outlined.TextSnippet, contentDescription = null) }
            )

            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Campanhas") },
                icon = { Icon(Icons.Outlined.Campaign, contentDescription = null) }
            )

            Tab(
                selected = selectedTab == 4,
                onClick = { selectedTab = 4 },
                text = { Text("Histórico (${logs.size})") },
                icon = { Icon(Icons.Outlined.History, contentDescription = null) }
            )

            Tab(
                selected = selectedTab == 5,
                onClick = { selectedTab = 5 },
                text = { Text("Configurações IA") },
                icon = { Icon(Icons.Outlined.Tune, contentDescription = null) }
            )

            Tab(
                selected = selectedTab == 6,
                onClick = { selectedTab = 6 },
                text = { Text("Fila de Espera") },
                icon = { Icon(Icons.Outlined.People, contentDescription = null) }
            )

            Tab(
                selected = selectedTab == 7,
                onClick = { selectedTab = 7 },
                text = { Text("Clube VIP") },
                icon = { Icon(Icons.Outlined.Star, contentDescription = null) }
            )

            Tab(
                selected = selectedTab == 8,
                onClick = { selectedTab = 8 },
                text = { Text("Comandos de Voz") },
                icon = { Icon(Icons.Outlined.Mic, contentDescription = null) }
            )
        }

        // Tab Content
        when (selectedTab) {
            0 -> WhatsAppAiMonitorTab(
                inboundMessages = inboundMessages,
                isAiProcessing = isAiProcessing,
                onSimulateClick = { showSimulateDialog = true },
                onApprove = { viewModel.approveInboundBooking(it, context) },
                onReject = { viewModel.rejectInboundMessage(it) },
                onDelete = { viewModel.deleteInboundMessage(it) },
                onReplyClient = { viewModel.replyToInboundClient(context, it) },
                onNotifyProfessional = {
                    val notif = ProfessionalNotificationEntity(
                        professionalId = it.extractedProfessionalId ?: 0,
                        professionalName = it.extractedProfessionalName.ifBlank { "Profissional" },
                        professionalPhone = "(11) 91111-2222",
                        title = "Novo Agendamento Solicitado por WhatsApp",
                        message = "${it.extractedClientName} solicitou ${it.extractedServiceName} para ${MaskUtils.formatDatePtBr(it.extractedDate)} às ${it.extractedTime}.",
                        timestamp = System.currentTimeMillis(),
                        isRead = false,
                        inboundMessageId = it.id,
                        type = "NOVO_AGENDAMENTO"
                    )
                    viewModel.sendProfessionalWhatsAppAlert(context, notif)
                },
                onUpdateSuggestedReply = { msg, text -> viewModel.updateInboundSuggestedReply(msg, text) }
            )

            1 -> ProfessionalNotificationsTab(
                notifications = notifications,
                onMarkRead = { viewModel.markNotificationAsRead(it) },
                onMarkAllRead = { viewModel.markAllNotificationsAsRead() },
                onDelete = { viewModel.deleteProfessionalNotification(it) },
                onSendWhatsAppAlert = { viewModel.sendProfessionalWhatsAppAlert(context, it) }
            )

            2 -> WhatsAppTemplatesTab(
                templates = templates,
                onEditTemplate = { templateToEdit = it },
                onCreateNewTemplate = {
                    templateToEdit = WhatsAppTemplateEntity(
                        id = 0L,
                        type = "CUSTOM",
                        title = "",
                        templateText = ""
                    )
                },
                onDeleteTemplate = { viewModel.deleteWhatsAppTemplate(it) }
            )

            3 -> WhatsAppCampaignTab(
                clients = clients,
                templates = templates,
                onSaveCampaign = { viewModel.saveWhatsAppTemplate(it) },
                onDeleteCampaign = { viewModel.deleteWhatsAppTemplate(it) },
                onSendCampaign = { title, msgText, targetList ->
                    viewModel.sendPromotionalCampaign(title, msgText, targetList, context)
                }
            )

            4 -> WhatsAppLogsTab(logs = logs)

            5 -> WhatsAppAiConfigTabContent(
                currentConfig = aiConfig,
                onSave = { viewModel.saveAiMonitoringConfig(it) }
            )

            6 -> {
                val queueItems by viewModel.waitingQueueState.collectAsStateWithLifecycle()
                WaitingQueueTab(viewModel = viewModel, queueItems = queueItems, clients = clients)
            }

            7 -> {
                val subs by viewModel.vipSubscriptionsState.collectAsStateWithLifecycle()
                VipSubscriptionsTab(viewModel = viewModel, subscriptions = subs, clients = clients)
            }

            8 -> {
                VoiceAssistantTab(viewModel = viewModel)
            }
        }
    }

    // Dialogs
    if (showSimulateDialog) {
        WhatsAppMessageSimulatorDialog(
            isProcessing = isAiProcessing,
            onDismiss = { showSimulateDialog = false },
            onSimulatePreset = { presetIndex ->
                viewModel.simulateIncomingWhatsAppMessage(presetIndex, context)
                showSimulateDialog = false
            },
            onProcessCustom = { sender, phone, text ->
                viewModel.processIncomingWhatsAppMessage(
                    senderName = sender,
                    senderPhone = phone,
                    rawMessage = text,
                    context = context
                )
                showSimulateDialog = false
            }
        )
    }

    if (showAiConfigDialog) {
        AiConfigDialog(
            currentConfig = aiConfig,
            onDismiss = { showAiConfigDialog = false },
            onSave = { updated ->
                viewModel.saveAiMonitoringConfig(updated)
                showAiConfigDialog = false
            }
        )
    }

    if (templateToEdit != null) {
        WhatsAppTemplateEditDialog(
            template = templateToEdit!!,
            onDismiss = { templateToEdit = null },
            onSave = { updated ->
                viewModel.saveWhatsAppTemplate(updated)
                templateToEdit = null
            },
            onDelete = { toDelete ->
                viewModel.deleteWhatsAppTemplate(toDelete)
                templateToEdit = null
            }
        )
    }
}

// =========================================================================
// TAB 0: WHATSAPP AI MONITOR (MONITORAMENTO INTELIGENTE DE ENTRADA)
// =========================================================================

@Composable
fun WhatsAppAiMonitorTab(
    inboundMessages: List<WhatsAppInboundMessageEntity>,
    isAiProcessing: Boolean,
    onSimulateClick: () -> Unit,
    onApprove: (WhatsAppInboundMessageEntity) -> Unit,
    onReject: (WhatsAppInboundMessageEntity) -> Unit,
    onDelete: (WhatsAppInboundMessageEntity) -> Unit,
    onReplyClient: (WhatsAppInboundMessageEntity) -> Unit,
    onNotifyProfessional: (WhatsAppInboundMessageEntity) -> Unit,
    onUpdateSuggestedReply: (WhatsAppInboundMessageEntity, String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Quick Action Bar (Compact Header)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "Monitoramento em Tempo Real",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Captura e analisa automaticamente mensagens recebidas no WhatsApp",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = onSimulateClick,
                            modifier = Modifier.testTag("simulate_incoming_button"),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.AddComment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simular Mensagem", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (isAiProcessing) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "IA processando mensagem recebida...",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        if (inboundMessages.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.MarkChatUnread,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Nenhuma mensagem de entrada no momento.",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Clique em 'Simular Mensagem' para testar a captura automática com IA.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(inboundMessages, key = { it.id }) { message ->
                InboundMessageAiCard(
                    message = message,
                    onApprove = { onApprove(message) },
                    onReject = { onReject(message) },
                    onDelete = { onDelete(message) },
                    onReplyClient = { onReplyClient(message) },
                    onNotifyProfessional = { onNotifyProfessional(message) },
                    onUpdateSuggestedReply = { newReply -> onUpdateSuggestedReply(message, newReply) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InboundMessageAiCard(
    message: WhatsAppInboundMessageEntity,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onDelete: () -> Unit,
    onReplyClient: () -> Unit,
    onNotifyProfessional: () -> Unit,
    onUpdateSuggestedReply: (String) -> Unit
) {
    var isEditingReply by remember { mutableStateOf(false) }
    var editedReplyText by remember { mutableStateOf(message.suggestedReply) }

    LaunchedEffect(message.suggestedReply) {
        editedReplyText = message.suggestedReply
    }

    val timeFormatted = remember(message.timestamp) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    val statusColor = when (message.status) {
        "APROVADO", "AUTO_AGENDADO" -> Color(0xFF2E7D32)
        "RECUSADO" -> MaterialTheme.colorScheme.error
        else -> Color(0xFFE65100) // PENDENTE
    }

    val statusBg = when (message.status) {
        "APROVADO", "AUTO_AGENDADO" -> Color(0xFFE8F5E9)
        "RECUSADO" -> MaterialTheme.colorScheme.errorContainer
        else -> Color(0xFFFFF3E0)
    }

    val statusLabel = when (message.status) {
        "APROVADO" -> "✓ APROVADO & AGENDADO"
        "AUTO_AGENDADO" -> "✨ AUTO-AGENDADO POR IA"
        "RECUSADO" -> "✕ RECUSADO"
        else -> "⏳ PENDENTE DE APROVAÇÃO"
    }

    val intentLabel = when (message.detectedIntent) {
        "NEW_APPOINTMENT" -> "✨ NOVO AGENDAMENTO"
        "RESCHEDULE" -> "🔄 REAGENDAMENTO"
        "CANCELLATION" -> "❌ CANCELAMENTO"
        "INQUIRY" -> "❓ DÚVIDA / PREÇO"
        else -> "💬 MENSAGEM GERAL"
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inbound_card_${message.id}"),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header: Sender & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = message.senderName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = message.senderName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${message.senderPhone} • $timeFormatted",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Raw WhatsApp Message Balloon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFDCF8C6).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_whatsapp),
                        contentDescription = null,
                        tint = Color(0xFF075E54),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "\"${message.rawMessage}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF075E54),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Gemini AI Analysis Box
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Análise IA",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = intentLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                color = Color(0xFFE3F2FD),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${(message.confidenceScore * 100).toInt()}% Confiança",
                                    color = Color(0xFF1565C0),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Extracted Entities Grid
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Serviço Detectado:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = message.extractedServiceName.ifBlank { "Não especificado" },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Profissional Solicitada:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = message.extractedProfessionalName.ifBlank { "Equipe Geral" },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Data Solicitada:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = MaskUtils.formatDatePtBr(message.extractedDate),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Horário Solicitado:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = message.extractedTime.ifBlank { "--:--" },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (message.notes.isNotBlank()) {
                        Text(
                            text = "💡 Observação IA: ${message.notes}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Pre-Service Diagnosis Badge
                    val preServiceBadgeText = when (message.preServiceStage) {
                        "FALTA_SERVICO" -> "✂️ PRÉ-ATENDIMENTO: PERGUNTANDO SERVIÇO"
                        "FALTA_PROFISSIONAL" -> "👤 PRÉ-ATENDIMENTO: PERGUNTANDO PROFISSIONAL"
                        "FALTA_DATA_HORA" -> "📅 PRÉ-ATENDIMENTO: PERGUNTANDO DATA/HORA"
                        "INFORMACAO_INCOMPLETA" -> "👋 PRÉ-ATENDIMENTO: DADOS INCOMPLETOS / CONTATO INICIAL"
                        else -> "✅ PRÉ-ATENDIMENTO: DADOS COMPLETOS"
                    }

                    val preServiceBadgeBg = when (message.preServiceStage) {
                        "TUDO_INFORMADO" -> Color(0xFFE8F5E9)
                        else -> Color(0xFFFFF3E0)
                    }

                    val preServiceBadgeColor = when (message.preServiceStage) {
                        "TUDO_INFORMADO" -> Color(0xFF2E7D32)
                        else -> Color(0xFFE65100)
                    }

                    Surface(
                        color = preServiceBadgeBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = preServiceBadgeText,
                            color = preServiceBadgeColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }

                    if (message.suggestedReply.isNotBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                if (isEditingReply) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "✍️ Editando Resposta:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            IconButton(
                                                onClick = {
                                                    isEditingReply = false
                                                    editedReplyText = message.suggestedReply
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Cancelar",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    onUpdateSuggestedReply(editedReplyText)
                                                    isEditingReply = false
                                                },
                                                enabled = editedReplyText.isNotBlank(),
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = "Salvar",
                                                    tint = Color(0xFF2E7D32),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = editedReplyText,
                                        onValueChange = { editedReplyText = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = MaterialTheme.typography.bodySmall,
                                        maxLines = 5,
                                        minLines = 2
                                    )
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "💬 Resposta de Pré-Atendimento IA:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { isEditingReply = true },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Outlined.Edit,
                                                    contentDescription = "Editar Resposta",
                                                    tint = MaterialTheme.colorScheme.outline,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = onReplyClient,
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_whatsapp),
                                                    contentDescription = "Enviar no WhatsApp",
                                                    tint = Color(0xFF075E54),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = message.suggestedReply,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Action Buttons
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (message.status == "PENDENTE") {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.testTag("approve_booking_button_${message.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aprovar & Agendar", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, softWrap = false)
                    }

                    OutlinedButton(
                        onClick = onReplyClient,
                        modifier = Modifier.testTag("reply_client_button_${message.id}"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Responder", fontSize = 12.sp, maxLines = 1, softWrap = false)
                    }

                    IconButton(onClick = onReject, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Recusar", tint = MaterialTheme.colorScheme.error)
                    }
                } else {
                    OutlinedButton(
                        onClick = onNotifyProfessional,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_whatsapp),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Notificar Profissional no WhatsApp", fontSize = 12.sp, maxLines = 1, softWrap = false)
                    }

                    OutlinedButton(
                        onClick = onReplyClient,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Responder Cliente", fontSize = 12.sp, maxLines = 1, softWrap = false)
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 1: NOTIFICAÇÕES DA EQUIPE / PROFISSIONAIS
// =========================================================================

@Composable
fun ProfessionalNotificationsTab(
    notifications: List<ProfessionalNotificationEntity>,
    onMarkRead: (Long) -> Unit,
    onMarkAllRead: () -> Unit,
    onDelete: (ProfessionalNotificationEntity) -> Unit,
    onSendWhatsAppAlert: (ProfessionalNotificationEntity) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Notificações aos Profissionais",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Alertas sobre agendamentos de clientes via WhatsApp",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (notifications.any { !it.isRead }) {
                    TextButton(onClick = onMarkAllRead) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Marcar lidas")
                    }
                }
            }
        }

        if (notifications.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhuma notificação enviada à equipe ainda.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(notifications, key = { it.id }) { notif ->
                val timeFormatted = remember(notif.timestamp) {
                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(notif.timestamp))
                }

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMarkRead(notif.id) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (!notif.isRead) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = notif.professionalName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${notif.professionalPhone} • $timeFormatted",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            if (!notif.isRead) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text("Nova", modifier = Modifier.padding(2.dp))
                                }
                            }
                        }

                        Text(
                            text = notif.title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = notif.message,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { onSendWhatsAppAlert(notif) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                modifier = Modifier.testTag("send_whatsapp_alert_${notif.id}")
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_whatsapp),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Avisar no WhatsApp", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            IconButton(onClick = { onDelete(notif) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// SIMULATOR DIALOG & AI CONFIG DIALOG
// =========================================================================

@Composable
fun WhatsAppMessageSimulatorDialog(
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onSimulatePreset: (Int) -> Unit,
    onProcessCustom: (String, String, String) -> Unit
) {
    var customSender by remember { mutableStateOf("Mariana Silva") }
    var customPhone by remember { mutableStateOf("(11) 98765-4321") }
    var customMessage by remember { mutableStateOf("Olá! Gostaria de agendar um corte com a Beatriz amanhã às 14h, tem horário?") }

    val presets = listOf(
        "💇‍♀️ Mariana Silva: Corte Feminino com Beatriz amanhã 14:30",
        "💅 Camila Rodrigues: Manicure & Pedicure com Carla na sexta 10:00",
        "✨ Juliana Souza: Coloração com Beatriz no sábado 09:00",
        "👁️ Fernanda Lima: Valor da sobrancelha e vaga hoje às 16:00",
        "💆‍♀️ Renata Oliveira: Limpeza de pele urgente hoje às 15:00 com Dani",
        "❌ Aline Castro: Desmarcar/reagendar horário"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simulador de WhatsApp (IA)")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Selecione um cenário de teste real ou digite uma mensagem personalizada para a IA analisar:",
                    style = MaterialTheme.typography.bodySmall
                )

                Text("Cenários Pré-configurados:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    presets.forEachIndexed { index, text ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSimulatePreset(index) }
                        ) {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                HorizontalDivider()

                Text("Ou digite uma mensagem personalizada:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = customSender,
                    onValueChange = { customSender = it },
                    label = { Text("Nome do Cliente") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = customPhone,
                    onValueChange = { customPhone = it },
                    label = { Text("Telefone WhatsApp") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = customMessage,
                    onValueChange = { customMessage = it },
                    label = { Text("Texto da Mensagem") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onProcessCustom(customSender, customPhone, customMessage)
                },
                enabled = !isProcessing && customMessage.isNotBlank(),
                modifier = Modifier.testTag("submit_custom_message_button")
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Processar com IA")
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            ) {
                Text("Fechar")
            }
        }
    )
}

@Composable
fun AiConfigDialog(
    currentConfig: AiMonitoringConfigEntity,
    onDismiss: () -> Unit,
    onSave: (AiMonitoringConfigEntity) -> Unit
) {
    var isMonitoringActive by remember { mutableStateOf(currentConfig.isMonitoringActive) }
    var autoApproveHighConfidence by remember { mutableStateOf(currentConfig.autoApproveHighConfidence) }
    var notifyProfessionalWhatsApp by remember { mutableStateOf(currentConfig.notifyProfessionalWhatsApp) }
    var notifyAndroidSystem by remember { mutableStateOf(currentConfig.notifyAndroidSystem) }
    var smartAutoReply by remember { mutableStateOf(currentConfig.smartAutoReply) }
    var autoPreServiceEnabled by remember { mutableStateOf(currentConfig.autoPreServiceEnabled) }

    var msgMissingService by remember { mutableStateOf(currentConfig.msgMissingService) }
    var msgMissingProfessional by remember { mutableStateOf(currentConfig.msgMissingProfessional) }
    var msgMissingDateTime by remember { mutableStateOf(currentConfig.msgMissingDateTime) }
    var msgIncompleteInfo by remember { mutableStateOf(currentConfig.msgIncompleteInfo) }
    var msgCompleteConfirmation by remember { mutableStateOf(currentConfig.msgCompleteConfirmation) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SettingsSuggest, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Automação & Pré-Atendimento IA")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Fluxo do Monitor IA",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Monitoramento IA Ativo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Analisar mensagens recebidas automaticamente", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Switch(checked = isMonitoringActive, onCheckedChange = { isMonitoringActive = it })
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pré-Atendimento Automático", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Enviar mensagem ao cliente pedindo dados faltantes (Serviço, Profissional, Data/Hora)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Switch(checked = autoPreServiceEnabled, onCheckedChange = { autoPreServiceEnabled = it })
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Agendar (>90% Confiança)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Cria agendamento direto na agenda quando todas as informações estiverem ok", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Switch(checked = autoApproveHighConfidence, onCheckedChange = { autoApproveHighConfidence = it })
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Notificar Profissional no WhatsApp", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Prepara disparo com os detalhes para a profissional", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Switch(checked = notifyProfessionalWhatsApp, onCheckedChange = { notifyProfessionalWhatsApp = it })
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Modelos de Pré-Atendimento Personalizados",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "A IA seleciona a mensagem apropriada dependendo do que o cliente deixou de informar.\n\nVariáveis suportadas: {cliente}, {salão}, {servico}, {profissional}, {data}, {horario}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = msgIncompleteInfo,
                    onValueChange = { msgIncompleteInfo = it },
                    label = { Text("👋 Mensagem Inicial / Dados Incompletos") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = msgMissingService,
                    onValueChange = { msgMissingService = it },
                    label = { Text("✂️ Quando falta informar o Serviço") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = msgMissingProfessional,
                    onValueChange = { msgMissingProfessional = it },
                    label = { Text("👤 Quando falta informar a Profissional") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = msgMissingDateTime,
                    onValueChange = { msgMissingDateTime = it },
                    label = { Text("📅 Quando falta Data ou Horário") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = msgCompleteConfirmation,
                    onValueChange = { msgCompleteConfirmation = it },
                    label = { Text("✅ Confirmação de Agendamento Completo") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                TextButton(
                    onClick = {
                        val def = AiMonitoringConfigEntity()
                        msgMissingService = def.msgMissingService
                        msgMissingProfessional = def.msgMissingProfessional
                        msgMissingDateTime = def.msgMissingDateTime
                        msgIncompleteInfo = def.msgIncompleteInfo
                        msgCompleteConfirmation = def.msgCompleteConfirmation
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Restaurar Modelos Padrão")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        currentConfig.copy(
                            isMonitoringActive = isMonitoringActive,
                            autoApproveHighConfidence = autoApproveHighConfidence,
                            notifyProfessionalWhatsApp = notifyProfessionalWhatsApp,
                            notifyAndroidSystem = notifyAndroidSystem,
                            smartAutoReply = smartAutoReply,
                            autoPreServiceEnabled = autoPreServiceEnabled,
                            msgMissingService = msgMissingService,
                            msgMissingProfessional = msgMissingProfessional,
                            msgMissingDateTime = msgMissingDateTime,
                            msgIncompleteInfo = msgIncompleteInfo,
                            msgCompleteConfirmation = msgCompleteConfirmation
                        )
                    )
                }
            ) {
                Text("Salvar Configurações")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            ) {
                Text("Cancelar")
            }
        }
    )
}

// =========================================================================
// TAB 2, 3, 4: TEMPLATES, CAMPAIGNS, LOGS
// =========================================================================

@Composable
fun WhatsAppTemplatesTab(
    templates: List<WhatsAppTemplateEntity>,
    onEditTemplate: (WhatsAppTemplateEntity) -> Unit,
    onCreateNewTemplate: () -> Unit,
    onDeleteTemplate: (WhatsAppTemplateEntity) -> Unit
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
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Modelos de Mensagem", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Variáveis: {nome_cliente}, {data}, {horario}, {servico}, {nome_profissional}, {nome_salao}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = onCreateNewTemplate,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Novo Modelo", style = MaterialTheme.typography.labelSmall)
                        }
                    }
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
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text(
                                text = template.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val typeLabel = when (template.type) {
                                "CONFIRMATION" -> "Confirmação"
                                "REMINDER" -> "Lembrete"
                                "PROFESSIONAL_ALERT" -> "Alerta Profissional"
                                "PROMOTIONAL" -> "Promocional"
                                else -> "Personalizado"
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = typeLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row {
                            IconButton(onClick = { onEditTemplate(template) }) {
                                Icon(Icons.Outlined.Edit, contentDescription = "Editar Modelo")
                            }
                            IconButton(onClick = { onDeleteTemplate(template) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Excluir Modelo", tint = MaterialTheme.colorScheme.error)
                            }
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
    onSave: (WhatsAppTemplateEntity) -> Unit,
    onDelete: (WhatsAppTemplateEntity) -> Unit
) {
    var title by remember { mutableStateOf(template.title) }
    var templateText by remember { mutableStateOf(template.templateText) }
    var selectedType by remember { mutableStateOf(template.type) }

    val typeOptions = listOf(
        "CONFIRMATION" to "Confirmação",
        "REMINDER" to "Lembrete",
        "PROFESSIONAL_ALERT" to "Alerta Profissional",
        "PROMOTIONAL" to "Promocional",
        "CUSTOM" to "Personalizado"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (template.id == 0L) "Cadastrar Novo Modelo" else "Editar Modelo")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título / Nome do Modelo") },
                    placeholder = { Text("Ex: Lembrete de Retorno, Boas-Vindas...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Categoria do Modelo:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(typeOptions) { (key, label) ->
                        FilterChip(
                            selected = selectedType == key,
                            onClick = { selectedType = key },
                            label = { Text(label, fontSize = 12.sp, maxLines = 1, softWrap = false) }
                        )
                    }
                }

                OutlinedTextField(
                    value = templateText,
                    onValueChange = { templateText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    label = { Text("Texto da Mensagem") },
                    minLines = 3
                )

                Text(
                    text = "Variáveis suportadas: {nome_cliente}, {data}, {horario}, {servico}, {nome_profissional}, {nome_salao}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        template.copy(
                            title = title,
                            type = selectedType,
                            templateText = templateText
                        )
                    )
                },
                enabled = title.isNotBlank() && templateText.isNotBlank(),
                modifier = Modifier.testTag("save_template_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (template.id == 0L) "Cadastrar Modelo" else "Salvar Modelo")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (template.id != 0L) {
                    TextButton(
                        onClick = { onDelete(template) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Excluir")
                    }
                }
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Text("Cancelar")
                }
            }
        }
    )
}

@Composable
fun WhatsAppCampaignTab(
    clients: List<ClientEntity>,
    templates: List<WhatsAppTemplateEntity>,
    onSaveCampaign: (WhatsAppTemplateEntity) -> Unit,
    onDeleteCampaign: (WhatsAppTemplateEntity) -> Unit,
    onSendCampaign: (String, String, List<ClientEntity>) -> Unit
) {
    val promotionalTemplates = remember(templates) {
        templates.filter { it.type == "PROMOTIONAL" }
    }

    var selectedTemplateId by remember(promotionalTemplates) {
        mutableStateOf<Long?>(promotionalTemplates.firstOrNull()?.id)
    }
    var campaignTitle by remember(selectedTemplateId, promotionalTemplates) {
        val found = promotionalTemplates.find { it.id == selectedTemplateId }
        mutableStateOf(found?.title ?: "Nova Campanha Promocional")
    }
    var campaignMessage by remember(selectedTemplateId, promotionalTemplates) {
        val found = promotionalTemplates.find { it.id == selectedTemplateId }
        mutableStateOf(found?.templateText ?: "Olá {nome_cliente}! Temos uma novidade especial para você no {nome_salao}!")
    }

    var segmentFilter by remember { mutableStateOf("TODOS") } // ANIVERSARIANTES, TODOS
    var showCreateDialog by remember { mutableStateOf(false) }

    val targetClients = remember(clients, segmentFilter) {
        if (segmentFilter == "ANIVERSARIANTES") {
            clients.filter {
                val month = it.birthDate.split("-").getOrNull(1)?.toIntOrNull()
                month == 7 || month == 8 || month == 9
            }
        } else clients
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ElevatedCard(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Campanhas Cadastradas (${promotionalTemplates.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { showCreateDialog = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cadastrar Nova Campanha", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Text(
                    text = "Selecione uma das campanhas cadastradas ou crie novas versões personalizadas para disparo:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (promotionalTemplates.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(promotionalTemplates, key = { it.id }) { template ->
                            val isSelected = selectedTemplateId == template.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedTemplateId = template.id
                                    campaignTitle = template.title
                                    campaignMessage = template.templateText
                                },
                                label = { Text(template.title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, maxLines = 1, softWrap = false) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Nenhuma campanha cadastrada ainda. Clique no botão acima para cadastrar!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                HorizontalDivider()

                OutlinedTextField(
                    value = campaignTitle,
                    onValueChange = { campaignTitle = it },
                    label = { Text("Nome da Campanha Selecionada") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Segmentação do Público-Alvo:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = segmentFilter == "ANIVERSARIANTES",
                        onClick = { segmentFilter = "ANIVERSARIANTES" },
                        label = { Text("Aniversariantes (${targetClients.size})", maxLines = 1, softWrap = false) }
                    )
                    FilterChip(
                        selected = segmentFilter == "TODOS",
                        onClick = { segmentFilter = "TODOS" },
                        label = { Text("Todos os Clientes (${clients.size})", maxLines = 1, softWrap = false) }
                    )
                }

                OutlinedTextField(
                    value = campaignMessage,
                    onValueChange = { campaignMessage = it },
                    label = { Text("Texto da Mensagem Promocional") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    minLines = 3
                )

                Text(
                    text = "Variáveis suportadas: {nome_cliente}, {nome_salao}, {servico}, {nome_profissional}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentTemplate = promotionalTemplates.find { it.id == selectedTemplateId }
                    if (currentTemplate != null) {
                        TextButton(
                            onClick = {
                                onDeleteCampaign(currentTemplate)
                                selectedTemplateId = null
                                campaignTitle = ""
                                campaignMessage = ""
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Excluir")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    OutlinedButton(
                        onClick = {
                            val entity = WhatsAppTemplateEntity(
                                id = selectedTemplateId ?: 0L,
                                type = "PROMOTIONAL",
                                title = campaignTitle,
                                templateText = campaignMessage
                            )
                            onSaveCampaign(entity)
                        },
                        enabled = campaignTitle.isNotBlank() && campaignMessage.isNotBlank()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (selectedTemplateId != null) "Salvar Alterações" else "Cadastrar Campanha")
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🎯 Público Alvo Selecionado: ${targetClients.size} cliente(s)",
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

    if (showCreateDialog) {
        WhatsAppCreateCampaignDialog(
            onDismiss = { showCreateDialog = false },
            onSave = { newCampaign ->
                onSaveCampaign(newCampaign)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun WhatsAppCreateCampaignDialog(
    onDismiss: () -> Unit,
    onSave: (WhatsAppTemplateEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("Olá {nome_cliente}! Temos uma condição especial para você no {nome_salao}!") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cadastrar Nova Campanha")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Cadastre um novo modelo de campanha promocional para reutilizar e disparar aos seus clientes sempre que desejar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nome da Campanha (Ex: Black Friday, Dia das Mães, Retorno)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    label = { Text("Texto da Mensagem Promocional") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    minLines = 3
                )

                Text(
                    text = "Variáveis suportadas: {nome_cliente}, {nome_salao}, {servico}, {nome_profissional}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        WhatsAppTemplateEntity(
                            id = 0L,
                            type = "PROMOTIONAL",
                            title = title,
                            templateText = messageText
                        )
                    )
                },
                enabled = title.isNotBlank() && messageText.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cadastrar Campanha")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Cancelar")
            }
        }
    )
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

@Composable
fun WhatsAppAiConfigTabContent(
    currentConfig: AiMonitoringConfigEntity,
    onSave: (AiMonitoringConfigEntity) -> Unit
) {
    var isMonitoringActive by remember(currentConfig) { mutableStateOf(currentConfig.isMonitoringActive) }
    var autoApproveHighConfidence by remember(currentConfig) { mutableStateOf(currentConfig.autoApproveHighConfidence) }
    var notifyProfessionalWhatsApp by remember(currentConfig) { mutableStateOf(currentConfig.notifyProfessionalWhatsApp) }
    var autoPreServiceEnabled by remember(currentConfig) { mutableStateOf(currentConfig.autoPreServiceEnabled) }

    var msgMissingService by remember(currentConfig) { mutableStateOf(currentConfig.msgMissingService) }
    var msgMissingProfessional by remember(currentConfig) { mutableStateOf(currentConfig.msgMissingProfessional) }
    var msgMissingDateTime by remember(currentConfig) { mutableStateOf(currentConfig.msgMissingDateTime) }
    var msgIncompleteInfo by remember(currentConfig) { mutableStateOf(currentConfig.msgIncompleteInfo) }
    var msgCompleteConfirmation by remember(currentConfig) { mutableStateOf(currentConfig.msgCompleteConfirmation) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Configurações do Monitor IA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Monitoramento IA Ativo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Analisar mensagens recebidas automaticamente", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Switch(checked = isMonitoringActive, onCheckedChange = { isMonitoringActive = it })
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pré-Atendimento Automático", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Enviar mensagem ao cliente pedindo dados faltantes (Serviço, Profissional, Data/Hora)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Switch(checked = autoPreServiceEnabled, onCheckedChange = { autoPreServiceEnabled = it })
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Agendar (>90% Confiança)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Cria agendamento direto na agenda quando todas as informações estiverem ok", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Switch(checked = autoApproveHighConfidence, onCheckedChange = { autoApproveHighConfidence = it })
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Notificar Profissional no WhatsApp", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Prepara disparo com os detalhes para a profissional", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Switch(checked = notifyProfessionalWhatsApp, onCheckedChange = { notifyProfessionalWhatsApp = it })
                }
            }
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Modelos de Pré-Atendimento Personalizados",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "A IA seleciona a mensagem apropriada dependendo do que o cliente deixou de informar.\n\nVariáveis suportadas: {cliente}, {salão}, {servico}, {profissional}, {data}, {horario}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = msgIncompleteInfo,
                    onValueChange = { msgIncompleteInfo = it },
                    label = { Text("👋 Mensagem Inicial / Dados Incompletos") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = msgMissingService,
                    onValueChange = { msgMissingService = it },
                    label = { Text("✂️ Quando falta informar o Serviço") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = msgMissingProfessional,
                    onValueChange = { msgMissingProfessional = it },
                    label = { Text("👤 Quando falta informar a Profissional") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = msgMissingDateTime,
                    onValueChange = { msgMissingDateTime = it },
                    label = { Text("📅 Quando falta Data ou Horário") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = msgCompleteConfirmation,
                    onValueChange = { msgCompleteConfirmation = it },
                    label = { Text("✅ Confirmação de Agendamento Completo") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            val def = AiMonitoringConfigEntity()
                            msgMissingService = def.msgMissingService
                            msgMissingProfessional = def.msgMissingProfessional
                            msgMissingDateTime = def.msgMissingDateTime
                            msgIncompleteInfo = def.msgIncompleteInfo
                            msgCompleteConfirmation = def.msgCompleteConfirmation
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restaurar Padrão")
                    }

                    Button(
                        onClick = {
                            onSave(
                                currentConfig.copy(
                                    isMonitoringActive = isMonitoringActive,
                                    autoApproveHighConfidence = autoApproveHighConfidence,
                                    notifyProfessionalWhatsApp = notifyProfessionalWhatsApp,
                                    autoPreServiceEnabled = autoPreServiceEnabled,
                                    msgMissingService = msgMissingService,
                                    msgMissingProfessional = msgMissingProfessional,
                                    msgMissingDateTime = msgMissingDateTime,
                                    msgIncompleteInfo = msgIncompleteInfo,
                                    msgCompleteConfirmation = msgCompleteConfirmation
                                )
                            )
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salvar Configurações")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WaitingQueueTab(
    viewModel: SalonViewModel,
    queueItems: List<WaitingQueueEntity>,
    clients: List<ClientEntity>
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var serviceName by remember { mutableStateOf("Corte & Escova") }
    var professionalName by remember { mutableStateOf("Beatriz Oliveira") }
    var waitMinutes by remember { mutableStateOf("20") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    "Fila de Espera Dinâmica",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    "Notificações automáticas no WhatsApp",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Adicionar", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, softWrap = false)
            }
        }

        if (queueItems.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Nenhum cliente na fila de espera no momento.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            queueItems.forEach { item ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("${item.position}º", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                                Column {
                                    Text(item.clientName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("Tel: ${item.clientPhone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            AssistChip(
                                onClick = {},
                                label = { Text(item.status, fontSize = 11.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (item.status == "CHAMADO") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(end = 8.dp)) {
                                Text("Serviço: ${item.serviceName}", style = MaterialTheme.typography.bodySmall)
                                Text("Profissional: ${item.professionalName}", style = MaterialTheme.typography.bodySmall)
                                Text("Espera estimada: ~${item.estimatedWaitMinutes} min", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedButton(
                                    onClick = { viewModel.notifyQueueWhatsApp(context, item) },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Avisar WhatsApp", fontSize = 12.sp, maxLines = 1, softWrap = false)
                                }
                                Button(
                                    onClick = { viewModel.updateQueueStatus(item, if (item.status == "AGUARDANDO") "CHAMADO" else "ATENDIDO") },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(if (item.status == "AGUARDANDO") "Chamar" else "Concluir", fontSize = 12.sp, maxLines = 1, softWrap = false)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Adicionar Cliente à Fila de Espera") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Selecione o cliente e os detalhes do atendimento:")
                    OutlinedTextField(
                        value = serviceName,
                        onValueChange = { serviceName = it },
                        label = { Text("Serviço Desejado") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = professionalName,
                        onValueChange = { professionalName = it },
                        label = { Text("Profissional") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = waitMinutes,
                        onValueChange = { waitMinutes = it },
                        label = { Text("Tempo Estimado (min)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val c = clients.firstOrNull()
                        val cId = c?.id ?: 1L
                        val cName = c?.name ?: "Cliente Balcão"
                        val cPhone = c?.phone ?: "(11) 99999-9999"
                        viewModel.addWaitingQueueItem(cId, cName, cPhone, serviceName, professionalName, waitMinutes.toIntOrNull() ?: 20)
                        showAddDialog = false
                    }
                ) {
                    Text("Adicionar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun VipSubscriptionsTab(
    viewModel: SalonViewModel,
    subscriptions: List<VipSubscriptionEntity>,
    clients: List<ClientEntity>
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var planName by remember { mutableStateOf("Plano Escova Ouro") }
    var monthlyFee by remember { mutableStateOf("149.00") }
    var benefits by remember { mutableStateOf("2 escovas / mês, 1 hidratação") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Clube VIP & Assinaturas de Serviços", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Planos mensais com pagamento instantâneo via Pix", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Novo Plano VIP")
            }
        }

        if (subscriptions.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Nenhuma assinatura ativa no momento.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            subscriptions.forEach { sub ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(sub.planName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("Cliente: ${sub.clientName}", style = MaterialTheme.typography.bodyMedium)
                            }
                            AssistChip(
                                onClick = {},
                                label = { Text(sub.status, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (sub.status == "ATIVO") Color(0xFFE8F5E9) else Color(0xFFFFF8E1)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Benefícios: ${sub.remainingBenefits}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Valor Mensal: R$ %.2f".format(sub.monthlyFee), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("Próxima Renovação: ${sub.renewalDate}", style = MaterialTheme.typography.bodySmall)

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (sub.status == "PENDENTE_PIX") {
                                Button(
                                    onClick = { viewModel.simulatePixPaymentForSubscription(context, sub) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
                                ) {
                                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Simular Pagar PIX")
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { viewModel.cancelVipSubscription(sub) }
                                ) {
                                    Text("Cancelar Plano", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Criar Assinatura Clube VIP") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = planName,
                        onValueChange = { planName = it },
                        label = { Text("Nome do Plano") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = monthlyFee,
                        onValueChange = { monthlyFee = it },
                        label = { Text("Valor Mensal (R$)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = benefits,
                        onValueChange = { benefits = it },
                        label = { Text("Benefícios Inclusos") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val c = clients.firstOrNull()
                        val cId = c?.id ?: 1L
                        val cName = c?.name ?: "Cliente Balcão"
                        val cPhone = c?.phone ?: "(11) 99999-9999"
                        viewModel.addVipSubscription(cId, cName, cPhone, planName, monthlyFee.toDoubleOrNull() ?: 149.00, benefits)
                        showAddDialog = false
                    }
                ) {
                    Text("Gerar Assinatura & PIX")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun VoiceAssistantTab(
    viewModel: SalonViewModel
) {
    val context = LocalContext.current
    var voiceInputText by remember { mutableStateOf("") }
    var lastResponse by remember { mutableStateOf("Toque no microfone ou digite um comando de voz (Ex: 'Agendar corte amanhã às 14h', 'Verificar fila', 'Consultar clube vip').") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Comandos de Voz & Assistente", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Integração com Google Assistente para agendamentos e consultas em tempo real.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = voiceInputText,
            onValueChange = { voiceInputText = it },
            label = { Text("Simular Comando de Voz / Texto") },
            placeholder = { Text("Ex: Agendar escova com Beatriz amanhã") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (voiceInputText.isNotBlank()) {
                    viewModel.processVoiceCommand(context, voiceInputText) { resp ->
                        lastResponse = resp
                        voiceInputText = ""
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Processar Comando")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Resposta da IA / Assistente:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))
                Text(lastResponse, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
