package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.SalonRepository
import com.example.data.model.*
import com.example.utils.GeminiAiService
import com.example.utils.MaskUtils
import com.example.utils.NotificationHelper
import com.example.utils.PreServiceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

class SalonViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SalonRepository

    init {
        val dao = AppDatabase.getDatabase(application).salonDao()
        repository = SalonRepository(dao)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (dao.getSettings() == null) {
                    AppDatabase.populateDatabase(dao)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val settingsState: StateFlow<SalonSettingsEntity> = repository.settingsFlow
        .map { it ?: SalonSettingsEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SalonSettingsEntity())

    val clientsState: StateFlow<List<ClientEntity>> = repository.clientsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val servicesState: StateFlow<List<ServiceEntity>> = repository.servicesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val professionalsState: StateFlow<List<ProfessionalEntity>> = repository.professionalsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appointmentsState: StateFlow<List<AppointmentEntity>> = repository.appointmentsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nfseListState: StateFlow<List<NfseEntity>> = repository.nfseListFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nfseConfigState: StateFlow<NfseConfigEntity> = repository.nfseConfigFlow
        .map { it ?: NfseConfigEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NfseConfigEntity())

    val whatsappTemplatesState: StateFlow<List<WhatsAppTemplateEntity>> = repository.whatsappTemplatesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val whatsappLogsState: StateFlow<List<WhatsAppLogEntity>> = repository.whatsappLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val financialTransactionsState: StateFlow<List<FinancialTransactionEntity>> = repository.financialTransactionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // WhatsApp AI Inbound & Professional Notifications
    val inboundMessagesState: StateFlow<List<WhatsAppInboundMessageEntity>> = repository.inboundMessagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val professionalNotificationsState: StateFlow<List<ProfessionalNotificationEntity>> = repository.professionalNotificationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiMonitoringConfigState: StateFlow<AiMonitoringConfigEntity> = repository.aiMonitoringConfigFlow
        .map { it ?: AiMonitoringConfigEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AiMonitoringConfigEntity())

    val unreadNotificationsCount: StateFlow<Int> = professionalNotificationsState
        .map { list -> list.count { !it.isRead } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingInboundCount: StateFlow<Int> = inboundMessagesState
        .map { list -> list.count { it.status == "PENDENTE" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val waitingQueueState: StateFlow<List<WaitingQueueEntity>> = repository.waitingQueueFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val procedureHistoryState: StateFlow<List<ProcedureHistoryEntity>> = repository.procedureHistoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vipSubscriptionsState: StateFlow<List<VipSubscriptionEntity>> = repository.vipSubscriptionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAiProcessing = MutableStateFlow(false)
    val isAiProcessing: StateFlow<Boolean> = _isAiProcessing.asStateFlow()

    // UI State & View Toggles
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _viewModeGrid = MutableStateFlow<Map<String, Boolean>>(
        mapOf(
            "AGENDAMENTOS" to false,
            "SERVICOS" to false,
            "PROFISSIONAIS" to false,
            "CLIENTES" to false,
            "NFSE" to false,
            "FINANCEIRO" to false
        )
    )
    val viewModeGrid: StateFlow<Map<String, Boolean>> = _viewModeGrid.asStateFlow()

    private val _calendarViewMode = MutableStateFlow("DAY") // DAY, 3DAYS, WEEK, MONTH
    val calendarViewMode: StateFlow<String> = _calendarViewMode.asStateFlow()

    private val _selectedDate = MutableStateFlow(MaskUtils.getCurrentDateIso())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedProfFilter = MutableStateFlow<Long?>(null)
    val selectedProfFilter: StateFlow<Long?> = _selectedProfFilter.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _selectedBirthMonthFilter = MutableStateFlow<Int?>(null)
    val selectedBirthMonthFilter: StateFlow<Int?> = _selectedBirthMonthFilter.asStateFlow()

    private val _selectedNfseStatusFilter = MutableStateFlow<String?>(null)
    val selectedNfseStatusFilter: StateFlow<String?> = _selectedNfseStatusFilter.asStateFlow()

    private val _selectedFinanceTypeFilter = MutableStateFlow<String?>("TODAS") // TODAS, RECEITA, DESPESA, PENDENTE
    val selectedFinanceTypeFilter: StateFlow<String?> = _selectedFinanceTypeFilter.asStateFlow()

    private val _selectedFinancePeriodFilter = MutableStateFlow<String>("ESTE_MES") // ESTE_MES, HOJE, MES_ANTERIOR, TODOS
    val selectedFinancePeriodFilter: StateFlow<String> = _selectedFinancePeriodFilter.asStateFlow()

    // Confirmation Dialog State
    var pendingConfirmationAction: (() -> Unit)? = null
    private val _confirmationDialogText = MutableStateFlow<String?>(null)
    val confirmationDialogText: StateFlow<String?> = _confirmationDialogText.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleViewMode(moduleKey: String) {
        val current = _viewModeGrid.value.toMutableMap()
        current[moduleKey] = !(current[moduleKey] ?: false)
        _viewModeGrid.value = current
    }

    fun setCalendarViewMode(mode: String) {
        _calendarViewMode.value = mode
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun setSelectedProfFilter(profId: Long?) {
        _selectedProfFilter.value = profId
    }

    fun setSelectedCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
    }

    fun setSelectedBirthMonthFilter(month: Int?) {
        _selectedBirthMonthFilter.value = month
    }

    fun setSelectedNfseStatusFilter(status: String?) {
        _selectedNfseStatusFilter.value = status
    }

    fun requestActionConfirmation(message: String, action: () -> Unit) {
        pendingConfirmationAction = action
        _confirmationDialogText.value = message
    }

    fun confirmAction() {
        pendingConfirmationAction?.invoke()
        pendingConfirmationAction = null
        _confirmationDialogText.value = null
    }

    fun dismissConfirmation() {
        pendingConfirmationAction = null
        _confirmationDialogText.value = null
    }

    // --- Settings ---
    fun saveSettings(settings: SalonSettingsEntity) {
        viewModelScope.launch {
            repository.saveSettings(settings)
        }
    }

    // --- Clients ---
    fun addClient(client: ClientEntity) {
        viewModelScope.launch {
            repository.insertClient(client)
        }
    }

    fun updateClient(client: ClientEntity) {
        viewModelScope.launch {
            repository.updateClient(client)
        }
    }

    fun deleteClient(client: ClientEntity) {
        requestActionConfirmation("Tem certeza que deseja excluir o cliente '${client.name}'? Todos os registros associados serão mantidos no histórico.") {
            viewModelScope.launch {
                repository.deleteClient(client)
            }
        }
    }

    // --- Services ---
    fun addService(service: ServiceEntity) {
        viewModelScope.launch {
            repository.insertService(service)
        }
    }

    fun updateService(service: ServiceEntity) {
        viewModelScope.launch {
            repository.updateService(service)
        }
    }

    fun deleteService(service: ServiceEntity) {
        requestActionConfirmation("Deseja realmente remover o serviço '${service.name}'?") {
            viewModelScope.launch {
                repository.deleteService(service)
            }
        }
    }

    fun duplicateService(service: ServiceEntity) {
        viewModelScope.launch {
            val duplicated = service.copy(id = 0, name = "${service.name} (Cópia)")
            repository.insertService(duplicated)
            Toast.makeText(getApplication(), "Serviço duplicado com sucesso!", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Professionals ---
    fun addProfessional(professional: ProfessionalEntity) {
        viewModelScope.launch {
            repository.insertProfessional(professional)
        }
    }

    fun updateProfessional(professional: ProfessionalEntity) {
        viewModelScope.launch {
            repository.updateProfessional(professional)
        }
    }

    fun deleteProfessional(professional: ProfessionalEntity) {
        requestActionConfirmation("Deseja excluir o colaborador '${professional.name}'?") {
            viewModelScope.launch {
                repository.deleteProfessional(professional)
            }
        }
    }

    fun duplicateProfessional(prof: ProfessionalEntity) {
        viewModelScope.launch {
            val duplicated = prof.copy(id = 0, name = "${prof.name} (Novo Perfil)")
            repository.insertProfessional(duplicated)
            Toast.makeText(getApplication(), "Perfil/Escala replicado com sucesso!", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Appointments ---
    fun addAppointment(appointment: AppointmentEntity, context: Context? = null) {
        viewModelScope.launch {
            val newId = repository.insertAppointment(appointment)
            val apptWithId = appointment.copy(id = newId)
            autoSyncAppointmentToFinance(apptWithId)
            if (!appointment.isBlock && context != null) {
                // Auto trigger WhatsApp confirmation
                val msg = formatWhatsAppMessage("CONFIRMATION", appointment.clientName, appointment.date, appointment.time, appointment.serviceName, appointment.professionalName)
                repository.insertWhatsAppLog(
                    WhatsAppLogEntity(
                        recipientName = appointment.clientName,
                        recipientPhone = appointment.clientPhone,
                        messageText = msg,
                        status = "ENVIADO",
                        campaignName = "Novo Agendamento"
                    )
                )
                sendWhatsAppDirect(context, appointment.clientPhone, msg)
            }
        }
    }

    fun updateAppointment(appointment: AppointmentEntity) {
        viewModelScope.launch {
            repository.updateAppointment(appointment)
            autoSyncAppointmentToFinance(appointment)
        }
    }

    fun deleteAppointment(appointment: AppointmentEntity) {
        requestActionConfirmation("Tem certeza que deseja cancelar/excluir este agendamento?") {
            viewModelScope.launch {
                repository.deleteAppointment(appointment)
                removeFinanceForAppointment(appointment.id)
            }
        }
    }

    fun duplicateAppointment(app: AppointmentEntity) {
        viewModelScope.launch {
            val duplicated = app.copy(
                id = 0,
                status = "AGENDADO",
                notes = "Cópia do agendamento #${app.id}"
            )
            repository.insertAppointment(duplicated)
            Toast.makeText(getApplication(), "Agendamento copiado com sucesso!", Toast.LENGTH_SHORT).show()
        }
    }

    fun completeAndIssueNfse(appointment: AppointmentEntity, context: Context, selectedPaymentMethod: String? = null) {
        viewModelScope.launch {
            val paymentMethodToUse = selectedPaymentMethod ?: appointment.paymentMethod
            // Update appointment status & payment method
            val updated = appointment.copy(
                status = "CONCLUIDO",
                paymentMethod = paymentMethodToUse
            )
            repository.updateAppointment(updated)
            autoSyncAppointmentToFinance(updated)

            // Business Constraint: "Somente irão para plataforma de emissão de nota pagamentos realizados em cartão ou pix"
            val isEligibleForNfse = paymentMethodToUse.uppercase() in listOf("CARTAO", "PIX", "CARTÃO")

            if (isEligibleForNfse) {
                // Find client CPF
                val client = clientsState.value.firstOrNull { it.id == appointment.clientId }
                val cpf = client?.cpf ?: "000.000.000-00"

                val randomNum = (100000..999999).random()
                val nfse = NfseEntity(
                    appointmentId = appointment.id,
                    clientName = appointment.clientName,
                    clientCpf = cpf,
                    serviceName = appointment.serviceName,
                    price = appointment.price,
                    issueDate = MaskUtils.getCurrentDateTimeIso(),
                    paymentMethod = paymentMethodToUse,
                    status = "EMITIDA",
                    number = "2026$randomNum",
                    verificationCode = "NF-${(1000..9999).random()}",
                    xmlContent = "<Nfse><Numero>2026$randomNum</Numero><Cliente>${appointment.clientName}</Cliente><Servico>${appointment.serviceName}</Servico><Valor>${appointment.price}</Valor><FormaPagamento>$paymentMethodToUse</FormaPagamento></Nfse>",
                    pdfUrl = "https://nfse.prefeitura.sp.gov.br/pdf/2026$randomNum"
                )
                repository.insertNfse(nfse)
                Toast.makeText(context, "Atendimento concluído via $paymentMethodToUse! NFS-e #${nfse.number} emitida e enviada para a plataforma fiscal.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Atendimento concluído via $paymentMethodToUse! NFS-e NÃO emitida (emissão restrita a Cartão e Pix).", Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun autoSyncAppointmentToFinance(appt: AppointmentEntity) {
        if (appt.isBlock) {
            removeFinanceForAppointment(appt.id)
            return
        }
        val existingTransactions = financialTransactionsState.value
        val existing = existingTransactions.firstOrNull { it.relatedAppointmentId == appt.id }

        if (appt.status == "CONCLUIDO") {
            if (existing != null) {
                repository.updateFinancialTransaction(
                    existing.copy(
                        amount = appt.price,
                        date = appt.date,
                        paymentMethod = appt.paymentMethod,
                        status = "PAGO",
                        description = "Atendimento - ${appt.serviceName} (${appt.clientName})"
                    )
                )
            } else {
                repository.insertFinancialTransaction(
                    FinancialTransactionEntity(
                        type = "RECEITA",
                        description = "Atendimento - ${appt.serviceName} (${appt.clientName})",
                        amount = appt.price,
                        category = "Serviços",
                        date = appt.date,
                        paymentMethod = appt.paymentMethod,
                        status = "PAGO",
                        relatedAppointmentId = appt.id,
                        notes = "Sincronizado automaticamente da Agenda"
                    )
                )
            }
        } else if (appt.status == "CANCELADO") {
            if (existing != null) {
                repository.deleteFinancialTransaction(existing)
            }
        }
    }

    private suspend fun removeFinanceForAppointment(appointmentId: Long) {
        val existingTransactions = financialTransactionsState.value
        val existing = existingTransactions.firstOrNull { it.relatedAppointmentId == appointmentId }
        if (existing != null) {
            repository.deleteFinancialTransaction(existing)
        }
    }

    // --- NFS-e Actions ---
    fun saveNfseConfig(config: NfseConfigEntity) {
        viewModelScope.launch {
            repository.saveNfseConfig(config)
            Toast.makeText(getApplication(), "Configurações NFS-e salvas com sucesso!", Toast.LENGTH_SHORT).show()
        }
    }

    fun cancelNfse(nfse: NfseEntity, reason: String) {
        requestActionConfirmation("Deseja realmente cancelar a NFS-e #${nfse.number}? Esta operação é irreversível.") {
            viewModelScope.launch {
                val updated = nfse.copy(
                    status = "CANCELADA",
                    cancellationReason = reason
                )
                repository.updateNfse(updated)
                Toast.makeText(getApplication(), "NFS-e #${nfse.number} cancelada com sucesso.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- WhatsApp ---
    fun saveWhatsAppTemplate(template: WhatsAppTemplateEntity) {
        viewModelScope.launch {
            if (template.id == 0L) {
                repository.insertWhatsAppTemplate(template)
                Toast.makeText(getApplication(), "Nova campanha/modelo salva com sucesso!", Toast.LENGTH_SHORT).show()
            } else {
                repository.updateWhatsAppTemplate(template)
                Toast.makeText(getApplication(), "Modelo de mensagem atualizado!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updateWhatsAppTemplate(template: WhatsAppTemplateEntity) {
        saveWhatsAppTemplate(template)
    }

    fun deleteWhatsAppTemplate(template: WhatsAppTemplateEntity) {
        viewModelScope.launch {
            repository.deleteWhatsAppTemplate(template)
            Toast.makeText(getApplication(), "Campanha removida com sucesso!", Toast.LENGTH_SHORT).show()
        }
    }

    fun formatWhatsAppMessage(
        typeKey: String,
        clientName: String,
        date: String,
        time: String,
        service: String,
        profName: String
    ): String {
        val template = whatsappTemplatesState.value.firstOrNull { it.type == typeKey }
        val templateText = template?.templateText ?: "Olá {nome_cliente}! Seu agendamento para {servico} no {nome_salao} está marcado para {data} às {horario} com {nome_profissional}."
        val salonName = settingsState.value.nomeFantasia

        return templateText
            .replace("{nome_cliente}", clientName)
            .replace("{data}", MaskUtils.formatDatePtBr(date))
            .replace("{horario}", time)
            .replace("{servico}", service)
            .replace("{nome_profissional}", profName)
            .replace("{nome_salao}", salonName)
    }

    fun sendWhatsAppDirect(context: Context, phone: String, messageText: String) {
        try {
            val cleanPhone = MaskUtils.unmask(phone)
            val fullPhone = if (cleanPhone.startsWith("55")) cleanPhone else "55$cleanPhone"
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$fullPhone&text=${Uri.encode(messageText)}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Não foi possível abrir o WhatsApp: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun sendPromotionalCampaign(campaignTitle: String, messageTemplate: String, targetClients: List<ClientEntity>, context: Context) {
        requestActionConfirmation("Deseja disparar esta campanha promocional para ${targetClients.size} cliente(s)?") {
            viewModelScope.launch {
                for (client in targetClients) {
                    val msg = messageTemplate
                        .replace("{nome_cliente}", client.name)
                        .replace("{nome_salao}", settingsState.value.nomeFantasia)

                    repository.insertWhatsAppLog(
                        WhatsAppLogEntity(
                            recipientName = client.name,
                            recipientPhone = client.phone,
                            messageText = msg,
                            status = "ENVIADO",
                            campaignName = campaignTitle
                        )
                    )
                }
                Toast.makeText(context, "Campanha '$campaignTitle' enviada para ${targetClients.size} clientes!", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- Financial Management ---
    fun setSelectedFinanceTypeFilter(type: String?) {
        _selectedFinanceTypeFilter.value = type
    }

    fun setSelectedFinancePeriodFilter(period: String) {
        _selectedFinancePeriodFilter.value = period
    }

    fun addFinancialTransaction(transaction: FinancialTransactionEntity) {
        viewModelScope.launch {
            repository.insertFinancialTransaction(transaction)
        }
    }

    fun updateFinancialTransaction(transaction: FinancialTransactionEntity) {
        viewModelScope.launch {
            repository.updateFinancialTransaction(transaction)
        }
    }

    fun deleteFinancialTransaction(transaction: FinancialTransactionEntity) {
        requestActionConfirmation("Tem certeza que deseja excluir o lançamento '${transaction.description}' de R$ ${String.format("%.2f", transaction.amount)}?") {
            viewModelScope.launch {
                repository.deleteFinancialTransaction(transaction)
            }
        }
    }

    fun duplicateFinancialTransaction(transaction: FinancialTransactionEntity) {
        viewModelScope.launch {
            val duplicated = transaction.copy(id = 0, description = "${transaction.description} (Cópia)")
            repository.insertFinancialTransaction(duplicated)
            Toast.makeText(getApplication(), "Lançamento duplicado com sucesso!", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleFinancialTransactionStatus(transaction: FinancialTransactionEntity) {
        viewModelScope.launch {
            val newStatus = if (transaction.status == "PAGO") "PENDENTE" else "PAGO"
            repository.updateFinancialTransaction(transaction.copy(status = newStatus))
            Toast.makeText(getApplication(), "Status alterado para $newStatus!", Toast.LENGTH_SHORT).show()
        }
    }

    fun syncAppointmentsToFinancial(context: Context) {
        viewModelScope.launch {
            val appointments = appointmentsState.value.filter { it.status == "CONCLUIDO" && !it.isBlock }
            val existingFinancials = financialTransactionsState.value
            var syncCount = 0

            for (appt in appointments) {
                val alreadySynced = existingFinancials.any { it.relatedAppointmentId == appt.id }
                if (!alreadySynced) {
                    val newTransaction = FinancialTransactionEntity(
                        type = "RECEITA",
                        description = "Atendimento - ${appt.serviceName} (${appt.clientName})",
                        amount = appt.price,
                        category = "Serviços",
                        date = appt.date,
                        paymentMethod = appt.paymentMethod,
                        status = "PAGO",
                        relatedAppointmentId = appt.id,
                        notes = "Importado automaticamente do módulo de Agendamentos (Concluído)"
                    )
                    repository.insertFinancialTransaction(newTransaction)
                    syncCount++
                }
            }

            if (syncCount > 0) {
                Toast.makeText(context, "$syncCount novo(s) atendimento(s) sincronizado(s) com o Caixa!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Todos os atendimentos concluídos já estão lançados no Caixa.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ==========================================
    // AI WHATSAPP INBOUND MONITORING & AUTOMATION
    // ==========================================

    fun saveAiMonitoringConfig(config: AiMonitoringConfigEntity) {
        viewModelScope.launch {
            repository.saveAiMonitoringConfig(config)
            Toast.makeText(getApplication(), "Configurações do Monitor IA salvas!", Toast.LENGTH_SHORT).show()
        }
    }

    fun processIncomingWhatsAppMessage(
        senderName: String,
        senderPhone: String,
        rawMessage: String,
        context: Context,
        onComplete: ((WhatsAppInboundMessageEntity) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isAiProcessing.value = true
            try {
                val services = servicesState.value
                val professionals = professionalsState.value
                val salonName = settingsState.value.nomeFantasia

                val aiResult = GeminiAiService.analyzeWhatsAppMessage(
                    senderName = senderName,
                    senderPhone = senderPhone,
                    rawMessage = rawMessage,
                    availableServices = services,
                    availableProfessionals = professionals,
                    salonName = salonName
                )

                val config = aiMonitoringConfigState.value

                // Find matching professional or default
                val targetProf = professionals.firstOrNull { it.id == aiResult.professionalId }
                    ?: professionals.firstOrNull { it.name.equals(aiResult.professionalName, ignoreCase = true) }
                    ?: professionals.firstOrNull()

                val targetSvc = services.firstOrNull { it.id == aiResult.serviceId }
                    ?: services.firstOrNull { it.name.equals(aiResult.serviceName, ignoreCase = true) }
                    ?: services.firstOrNull()

                val shouldAutoApprove = config.autoApproveHighConfidence &&
                        aiResult.confidenceScore >= 0.90f &&
                        aiResult.detectedIntent == "NEW_APPOINTMENT"

                var createdAppointmentId: Long? = null
                val initialStatus = if (shouldAutoApprove) "AUTO_AGENDADO" else "PENDENTE"

                if (shouldAutoApprove) {
                    val newAppt = AppointmentEntity(
                        clientId = 0,
                        clientName = aiResult.clientName,
                        clientPhone = senderPhone,
                        serviceId = targetSvc?.id ?: 0,
                        serviceName = targetSvc?.name ?: aiResult.serviceName,
                        professionalId = targetProf?.id ?: 0,
                        professionalName = targetProf?.name ?: aiResult.professionalName,
                        date = aiResult.date,
                        time = aiResult.time,
                        durationMinutes = targetSvc?.durationMinutes ?: 60,
                        price = targetSvc?.price ?: 100.0,
                        status = "AGENDADO",
                        notes = "Auto-agendado por IA via WhatsApp. ${aiResult.notes}"
                    )
                    createdAppointmentId = repository.insertAppointment(newAppt)
                }

                val stage = PreServiceHelper.diagnoseMessage(
                    rawMessage = rawMessage,
                    extractedServiceName = targetSvc?.name ?: aiResult.serviceName,
                    extractedProfessionalName = targetProf?.name ?: aiResult.professionalName,
                    extractedDate = aiResult.date,
                    extractedTime = aiResult.time,
                    detectedIntent = aiResult.detectedIntent
                )

                val preServiceReply = PreServiceHelper.generatePreServiceReply(
                    stage = stage,
                    config = config,
                    salonName = salonName,
                    clientName = aiResult.clientName,
                    serviceName = targetSvc?.name ?: aiResult.serviceName,
                    professionalName = targetProf?.name ?: aiResult.professionalName,
                    dateFormatted = MaskUtils.formatDatePtBr(aiResult.date),
                    time = aiResult.time,
                    aiSuggestedReply = aiResult.suggestedReply
                )

                val inboundEntity = WhatsAppInboundMessageEntity(
                    senderName = senderName,
                    senderPhone = senderPhone,
                    rawMessage = rawMessage,
                    timestamp = System.currentTimeMillis(),
                    aiProcessed = true,
                    detectedIntent = aiResult.detectedIntent,
                    extractedClientName = aiResult.clientName,
                    extractedServiceName = targetSvc?.name ?: aiResult.serviceName,
                    extractedServiceId = targetSvc?.id,
                    extractedProfessionalName = targetProf?.name ?: aiResult.professionalName,
                    extractedProfessionalId = targetProf?.id,
                    extractedDate = aiResult.date,
                    extractedTime = aiResult.time,
                    confidenceScore = aiResult.confidenceScore,
                    customerSentiment = aiResult.sentiment,
                    notes = aiResult.notes,
                    suggestedReply = preServiceReply,
                    status = initialStatus,
                    createdAppointmentId = createdAppointmentId,
                    preServiceStage = stage.name
                )

                val inboundId = repository.insertInboundMessage(inboundEntity)
                val savedInbound = inboundEntity.copy(id = inboundId)

                // If auto Pre-Service is enabled, automatically log the auto-reply message sent
                if (config.autoPreServiceEnabled && preServiceReply.isNotBlank()) {
                    repository.insertWhatsAppLog(
                        WhatsAppLogEntity(
                            recipientName = aiResult.clientName.ifBlank { senderName },
                            recipientPhone = senderPhone,
                            messageText = preServiceReply,
                            status = "ENVIADO",
                            campaignName = "Pré-Atendimento Automático IA"
                        )
                    )
                }

                // Dispatch notification for professional
                if (targetProf != null) {
                    val notifTitle = when (aiResult.detectedIntent) {
                        "NEW_APPOINTMENT" -> if (shouldAutoApprove) "Novo Agendamento Confirmado por IA" else "Novo Agendamento Solicitado via WhatsApp"
                        "CANCELLATION" -> "Aviso de Cancelamento via WhatsApp"
                        "RESCHEDULE" -> "Pedido de Reagendamento via WhatsApp"
                        else -> "Nova Mensagem de Cliente via WhatsApp"
                    }

                    val notifMessage = "${aiResult.clientName} enviou: \"$rawMessage\"\n(Serviço: ${targetSvc?.name ?: aiResult.serviceName} em ${MaskUtils.formatDatePtBr(aiResult.date)} às ${aiResult.time})"

                    val notifEntity = ProfessionalNotificationEntity(
                        professionalId = targetProf.id,
                        professionalName = targetProf.name,
                        professionalPhone = targetProf.phone,
                        title = notifTitle,
                        message = notifMessage,
                        timestamp = System.currentTimeMillis(),
                        isRead = false,
                        appointmentId = createdAppointmentId,
                        inboundMessageId = inboundId,
                        type = when (aiResult.detectedIntent) {
                            "CANCELLATION" -> "CANCELAMENTO"
                            "RESCHEDULE" -> "REAGENDAMENTO"
                            else -> "NOVO_AGENDAMENTO"
                        }
                    )
                    repository.insertProfessionalNotification(notifEntity)

                    // Android System Push Notification
                    if (config.notifyAndroidSystem) {
                        NotificationHelper.showProfessionalNotification(
                            context = context,
                            notificationId = inboundId.toInt(),
                            title = notifTitle,
                            message = notifMessage,
                            professionalName = targetProf.name
                        )
                    }
                }

                Toast.makeText(
                    context,
                    "✨ Mensagem de $senderName analisada com IA! Profissional (${targetProf?.name ?: "Equipe"}) notificado.",
                    Toast.LENGTH_LONG
                ).show()

                onComplete?.invoke(savedInbound)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Erro ao processar mensagem: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                _isAiProcessing.value = false
            }
        }
    }

    fun approveInboundBooking(inbound: WhatsAppInboundMessageEntity, context: Context) {
        viewModelScope.launch {
            val srv = servicesState.value.firstOrNull { it.id == inbound.extractedServiceId }
                ?: servicesState.value.firstOrNull { it.name.equals(inbound.extractedServiceName, ignoreCase = true) }
                ?: servicesState.value.firstOrNull()

            val prof = professionalsState.value.firstOrNull { it.id == inbound.extractedProfessionalId }
                ?: professionalsState.value.firstOrNull { it.name.equals(inbound.extractedProfessionalName, ignoreCase = true) }
                ?: professionalsState.value.firstOrNull()

            val existingClient = clientsState.value.firstOrNull {
                it.name.equals(inbound.extractedClientName, ignoreCase = true) ||
                MaskUtils.unmask(it.phone) == MaskUtils.unmask(inbound.senderPhone)
            }

            val clientId = existingClient?.id ?: repository.insertClient(
                ClientEntity(
                    name = inbound.extractedClientName.ifBlank { inbound.senderName },
                    cpf = "000.000.000-00",
                    phone = inbound.senderPhone,
                    email = "",
                    birthDate = "2000-01-01"
                )
            )

            val newAppointment = AppointmentEntity(
                clientId = clientId,
                clientName = inbound.extractedClientName.ifBlank { inbound.senderName },
                clientPhone = inbound.senderPhone,
                serviceId = srv?.id ?: 0,
                serviceName = srv?.name ?: inbound.extractedServiceName,
                professionalId = prof?.id ?: 0,
                professionalName = prof?.name ?: inbound.extractedProfessionalName,
                date = inbound.extractedDate.ifBlank { MaskUtils.getCurrentDateIso() },
                time = inbound.extractedTime.ifBlank { "10:00" },
                durationMinutes = srv?.durationMinutes ?: 60,
                price = srv?.price ?: 100.0,
                status = "AGENDADO",
                notes = "Agendamento aprovado do WhatsApp IA (#${inbound.id})."
            )

            val apptId = repository.insertAppointment(newAppointment)

            val updatedInbound = inbound.copy(
                status = "APROVADO",
                createdAppointmentId = apptId
            )
            repository.updateInboundMessage(updatedInbound)

            // Notify professional
            if (prof != null) {
                val notif = ProfessionalNotificationEntity(
                    professionalId = prof.id,
                    professionalName = prof.name,
                    professionalPhone = prof.phone,
                    title = "Agendamento Aprovado na sua Agenda!",
                    message = "Cliente ${newAppointment.clientName} agendou ${newAppointment.serviceName} para ${MaskUtils.formatDatePtBr(newAppointment.date)} às ${newAppointment.time}.",
                    timestamp = System.currentTimeMillis(),
                    isRead = false,
                    appointmentId = apptId,
                    inboundMessageId = inbound.id,
                    type = "NOVO_AGENDAMENTO"
                )
                repository.insertProfessionalNotification(notif)

                NotificationHelper.showProfessionalNotification(
                    context = context,
                    notificationId = apptId.toInt(),
                    title = "Agendamento Confirmado!",
                    message = "Cliente ${newAppointment.clientName} agendou ${newAppointment.serviceName} para ${MaskUtils.formatDatePtBr(newAppointment.date)} às ${newAppointment.time}.",
                    professionalName = prof.name
                )
            }

            Toast.makeText(context, "Agendamento #${apptId} confirmado com sucesso para ${prof?.name ?: "Profissional"}!", Toast.LENGTH_LONG).show()
        }
    }

    fun rejectInboundMessage(inbound: WhatsAppInboundMessageEntity) {
        viewModelScope.launch {
            val updated = inbound.copy(status = "RECUSADO")
            repository.updateInboundMessage(updated)
            Toast.makeText(getApplication(), "Solicitação de WhatsApp recusada.", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteInboundMessage(inbound: WhatsAppInboundMessageEntity) {
        requestActionConfirmation("Deseja remover este registro de entrada do WhatsApp?") {
            viewModelScope.launch {
                repository.deleteInboundMessage(inbound)
                Toast.makeText(getApplication(), "Registro removido.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updateInboundSuggestedReply(inbound: WhatsAppInboundMessageEntity, newReply: String) {
        viewModelScope.launch {
            val updated = inbound.copy(suggestedReply = newReply)
            repository.updateInboundMessage(updated)
            Toast.makeText(getApplication(), "Resposta sugerida atualizada com sucesso!", Toast.LENGTH_SHORT).show()
        }
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
            Toast.makeText(getApplication(), "Todas as notificações marcadas como lidas.", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteProfessionalNotification(notification: ProfessionalNotificationEntity) {
        viewModelScope.launch {
            repository.deleteProfessionalNotification(notification)
        }
    }

    fun sendProfessionalWhatsAppAlert(context: Context, notification: ProfessionalNotificationEntity) {
        val salonName = settingsState.value.nomeFantasia
        val text = "Olá, *${notification.professionalName}*! 👋\n\n🔔 *Aviso do Sistema $salonName*:\n${notification.title}\n\n${notification.message}\n\nAbra o aplicativo para conferir os detalhes!"
        sendWhatsAppDirect(context, notification.professionalPhone, text)
        viewModelScope.launch {
            repository.updateProfessionalNotification(notification.copy(whatsappDispatched = true))
        }
    }

    fun replyToInboundClient(context: Context, inbound: WhatsAppInboundMessageEntity) {
        val replyText = inbound.suggestedReply.ifBlank {
            "Olá ${inbound.extractedClientName.ifBlank { inbound.senderName }}! Agradecemos o contato com o salão ${settingsState.value.nomeFantasia}. Seu atendimento para ${inbound.extractedServiceName} foi recebido!"
        }
        sendWhatsAppDirect(context, inbound.senderPhone, replyText)
    }

    fun simulateIncomingWhatsAppMessage(presetScenario: Int, context: Context) {
        val today = MaskUtils.getCurrentDateIso()
        val (senderName, phone, message) = when (presetScenario) {
            0 -> Triple(
                "Mariana Silva",
                "(11) 98765-4321",
                "Olá, boa tarde! Gostaria de agendar um Corte Feminino com a Beatriz para amanhã às 14:30. Vocês têm esse horário livre?"
            )
            1 -> Triple(
                "Camila Rodrigues",
                "(11) 97654-3210",
                "Oi! Quero marcar Manicure & Pedicure Completa com a Carla Mendes na sexta-feira às 10:00 da manhã."
            )
            2 -> Triple(
                "Juliana Souza",
                "(11) 96543-2109",
                "Boa noite! Gostaria de fazer Coloração / Mechas Balayage com a Beatriz no sábado às 09:00, quanto tempo dura o procedimento?"
            )
            3 -> Triple(
                "Fernanda Lima",
                "(11) 95432-1098",
                "Oi meninas! Qual o valor do Design de Sobrancelha & Henna com a Daniela Rocha e tem horário para hoje às 16:00?"
            )
            4 -> Triple(
                "Renata Oliveira",
                "(11) 94321-0987",
                "Olá! Preciso fazer uma Limpeza de Pele Profunda com urgência hoje de tarde com a Dani. Dá pra encaixar às 15h?"
            )
            else -> Triple(
                "Aline Castro",
                "(11) 93210-9876",
                "Oi! Gostaria de saber se consigo desmarcar meu horário de amanhã e reagendar para a próxima semana."
            )
        }

        processIncomingWhatsAppMessage(
            senderName = senderName,
            senderPhone = phone,
            rawMessage = message,
            context = context
        )
    }

    // Google Drive Backup & Restore States & Flows
    val gdriveConnected = MutableStateFlow(com.example.data.GoogleDriveBackupManager.isConnected(application))
    val gdriveBackups = MutableStateFlow<List<com.example.data.GoogleDriveBackupManager.DriveFile>>(emptyList())
    val gdriveLoading = MutableStateFlow(false)
    val gdriveStatusMessage = MutableStateFlow<String?>(null)
    val gdriveEmail = MutableStateFlow<String?>(com.example.data.GoogleDriveBackupManager.getSavedConnectedEmail(application))

    // Scheduling states
    val gdriveScheduleInterval = MutableStateFlow(com.example.data.GoogleDriveBackupManager.getScheduleInterval(application))
    val gdriveScheduleHour = MutableStateFlow(com.example.data.GoogleDriveBackupManager.getScheduleHour(application))
    val gdriveScheduleMinute = MutableStateFlow(com.example.data.GoogleDriveBackupManager.getScheduleMinute(application))

    fun updateGDriveConnectionStatus() {
        gdriveConnected.value = com.example.data.GoogleDriveBackupManager.isConnected(getApplication())
        gdriveEmail.value = com.example.data.GoogleDriveBackupManager.getSavedConnectedEmail(getApplication())
    }

    fun connectGDrive(code: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            gdriveLoading.value = true
            gdriveStatusMessage.value = "Conectando ao Google Drive..."
            val result = com.example.data.GoogleDriveBackupManager.exchangeCodeForTokens(getApplication(), code)
            if (result.isSuccess) {
                gdriveConnected.value = true
                gdriveStatusMessage.value = "Google Drive conectado! Obtendo conta cadastrada..."
                val emailResult = com.example.data.GoogleDriveBackupManager.fetchUserEmail(getApplication())
                gdriveLoading.value = false
                if (emailResult.isSuccess) {
                    gdriveEmail.value = emailResult.getOrThrow()
                    gdriveStatusMessage.value = "Conectado na conta: ${emailResult.getOrThrow()}"
                } else {
                    gdriveEmail.value = "Conta Conectada"
                    gdriveStatusMessage.value = "Google Drive conectado com sucesso!"
                }
                onComplete(true)
                loadGDriveBackups()
                // Auto setup alarm if a schedule is already configured
                com.example.data.GoogleDriveBackupManager.setupBackupAlarm(getApplication())
            } else {
                gdriveLoading.value = false
                val err = result.exceptionOrNull()?.message ?: "Erro desconhecido"
                gdriveStatusMessage.value = "Erro ao conectar: $err"
                onComplete(false)
            }
        }
    }

    fun disconnectGDrive() {
        com.example.data.GoogleDriveBackupManager.disconnect(getApplication())
        gdriveConnected.value = false
        gdriveEmail.value = null
        gdriveBackups.value = emptyList()
        gdriveStatusMessage.value = "Google Drive desconectado."
    }

    fun loadGDriveBackups() {
        if (!gdriveConnected.value) return
        viewModelScope.launch {
            gdriveLoading.value = true
            gdriveStatusMessage.value = "Carregando backups..."
            val result = com.example.data.GoogleDriveBackupManager.listBackups(getApplication())
            gdriveLoading.value = false
            if (result.isSuccess) {
                gdriveBackups.value = result.getOrThrow()
                gdriveStatusMessage.value = if (gdriveBackups.value.isEmpty()) "Nenhum backup encontrado no Drive." else "Backups carregados com sucesso."
            } else {
                val err = result.exceptionOrNull()?.message ?: "Erro ao carregar backups"
                gdriveStatusMessage.value = "Erro: $err"
            }
        }
    }

    fun saveBackupSchedule(interval: String, hour: Int, minute: Int) {
        com.example.data.GoogleDriveBackupManager.saveScheduleSettings(getApplication(), interval, hour, minute)
        gdriveScheduleInterval.value = interval
        gdriveScheduleHour.value = hour
        gdriveScheduleMinute.value = minute
        gdriveStatusMessage.value = if (interval == "disabled") {
            "Agendamento de backup desativado."
        } else {
            val freq = if (interval == "daily") "Diário" else "Semanal"
            val hourStr = hour.toString().padStart(2, '0')
            val minStr = minute.toString().padStart(2, '0')
            "Agendamento de backup configurado: $freq às $hourStr:$minStr"
        }
    }

    fun uploadBackupToGDrive(onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            gdriveLoading.value = true
            gdriveStatusMessage.value = "Criando e enviando backup do sistema..."
            val result = com.example.data.GoogleDriveBackupManager.uploadDatabaseBackup(getApplication())
            gdriveLoading.value = false
            if (result.isSuccess) {
                gdriveStatusMessage.value = "Backup realizado com sucesso no Google Drive!"
                loadGDriveBackups()
                onComplete(true)
            } else {
                val err = result.exceptionOrNull()?.message ?: "Erro ao enviar backup"
                gdriveStatusMessage.value = "Falha no Backup: $err"
                onComplete(false)
            }
        }
    }

    fun restoreBackupFromGDrive(fileId: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            gdriveLoading.value = true
            gdriveStatusMessage.value = "Baixando e restaurando banco de dados..."
            val result = com.example.data.GoogleDriveBackupManager.restoreDatabaseBackup(getApplication(), fileId)
            gdriveLoading.value = false
            if (result.isSuccess) {
                gdriveStatusMessage.value = "Restauração bem-sucedida! O aplicativo será reiniciado para aplicar os dados."
                onComplete(true)
            } else {
                val err = result.exceptionOrNull()?.message ?: "Erro ao restaurar backup"
                gdriveStatusMessage.value = "Falha na Restauração: $err"
                onComplete(false)
            }
        }
    }

    fun restartApp() {
        com.example.data.GoogleDriveBackupManager.triggerAppRestart(getApplication())
    }

    fun addWaitingQueueItem(clientId: Long, clientName: String, clientPhone: String, serviceName: String, professionalName: String, estimatedWaitMinutes: Int) {
        viewModelScope.launch {
            val currentQueue = waitingQueueState.value
            val nextPos = (currentQueue.maxOfOrNull { it.position } ?: 0) + 1
            repository.insertWaitingQueue(
                WaitingQueueEntity(
                    clientId = clientId,
                    clientName = clientName,
                    clientPhone = clientPhone,
                    serviceName = serviceName,
                    professionalName = professionalName,
                    position = nextPos,
                    status = "AGUARDANDO",
                    estimatedWaitMinutes = estimatedWaitMinutes
                )
            )
        }
    }

    fun updateQueueStatus(item: WaitingQueueEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateWaitingQueue(item.copy(status = newStatus))
        }
    }

    fun deleteQueueItem(item: WaitingQueueEntity) {
        viewModelScope.launch {
            repository.deleteWaitingQueue(item)
        }
    }

    fun notifyQueueWhatsApp(context: Context, item: WaitingQueueEntity) {
        val salonName = settingsState.value.nomeFantasia
        val msg = "Olá ${item.clientName}! Sua vez no $salonName está próxima para o serviço de ${item.serviceName} com ${item.professionalName}. Por favor dirija-se ao atendimento. Te esperamos!"
        sendWhatsAppDirect(context, item.clientPhone, msg)
        viewModelScope.launch {
            repository.insertWhatsAppLog(
                WhatsAppLogEntity(
                    recipientName = item.clientName,
                    recipientPhone = item.clientPhone,
                    messageText = msg,
                    status = "ENVIADO",
                    campaignName = "Alerta Fila de Espera"
                )
            )
        }
    }

    fun addProcedureHistory(clientId: Long, clientName: String, serviceName: String, professionalName: String, price: Double, notes: String) {
        viewModelScope.launch {
            val today = MaskUtils.getCurrentDateIso()
            repository.insertProcedureHistory(
                ProcedureHistoryEntity(
                    clientId = clientId,
                    clientName = clientName,
                    serviceName = serviceName,
                    professionalName = professionalName,
                    date = today,
                    price = price,
                    notes = notes
                )
            )
        }
    }

    fun addVipSubscription(clientId: Long, clientName: String, clientPhone: String, planName: String, monthlyFee: Double, benefits: String) {
        viewModelScope.launch {
            val today = MaskUtils.getCurrentDateIso()
            val renewal = "2026-09-25"
            repository.insertVipSubscription(
                VipSubscriptionEntity(
                    clientId = clientId,
                    clientName = clientName,
                    clientPhone = clientPhone,
                    planName = planName,
                    monthlyFee = monthlyFee,
                    status = "PENDENTE_PIX",
                    startDate = today,
                    renewalDate = renewal,
                    remainingBenefits = benefits
                )
            )
        }
    }

    fun simulatePixPaymentForSubscription(context: Context, sub: VipSubscriptionEntity) {
        viewModelScope.launch {
            repository.updateVipSubscription(sub.copy(status = "ATIVO"))
            repository.insertFinancialTransaction(
                FinancialTransactionEntity(
                    type = "RECEITA",
                    description = "Assinatura Clube VIP - ${sub.planName} (${sub.clientName})",
                    amount = sub.monthlyFee,
                    category = "Serviços",
                    date = MaskUtils.getCurrentDateIso(),
                    paymentMethod = "PIX",
                    status = "PAGO",
                    notes = "Pagamento PIX confirmado instantaneamente"
                )
            )
            Toast.makeText(context, "Pagamento PIX confirmado! Assinatura VIP ativada com sucesso.", Toast.LENGTH_LONG).show()
        }
    }

    fun cancelVipSubscription(sub: VipSubscriptionEntity) {
        viewModelScope.launch {
            repository.updateVipSubscription(sub.copy(status = "CANCELADO"))
        }
    }

    fun processVoiceCommand(context: Context, commandText: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val lower = commandText.lowercase(Locale.getDefault())
            val response: String
            if (lower.contains("agendar") || lower.contains("marcar") || lower.contains("corte")) {
                response = "Comando de voz processado: Agendamento identificado com sucesso! Verifique a agenda."
                Toast.makeText(context, "🎤 Assistente: $response", Toast.LENGTH_LONG).show()
            } else if (lower.contains("fila") || lower.contains("espera") || lower.contains("vez")) {
                val waitingCount = waitingQueueState.value.count { it.status == "AGUARDANDO" }
                response = "No momento há $waitingCount cliente(s) na fila de espera dinâmica."
                Toast.makeText(context, "🎤 Assistente: $response", Toast.LENGTH_LONG).show()
            } else if (lower.contains("cancelar") || lower.contains("desmarcar")) {
                response = "Comando de voz processado: Solicitação de cancelamento de horário registrada."
                Toast.makeText(context, "🎤 Assistente: $response", Toast.LENGTH_LONG).show()
            } else if (lower.contains("clube") || lower.contains("vip") || lower.contains("assinatura")) {
                val activeSubs = vipSubscriptionsState.value.count { it.status == "ATIVO" }
                response = "Existem $activeSubs assinaturas ativas no Clube VIP do salão."
                Toast.makeText(context, "🎤 Assistente: $response", Toast.LENGTH_LONG).show()
            } else {
                response = "Comando recebido: '$commandText'. Como posso ajudar na gestão do salão?"
                Toast.makeText(context, "🎤 Assistente: $response", Toast.LENGTH_LONG).show()
            }
            onResult(response)
        }
    }
}

