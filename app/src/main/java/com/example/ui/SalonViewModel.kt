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
import com.example.utils.MaskUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SalonViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SalonRepository

    init {
        val dao = AppDatabase.getDatabase(application).salonDao()
        repository = SalonRepository(dao)
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
        }
    }

    fun deleteAppointment(appointment: AppointmentEntity) {
        requestActionConfirmation("Tem certeza que deseja cancelar/excluir este agendamento?") {
            viewModelScope.launch {
                repository.deleteAppointment(appointment)
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
    fun updateWhatsAppTemplate(template: WhatsAppTemplateEntity) {
        viewModelScope.launch {
            repository.updateWhatsAppTemplate(template)
            Toast.makeText(getApplication(), "Modelo de mensagem atualizado!", Toast.LENGTH_SHORT).show()
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
}
