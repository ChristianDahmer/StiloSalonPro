package com.example.data

import com.example.data.dao.SalonDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class SalonRepository(private val dao: SalonDao) {

    // Settings
    val settingsFlow: Flow<SalonSettingsEntity?> = dao.getSettingsFlow()
    suspend fun getSettings(): SalonSettingsEntity? = dao.getSettings()
    suspend fun saveSettings(settings: SalonSettingsEntity) = dao.insertOrUpdateSettings(settings)

    // Clients
    val clientsFlow: Flow<List<ClientEntity>> = dao.getAllClientsFlow()
    suspend fun insertClient(client: ClientEntity): Long = dao.insertClient(client)
    suspend fun updateClient(client: ClientEntity) = dao.updateClient(client)
    suspend fun deleteClient(client: ClientEntity) = dao.deleteClient(client)

    // Services
    val servicesFlow: Flow<List<ServiceEntity>> = dao.getAllServicesFlow()
    suspend fun insertService(service: ServiceEntity): Long = dao.insertService(service)
    suspend fun updateService(service: ServiceEntity) = dao.updateService(service)
    suspend fun deleteService(service: ServiceEntity) = dao.deleteService(service)

    // Professionals
    val professionalsFlow: Flow<List<ProfessionalEntity>> = dao.getAllProfessionalsFlow()
    suspend fun insertProfessional(professional: ProfessionalEntity): Long = dao.insertProfessional(professional)
    suspend fun updateProfessional(professional: ProfessionalEntity) = dao.updateProfessional(professional)
    suspend fun deleteProfessional(professional: ProfessionalEntity) = dao.deleteProfessional(professional)

    // Appointments
    val appointmentsFlow: Flow<List<AppointmentEntity>> = dao.getAllAppointmentsFlow()
    suspend fun insertAppointment(appointment: AppointmentEntity): Long = dao.insertAppointment(appointment)
    suspend fun updateAppointment(appointment: AppointmentEntity) = dao.updateAppointment(appointment)
    suspend fun deleteAppointment(appointment: AppointmentEntity) = dao.deleteAppointment(appointment)

    // NFS-e
    val nfseListFlow: Flow<List<NfseEntity>> = dao.getAllNfseFlow()
    val nfseConfigFlow: Flow<NfseConfigEntity?> = dao.getNfseConfigFlow()
    suspend fun insertNfse(nfse: NfseEntity): Long = dao.insertNfse(nfse)
    suspend fun updateNfse(nfse: NfseEntity) = dao.updateNfse(nfse)
    suspend fun saveNfseConfig(config: NfseConfigEntity) = dao.insertOrUpdateNfseConfig(config)

    // WhatsApp
    val whatsappTemplatesFlow: Flow<List<WhatsAppTemplateEntity>> = dao.getAllWhatsAppTemplatesFlow()
    val whatsappLogsFlow: Flow<List<WhatsAppLogEntity>> = dao.getAllWhatsAppLogsFlow()
    suspend fun insertWhatsAppTemplate(template: WhatsAppTemplateEntity): Long = dao.insertWhatsAppTemplate(template)
    suspend fun updateWhatsAppTemplate(template: WhatsAppTemplateEntity) = dao.updateWhatsAppTemplate(template)
    suspend fun deleteWhatsAppTemplate(template: WhatsAppTemplateEntity) = dao.deleteWhatsAppTemplate(template)
    suspend fun insertWhatsAppLog(log: WhatsAppLogEntity): Long = dao.insertWhatsAppLog(log)

    // Financial Transactions
    val financialTransactionsFlow: Flow<List<FinancialTransactionEntity>> = dao.getAllFinancialTransactionsFlow()
    suspend fun insertFinancialTransaction(transaction: FinancialTransactionEntity): Long = dao.insertFinancialTransaction(transaction)
    suspend fun updateFinancialTransaction(transaction: FinancialTransactionEntity) = dao.updateFinancialTransaction(transaction)
    suspend fun deleteFinancialTransaction(transaction: FinancialTransactionEntity) = dao.deleteFinancialTransaction(transaction)

    // WhatsApp Inbound (AI Monitor)
    val inboundMessagesFlow: Flow<List<WhatsAppInboundMessageEntity>> = dao.getAllInboundMessagesFlow()
    suspend fun insertInboundMessage(message: WhatsAppInboundMessageEntity): Long = dao.insertInboundMessage(message)
    suspend fun updateInboundMessage(message: WhatsAppInboundMessageEntity) = dao.updateInboundMessage(message)
    suspend fun deleteInboundMessage(message: WhatsAppInboundMessageEntity) = dao.deleteInboundMessage(message)

    // Professional Notifications
    val professionalNotificationsFlow: Flow<List<ProfessionalNotificationEntity>> = dao.getAllProfessionalNotificationsFlow()
    suspend fun insertProfessionalNotification(notification: ProfessionalNotificationEntity): Long = dao.insertProfessionalNotification(notification)
    suspend fun updateProfessionalNotification(notification: ProfessionalNotificationEntity) = dao.updateProfessionalNotification(notification)
    suspend fun markNotificationAsRead(id: Long) = dao.markNotificationAsRead(id)
    suspend fun markAllNotificationsAsRead() = dao.markAllNotificationsAsRead()
    suspend fun deleteProfessionalNotification(notification: ProfessionalNotificationEntity) = dao.deleteProfessionalNotification(notification)

    // AI Monitoring Config
    val aiMonitoringConfigFlow: Flow<AiMonitoringConfigEntity?> = dao.getAiMonitoringConfigFlow()
    suspend fun saveAiMonitoringConfig(config: AiMonitoringConfigEntity) = dao.insertOrUpdateAiMonitoringConfig(config)

    // Waiting Queue
    val waitingQueueFlow: Flow<List<WaitingQueueEntity>> = dao.getAllWaitingQueueFlow()
    suspend fun insertWaitingQueue(item: WaitingQueueEntity): Long = dao.insertWaitingQueue(item)
    suspend fun updateWaitingQueue(item: WaitingQueueEntity) = dao.updateWaitingQueue(item)
    suspend fun deleteWaitingQueue(item: WaitingQueueEntity) = dao.deleteWaitingQueue(item)

    // Procedure History
    val procedureHistoryFlow: Flow<List<ProcedureHistoryEntity>> = dao.getAllProcedureHistoryFlow()
    fun getProcedureHistoryForClient(clientId: Long): Flow<List<ProcedureHistoryEntity>> = dao.getProcedureHistoryForClientFlow(clientId)
    suspend fun insertProcedureHistory(history: ProcedureHistoryEntity): Long = dao.insertProcedureHistory(history)
    suspend fun deleteProcedureHistory(history: ProcedureHistoryEntity) = dao.deleteProcedureHistory(history)

    // VIP Subscriptions
    val vipSubscriptionsFlow: Flow<List<VipSubscriptionEntity>> = dao.getAllVipSubscriptionsFlow()
    suspend fun insertVipSubscription(sub: VipSubscriptionEntity): Long = dao.insertVipSubscription(sub)
    suspend fun updateVipSubscription(sub: VipSubscriptionEntity) = dao.updateVipSubscription(sub)
    suspend fun deleteVipSubscription(sub: VipSubscriptionEntity) = dao.deleteVipSubscription(sub)
}

