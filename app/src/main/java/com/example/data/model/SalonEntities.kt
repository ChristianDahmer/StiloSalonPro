package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "salon_settings")
data class SalonSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val razaoSocial: String = "Salão de Beleza Elegance Ltda",
    val nomeFantasia: String = "Elegance Beauty & Spa",
    val cnpj: String = "12.345.678/0001-90",
    val inscricaoEstadual: String = "123.456.789.110",
    val defaultStartScreen: String = "AGENDAMENTOS", // AGENDAMENTOS, SERVICOS, PROFISSIONAIS, CLIENTES, NFSE, WHATSAPP
    val themePalette: String = "ROSE_GOLD", // ROSE_GOLD, ROYAL_VIOLET, EMERALD_GLAM, GOLDEN_LUXURY, SUNSET_COPPER
    val darkModeOption: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    val useDynamicColor: Boolean = false,
    val primaryColorHex: String = "#D81B60",
    val secondaryColorHex: String = "#AD1457",
    val accentColorHex: String = "#F06292",
    val masterPassword: String = "1234"
)

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val cpf: String,
    val phone: String,
    val email: String,
    val birthDate: String // Format YYYY-MM-DD or DD/MM/YYYY
)

@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val price: Double,
    val durationMinutes: Int,
    val category: String, // Cabelo, Unhas, Estética, Maquiagem, Barba/Masculino, Outros
    val qualifiedProfessionalIds: String = "", // Comma-separated IDs
    val taxCode: String = "06.01" // Código LC 116
)

@Entity(tableName = "professionals")
data class ProfessionalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String,
    val specialty: String,
    val qualifiedServiceIds: String = "", // Comma-separated IDs
    val workDays: String = "Seg,Ter,Qua,Qui,Sex,Sáb",
    val startHour: String = "08:00",
    val endHour: String = "19:00"
)

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val clientName: String,
    val clientPhone: String,
    val serviceId: Long,
    val serviceName: String,
    val professionalId: Long,
    val professionalName: String,
    val date: String, // YYYY-MM-DD
    val time: String, // HH:mm
    val durationMinutes: Int,
    val price: Double,
    val status: String = "AGENDADO", // AGENDADO, CONCLUIDO, CANCELADO, BLOQUEIO
    val paymentMethod: String = "CARTAO", // CARTAO, PIX, DINHEIRO, OUTROS
    val isBlock: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "nfse")
data class NfseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val appointmentId: Long = 0,
    val clientName: String,
    val clientCpf: String,
    val serviceName: String,
    val price: Double,
    val issueDate: String, // YYYY-MM-DD HH:mm
    val paymentMethod: String = "CARTAO", // CARTAO, PIX
    val status: String = "EMITIDA", // EMITIDA, CANCELADA, RASCUNHO
    val number: String,
    val verificationCode: String,
    val cancellationReason: String? = null,
    val xmlContent: String = "",
    val pdfUrl: String = ""
)

@Entity(tableName = "nfse_config")
data class NfseConfigEntity(
    @PrimaryKey val id: Int = 1,
    val certificadoStatus: String = "VÁLIDO (A1)",
    val apiToken: String = "tok_prod_89a7f6e5d4c3b2a1",
    val inscricaoMunicipal: String = "987654-3",
    val regimeEspecial: String = "Simples Nacional",
    val aliquotaIss: Double = 2.50
)

@Entity(tableName = "whatsapp_templates")
data class WhatsAppTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // CONFIRMATION, REMINDER, POST_SERVICE, PROMOTIONAL
    val title: String,
    val templateText: String
)

@Entity(tableName = "whatsapp_logs")
data class WhatsAppLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipientName: String,
    val recipientPhone: String,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "ENVIADO", // ENVIADO, FALHA
    val campaignName: String? = null
)

@Entity(tableName = "financial_transactions")
data class FinancialTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // RECEITA, DESPESA
    val description: String,
    val amount: Double,
    val category: String, // Serviços, Vendas de Produtos, Fornecedores, Aluguel, Utilidades, Comissões, Impostos, Outros
    val date: String, // YYYY-MM-DD
    val paymentMethod: String = "PIX", // PIX, CARTAO_CREDITO, CARTAO_DEBITO, DINHEIRO, BOLETO, OUTROS
    val status: String = "PAGO", // PAGO, PENDENTE, CANCELADO
    val relatedAppointmentId: Long? = null,
    val relatedNfseId: Long? = null,
    val notes: String = ""
)
