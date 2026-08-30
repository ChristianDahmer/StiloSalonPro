package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SalonDao {
    // Salon Settings
    @Query("SELECT * FROM salon_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<SalonSettingsEntity?>

    @Query("SELECT * FROM salon_settings WHERE id = 1")
    suspend fun getSettings(): SalonSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: SalonSettingsEntity)

    // Clients
    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAllClientsFlow(): Flow<List<ClientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity): Long

    @Update
    suspend fun updateClient(client: ClientEntity)

    @Delete
    suspend fun deleteClient(client: ClientEntity)

    // Services
    @Query("SELECT * FROM services ORDER BY name ASC")
    fun getAllServicesFlow(): Flow<List<ServiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceEntity): Long

    @Update
    suspend fun updateService(service: ServiceEntity)

    @Delete
    suspend fun deleteService(service: ServiceEntity)

    // Professionals
    @Query("SELECT * FROM professionals ORDER BY name ASC")
    fun getAllProfessionalsFlow(): Flow<List<ProfessionalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfessional(professional: ProfessionalEntity): Long

    @Update
    suspend fun updateProfessional(professional: ProfessionalEntity)

    @Delete
    suspend fun deleteProfessional(professional: ProfessionalEntity)

    // Appointments
    @Query("SELECT * FROM appointments ORDER BY date DESC, time ASC")
    fun getAllAppointmentsFlow(): Flow<List<AppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity): Long

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)

    @Delete
    suspend fun deleteAppointment(appointment: AppointmentEntity)

    // NFS-e
    @Query("SELECT * FROM nfse ORDER BY id DESC")
    fun getAllNfseFlow(): Flow<List<NfseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNfse(nfse: NfseEntity): Long

    @Update
    suspend fun updateNfse(nfse: NfseEntity)

    @Query("SELECT * FROM nfse_config WHERE id = 1")
    fun getNfseConfigFlow(): Flow<NfseConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateNfseConfig(config: NfseConfigEntity)

    // WhatsApp Templates & Logs
    @Query("SELECT * FROM whatsapp_templates")
    fun getAllWhatsAppTemplatesFlow(): Flow<List<WhatsAppTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWhatsAppTemplate(template: WhatsAppTemplateEntity): Long

    @Update
    suspend fun updateWhatsAppTemplate(template: WhatsAppTemplateEntity)

    @Delete
    suspend fun deleteWhatsAppTemplate(template: WhatsAppTemplateEntity)

    @Query("SELECT * FROM whatsapp_logs ORDER BY timestamp DESC")
    fun getAllWhatsAppLogsFlow(): Flow<List<WhatsAppLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWhatsAppLog(log: WhatsAppLogEntity): Long

    // Financial Transactions
    @Query("SELECT * FROM financial_transactions ORDER BY date DESC, id DESC")
    fun getAllFinancialTransactionsFlow(): Flow<List<FinancialTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinancialTransaction(transaction: FinancialTransactionEntity): Long

    @Update
    suspend fun updateFinancialTransaction(transaction: FinancialTransactionEntity)

    @Delete
    suspend fun deleteFinancialTransaction(transaction: FinancialTransactionEntity)

    // WhatsApp Inbound Messages (AI Monitor)
    @Query("SELECT * FROM whatsapp_inbound_messages ORDER BY timestamp DESC")
    fun getAllInboundMessagesFlow(): Flow<List<WhatsAppInboundMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInboundMessage(message: WhatsAppInboundMessageEntity): Long

    @Update
    suspend fun updateInboundMessage(message: WhatsAppInboundMessageEntity)

    @Delete
    suspend fun deleteInboundMessage(message: WhatsAppInboundMessageEntity)

    // Professional Notifications
    @Query("SELECT * FROM professional_notifications ORDER BY timestamp DESC")
    fun getAllProfessionalNotificationsFlow(): Flow<List<ProfessionalNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfessionalNotification(notification: ProfessionalNotificationEntity): Long

    @Update
    suspend fun updateProfessionalNotification(notification: ProfessionalNotificationEntity)

    @Query("UPDATE professional_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Long)

    @Query("UPDATE professional_notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    @Delete
    suspend fun deleteProfessionalNotification(notification: ProfessionalNotificationEntity)

    // AI Monitoring Config
    @Query("SELECT * FROM ai_monitoring_config WHERE id = 1")
    fun getAiMonitoringConfigFlow(): Flow<AiMonitoringConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAiMonitoringConfig(config: AiMonitoringConfigEntity)

    // Waiting Queue
    @Query("SELECT * FROM waiting_queue ORDER BY position ASC")
    fun getAllWaitingQueueFlow(): Flow<List<WaitingQueueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaitingQueue(item: WaitingQueueEntity): Long

    @Update
    suspend fun updateWaitingQueue(item: WaitingQueueEntity)

    @Delete
    suspend fun deleteWaitingQueue(item: WaitingQueueEntity)

    // Procedure History
    @Query("SELECT * FROM procedure_history ORDER BY date DESC")
    fun getAllProcedureHistoryFlow(): Flow<List<ProcedureHistoryEntity>>

    @Query("SELECT * FROM procedure_history WHERE clientId = :clientId ORDER BY date DESC")
    fun getProcedureHistoryForClientFlow(clientId: Long): Flow<List<ProcedureHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProcedureHistory(history: ProcedureHistoryEntity): Long

    @Delete
    suspend fun deleteProcedureHistory(history: ProcedureHistoryEntity)

    // VIP Subscriptions
    @Query("SELECT * FROM vip_subscriptions ORDER BY id DESC")
    fun getAllVipSubscriptionsFlow(): Flow<List<VipSubscriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVipSubscription(sub: VipSubscriptionEntity): Long

    @Update
    suspend fun updateVipSubscription(sub: VipSubscriptionEntity)

    @Delete
    suspend fun deleteVipSubscription(sub: VipSubscriptionEntity)
}

