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
}
