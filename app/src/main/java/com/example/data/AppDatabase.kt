package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.SalonDao
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SalonSettingsEntity::class,
        ClientEntity::class,
        ServiceEntity::class,
        ProfessionalEntity::class,
        AppointmentEntity::class,
        NfseEntity::class,
        NfseConfigEntity::class,
        WhatsAppTemplateEntity::class,
        WhatsAppLogEntity::class,
        FinancialTransactionEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun salonDao(): SalonDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "salon_beauty_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.salonDao())
                    }
                }
            }

            suspend fun populateDatabase(dao: SalonDao) {
                // Settings
                dao.insertOrUpdateSettings(
                    SalonSettingsEntity(
                        id = 1,
                        razaoSocial = "Salão de Beleza Elegance Ltda",
                        nomeFantasia = "Elegance Beauty & Spa",
                        cnpj = "12.345.678/0001-90",
                        inscricaoEstadual = "123.456.789.110",
                        defaultStartScreen = "AGENDAMENTOS",
                        themePalette = "ROSE_GOLD",
                        darkModeOption = "SYSTEM",
                        useDynamicColor = false
                    )
                )

                // Clients
                val c1 = dao.insertClient(ClientEntity(name = "Mariana Silva", cpf = "123.456.789-01", phone = "(11) 98765-4321", email = "mariana@gmail.com", birthDate = "1994-07-15"))
                val c2 = dao.insertClient(ClientEntity(name = "Camila Rodrigues", cpf = "234.567.890-12", phone = "(11) 97654-3210", email = "camila.rod@hotmail.com", birthDate = "1990-07-28"))
                val c3 = dao.insertClient(ClientEntity(name = "Juliana Souza", cpf = "345.678.901-23", phone = "(11) 96543-2109", email = "jusouza@outlook.com", birthDate = "1988-11-04"))
                val c4 = dao.insertClient(ClientEntity(name = "Fernanda Lima", cpf = "456.789.012-34", phone = "(11) 95432-1098", email = "nandalima@yahoo.com.br", birthDate = "1996-03-22"))

                // Professionals
                val p1 = dao.insertProfessional(ProfessionalEntity(name = "Beatriz Oliveira", phone = "(11) 91111-2222", email = "bea.hair@elegance.com", specialty = "Hairstylist & Colorista", workDays = "Seg,Ter,Qua,Qui,Sex,Sáb", startHour = "08:00", endHour = "18:00"))
                val p2 = dao.insertProfessional(ProfessionalEntity(name = "Carla Mendes", phone = "(11) 92222-3333", email = "carla.nails@elegance.com", specialty = "Manicure & Nail Designer", workDays = "Ter,Qua,Qui,Sex,Sáb", startHour = "09:00", endHour = "19:00"))
                val p3 = dao.insertProfessional(ProfessionalEntity(name = "Daniela Rocha", phone = "(11) 93333-4444", email = "dani.estetica@elegance.com", specialty = "Esteticista & Designer Sobrancelhas", workDays = "Seg,Ter,Qua,Sex,Sáb", startHour = "08:30", endHour = "17:30"))

                // Services
                val s1 = dao.insertService(ServiceEntity(name = "Corte Feminino & Escova", description = "Corte personalizado com lavagem especial e escova modeladora", price = 120.00, durationMinutes = 60, category = "Cabelo", qualifiedProfessionalIds = "$p1", taxCode = "06.01"))
                val s2 = dao.insertService(ServiceEntity(name = "Coloração / Mechas Balayage", description = "Coloração premium com matização e tratamento reconstrutor", price = 350.00, durationMinutes = 180, category = "Cabelo", qualifiedProfessionalIds = "$p1", taxCode = "06.01"))
                val s3 = dao.insertService(ServiceEntity(name = "Manicure & Pedicure Completa", description = "Cutilagem, esaltação e hidratação profunda das mãos e pés", price = 85.00, durationMinutes = 75, category = "Unhas", qualifiedProfessionalIds = "$p2", taxCode = "06.01"))
                val s4 = dao.insertService(ServiceEntity(name = "Design de Sobrancelha & Henna", description = "Mapeamento facial, limpeza precisa e aplicação de henna", price = 65.00, durationMinutes = 45, category = "Estética", qualifiedProfessionalIds = "$p3", taxCode = "06.01"))
                val s5 = dao.insertService(ServiceEntity(name = "Limpeza de Pele Profunda", description = "Higienização, vapor de ozônio, extração e máscara calmante", price = 180.00, durationMinutes = 90, category = "Estética", qualifiedProfessionalIds = "$p3", taxCode = "06.01"))

                // Sample Appointments (Today & upcoming dates)
                val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                
                dao.insertAppointment(
                    AppointmentEntity(
                        clientId = c1,
                        clientName = "Mariana Silva",
                        clientPhone = "(11) 98765-4321",
                        serviceId = s1,
                        serviceName = "Corte Feminino & Escova",
                        professionalId = p1,
                        professionalName = "Beatriz Oliveira",
                        date = today,
                        time = "09:00",
                        durationMinutes = 60,
                        price = 120.00,
                        status = "AGENDADO",
                        notes = "Cliente prefere corte repicado nas pontas."
                    )
                )

                dao.insertAppointment(
                    AppointmentEntity(
                        clientId = c2,
                        clientName = "Camila Rodrigues",
                        clientPhone = "(11) 97654-3210",
                        serviceId = s3,
                        serviceName = "Manicure & Pedicure Completa",
                        professionalId = p2,
                        professionalName = "Carla Mendes",
                        date = today,
                        time = "10:30",
                        durationMinutes = 75,
                        price = 85.00,
                        status = "AGENDADO",
                        notes = "Esmalte cor Nude Chic."
                    )
                )

                dao.insertAppointment(
                    AppointmentEntity(
                        clientId = c3,
                        clientName = "Juliana Souza",
                        clientPhone = "(11) 96543-2109",
                        serviceId = s2,
                        serviceName = "Coloração / Mechas Balayage",
                        professionalId = p1,
                        professionalName = "Beatriz Oliveira",
                        date = today,
                        time = "14:00",
                        durationMinutes = 180,
                        price = 350.00,
                        status = "CONCLUIDO",
                        notes = "Procedimento finalizado com sucesso."
                    )
                )

                // Schedule Block
                dao.insertAppointment(
                    AppointmentEntity(
                        clientId = 0,
                        clientName = "BLOQUEIO DE AGENDA",
                        clientPhone = "",
                        serviceId = 0,
                        serviceName = "Manutenção de Equipamentos",
                        professionalId = p3,
                        professionalName = "Daniela Rocha",
                        date = today,
                        time = "12:00",
                        durationMinutes = 60,
                        price = 0.0,
                        status = "BLOQUEIO",
                        isBlock = true,
                        notes = "Intervalo e higienização dos aparelhos de estética."
                    )
                )

                // NFS-e Config & Sample
                dao.insertOrUpdateNfseConfig(
                    NfseConfigEntity(
                        id = 1,
                        certificadoStatus = "VÁLIDO (A1)",
                        apiToken = "tok_prod_89a7f6e5d4c3b2a1",
                        inscricaoMunicipal = "987654-3",
                        regimeEspecial = "Simples Nacional",
                        aliquotaIss = 2.50
                    )
                )

                dao.insertNfse(
                    NfseEntity(
                        appointmentId = 3,
                        clientName = "Juliana Souza",
                        clientCpf = "345.678.901-23",
                        serviceName = "Coloração / Mechas Balayage",
                        price = 350.00,
                        issueDate = "$today 17:05",
                        status = "EMITIDA",
                        number = "2026000142",
                        verificationCode = "A8F9-2B3C",
                        xmlContent = "<Nfse><Numero>2026000142</Numero><Valor>350.00</Valor></Nfse>",
                        pdfUrl = "https://nfse.prefeitura.sp.gov.br/pdf/2026000142"
                    )
                )

                // WhatsApp Templates
                dao.insertWhatsAppTemplate(
                    WhatsAppTemplateEntity(
                        type = "CONFIRMATION",
                        title = "Confirmação de Agendamento",
                        templateText = "Olá, {nome_cliente}! Seu agendamento para {servico} com {nome_profissional} no {nome_salao} está CONFIRMADO para {data} às {horario}. Qualquer dúvida entre em contato!"
                    )
                )

                dao.insertWhatsAppTemplate(
                    WhatsAppTemplateEntity(
                        type = "REMINDER",
                        title = "Lembrete de Horário",
                        templateText = "Olá {nome_cliente}! Lembramos do seu horário amanhã ({data} às {horario}) para {servico} no {nome_salao}. Estamos te esperando com carinho!"
                    )
                )

                dao.insertWhatsAppTemplate(
                    WhatsAppTemplateEntity(
                        type = "POST_SERVICE",
                        title = "Agradecimento Pós-Atendimento",
                        templateText = "Agradecemos pela visita hoje no {nome_salao}, {nome_cliente}! Esperamos que tenha amado seu atendimento de {servico} com {nome_profissional}. Até a próxima!"
                    )
                )

                dao.insertWhatsAppTemplate(
                    WhatsAppTemplateEntity(
                        type = "PROMOTIONAL",
                        title = "Campanha Aniversariantes / Promoção",
                        templateText = "Parabéns, {nome_cliente}! No seu mês de aniversário, o {nome_salao} te presenteia com 15% OFF em serviços de {servico}. Agende seu momento especial!"
                    )
                )

                // WhatsApp Logs
                dao.insertWhatsAppLog(
                    WhatsAppLogEntity(
                        recipientName = "Mariana Silva",
                        recipientPhone = "(11) 98765-4321",
                        messageText = "Olá, Mariana Silva! Seu agendamento para Corte Feminino & Escova no Elegance Beauty & Spa está CONFIRMADO.",
                        status = "ENVIADO",
                        campaignName = "Confirmação Direta"
                    )
                )

                // Initial Financial Seed Data
                dao.insertFinancialTransaction(
                    FinancialTransactionEntity(
                        type = "RECEITA",
                        description = "Atendimento - Coloração / Mechas Balayage (Juliana Souza)",
                        amount = 350.00,
                        category = "Serviços",
                        date = today,
                        paymentMethod = "PIX",
                        status = "PAGO",
                        relatedAppointmentId = 3,
                        notes = "Receita vinda de agendamento concluído"
                    )
                )

                dao.insertFinancialTransaction(
                    FinancialTransactionEntity(
                        type = "RECEITA",
                        description = "Atendimento - Corte Feminino & Escova (Mariana Silva)",
                        amount = 120.00,
                        category = "Serviços",
                        date = today,
                        paymentMethod = "CARTAO_CREDITO",
                        status = "PAGO",
                        relatedAppointmentId = 1,
                        notes = "Pagamento aprovado na maquininha"
                    )
                )

                dao.insertFinancialTransaction(
                    FinancialTransactionEntity(
                        type = "RECEITA",
                        description = "Venda Kit Manutenção Capilar Kerastase",
                        amount = 280.00,
                        category = "Vendas de Produtos",
                        date = today,
                        paymentMethod = "PIX",
                        status = "PAGO",
                        notes = "Venda direta no balcão"
                    )
                )

                dao.insertFinancialTransaction(
                    FinancialTransactionEntity(
                        type = "DESPESA",
                        description = "Aluguel Comercial do Salão (Mês Vigente)",
                        amount = 2500.00,
                        category = "Aluguel",
                        date = today,
                        paymentMethod = "BOLETO",
                        status = "PAGO",
                        notes = "Transferência bancária confirmada"
                    )
                )

                dao.insertFinancialTransaction(
                    FinancialTransactionEntity(
                        type = "DESPESA",
                        description = "Reposição de Estoque Tinturas & Descolorantes (Wella)",
                        amount = 890.00,
                        category = "Fornecedores",
                        date = today,
                        paymentMethod = "CARTAO_CREDITO",
                        status = "PAGO",
                        notes = "Parcelado em 2x sem juros"
                    )
                )

                dao.insertFinancialTransaction(
                    FinancialTransactionEntity(
                        type = "DESPESA",
                        description = "Conta de Energia Elétrica & Água",
                        amount = 450.00,
                        category = "Utilidades",
                        date = today,
                        paymentMethod = "PIX",
                        status = "PAGO",
                        notes = "Pagamento automático via código de barras"
                    )
                )

                dao.insertFinancialTransaction(
                    FinancialTransactionEntity(
                        type = "DESPESA",
                        description = "Comissão Semanal - Beatriz Oliveira (Cabeleireira)",
                        amount = 620.00,
                        category = "Comissões",
                        date = today,
                        paymentMethod = "PIX",
                        status = "PENDENTE",
                        notes = "Repasse de comissão agendado para sexta-feira"
                    )
                )

                dao.insertFinancialTransaction(
                    FinancialTransactionEntity(
                        type = "RECEITA",
                        description = "Sinal / Reserva Pacote Dia da Noiva (Camila R.)",
                        amount = 400.00,
                        category = "Serviços",
                        date = today,
                        paymentMethod = "PIX",
                        status = "PENDENTE",
                        notes = "Aguardando confirmação do comprovante"
                    )
                )
            }
        }
    }
}
