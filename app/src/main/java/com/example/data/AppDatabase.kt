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
        FinancialTransactionEntity::class,
        WhatsAppInboundMessageEntity::class,
        ProfessionalNotificationEntity::class,
        AiMonitoringConfigEntity::class,
        WaitingQueueEntity::class,
        ProcedureHistoryEntity::class,
        VipSubscriptionEntity::class
    ],
    version = 6,
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
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        getDatabase(context).salonDao().let { populateDatabase(it) }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        getDatabase(context).salonDao().let { populateDatabase(it) }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        suspend fun populateDatabase(dao: SalonDao) {
            // Settings
            if (dao.getSettings() == null) {
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
            }

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
                        title = "Campanha Aniversariantes do Mês",
                        templateText = "Parabéns, {nome_cliente}! No seu mês de aniversário, o {nome_salao} te presenteia com 15% OFF em qualquer serviço. Agende seu momento especial conosco!"
                    )
                )

                dao.insertWhatsAppTemplate(
                    WhatsAppTemplateEntity(
                        type = "PROMOTIONAL",
                        title = "Cupom de Retorno (Clientes Ausentes)",
                        templateText = "Olá {nome_cliente}! Estamos com saudades de você no {nome_salao}! Ganhe 20% OFF na sua próxima visita informando o cupom VOLTE20 ao agendar."
                    )
                )

                dao.insertWhatsAppTemplate(
                    WhatsAppTemplateEntity(
                        type = "PROMOTIONAL",
                        title = "Combo Fim de Semana Especial",
                        templateText = "Olá {nome_cliente}! Que tal renovar seu visual para este fim de semana no {nome_salao}? Temos horários promocionais disponíveis para você!"
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

                // AI Monitoring Config Initial
                dao.insertOrUpdateAiMonitoringConfig(
                    AiMonitoringConfigEntity(
                        id = 1,
                        isMonitoringActive = true,
                        autoApproveHighConfidence = false,
                        notifyProfessionalWhatsApp = true,
                        notifyAndroidSystem = true,
                        smartAutoReply = true
                    )
                )

                // Seed Inbound WhatsApp Messages (Analyzed by AI)
                val inb1 = dao.insertInboundMessage(
                    WhatsAppInboundMessageEntity(
                        senderName = "Mariana Silva",
                        senderPhone = "(11) 98765-4321",
                        rawMessage = "Oi! Gostaria de agendar um Corte Feminino com a Beatriz para amanhã às 15:00 se tiver horário disponível.",
                        timestamp = System.currentTimeMillis() - 15 * 60 * 1000,
                        aiProcessed = true,
                        detectedIntent = "NEW_APPOINTMENT",
                        extractedClientName = "Mariana Silva",
                        extractedServiceName = "Corte Feminino & Escova",
                        extractedServiceId = s1,
                        extractedProfessionalName = "Beatriz Oliveira",
                        extractedProfessionalId = p1,
                        extractedDate = today,
                        extractedTime = "15:00",
                        confidenceScore = 0.98f,
                        customerSentiment = "POSITIVO",
                        notes = "Cliente solicitou horário com preferência expressa pela cabeleireira Beatriz.",
                        suggestedReply = "Olá Mariana! Temos o horário das 15:00 com a Beatriz livre sim. Seu agendamento foi registrado com sucesso!",
                        status = "PENDENTE"
                    )
                )

                val inb2 = dao.insertInboundMessage(
                    WhatsAppInboundMessageEntity(
                        senderName = "Carla Souza",
                        senderPhone = "(11) 99123-4567",
                        rawMessage = "Boa tarde! Consigo fazer Manicure & Pedicure Completa hoje por volta das 16:30 com a Carla Mendes?",
                        timestamp = System.currentTimeMillis() - 45 * 60 * 1000,
                        aiProcessed = true,
                        detectedIntent = "NEW_APPOINTMENT",
                        extractedClientName = "Carla Souza",
                        extractedServiceName = "Manicure & Pedicure Completa",
                        extractedServiceId = s3,
                        extractedProfessionalName = "Carla Mendes",
                        extractedProfessionalId = p2,
                        extractedDate = today,
                        extractedTime = "16:30",
                        confidenceScore = 0.96f,
                        customerSentiment = "POSITIVO",
                        notes = "Cliente nova pelo WhatsApp.",
                        suggestedReply = "Olá Carla! Tudo bem? Temos sim disponibilidade às 16:30 com a Carla Mendes. Confirmamos seu horário?",
                        status = "PENDENTE"
                    )
                )

                val inb3 = dao.insertInboundMessage(
                    WhatsAppInboundMessageEntity(
                        senderName = "Patrícia Gomes",
                        senderPhone = "(11) 98877-6655",
                        rawMessage = "Olá! Qual o valor da Limpeza de Pele Profunda e vocês têm horário no sábado de manhã?",
                        timestamp = System.currentTimeMillis() - 2 * 3600 * 1000,
                        aiProcessed = true,
                        detectedIntent = "INQUIRY",
                        extractedClientName = "Patrícia Gomes",
                        extractedServiceName = "Limpeza de Pele Profunda",
                        extractedServiceId = s5,
                        extractedProfessionalName = "Daniela Rocha",
                        extractedProfessionalId = p3,
                        extractedDate = today,
                        extractedTime = "09:00",
                        confidenceScore = 0.91f,
                        customerSentiment = "DUVIDA",
                        notes = "Dúvida sobre valor e disponibilidade para Daniela Rocha.",
                        suggestedReply = "Olá Patrícia! A Limpeza de Pele Profunda custa R$ 180,00 e dura 90min com a Daniela Rocha. Temos horário às 09:00 e 10:30 no sábado. Gostaria de reservar?",
                        status = "PENDENTE"
                    )
                )

                // Seed Professional Notifications
                dao.insertProfessionalNotification(
                    ProfessionalNotificationEntity(
                        professionalId = p1,
                        professionalName = "Beatriz Oliveira",
                        professionalPhone = "(11) 91111-2222",
                        title = "Novo Agendamento Solicitado via WhatsApp",
                        message = "Mariana Silva solicitou Corte Feminino & Escova para hoje às 15:00 via WhatsApp.",
                        timestamp = System.currentTimeMillis() - 14 * 60 * 1000,
                        isRead = false,
                        inboundMessageId = inb1,
                        type = "NOVO_AGENDAMENTO",
                        whatsappDispatched = false
                    )
                )

                dao.insertProfessionalNotification(
                    ProfessionalNotificationEntity(
                        professionalId = p2,
                        professionalName = "Carla Mendes",
                        professionalPhone = "(11) 92222-3333",
                        title = "Novo Agendamento Solicitado via WhatsApp",
                        message = "Carla Souza solicitou Manicure & Pedicure Completa para hoje às 16:30 via WhatsApp.",
                        timestamp = System.currentTimeMillis() - 44 * 60 * 1000,
                        isRead = false,
                        inboundMessageId = inb2,
                        type = "NOVO_AGENDAMENTO",
                        whatsappDispatched = false
                    )
                )

                // Seed Waiting Queue
                dao.insertWaitingQueue(WaitingQueueEntity(clientId = c1, clientName = "Mariana Silva", clientPhone = "(11) 98765-4321", serviceName = "Corte Feminino & Escova", professionalName = "Beatriz Oliveira", position = 1, status = "AGUARDANDO", estimatedWaitMinutes = 15))
                dao.insertWaitingQueue(WaitingQueueEntity(clientId = c2, clientName = "Camila Rodrigues", clientPhone = "(11) 97654-3210", serviceName = "Manicure & Pedicure Completa", professionalName = "Carla Mendes", position = 2, status = "AGUARDANDO", estimatedWaitMinutes = 35))

                // Seed Procedure History
                dao.insertProcedureHistory(ProcedureHistoryEntity(clientId = c1, clientName = "Mariana Silva", serviceName = "Corte Feminino & Escova", professionalName = "Beatriz Oliveira", date = "2026-07-25", price = 120.00, notes = "Corte em camadas, cliente adorou."))
                dao.insertProcedureHistory(ProcedureHistoryEntity(clientId = c3, clientName = "Juliana Souza", serviceName = "Coloração / Mechas Balayage", professionalName = "Beatriz Oliveira", date = "2026-06-10", price = 350.00, notes = "Loiro iluminado com raiz esfumada."))

                // Seed VIP Subscriptions
                dao.insertVipSubscription(VipSubscriptionEntity(clientId = c1, clientName = "Mariana Silva", clientPhone = "(11) 98765-4321", planName = "Plano Escova Ouro", monthlyFee = 149.00, status = "ATIVO", startDate = "2026-08-01", renewalDate = "2026-09-01", remainingBenefits = "2 escovas / mês, 1 hidratação"))
                dao.insertVipSubscription(VipSubscriptionEntity(clientId = c3, clientName = "Juliana Souza", clientPhone = "(11) 96543-2109", planName = "Plano Premium Total", monthlyFee = 249.00, status = "ATIVO", startDate = "2026-08-05", renewalDate = "2026-09-05", remainingBenefits = "Corte ilimitado, 2 manicures"))
            }
        }
    }

