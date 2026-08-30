package com.example.service

import android.app.Notification
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.SalonRepository
import com.example.data.model.AppointmentEntity
import com.example.data.model.ProfessionalNotificationEntity
import com.example.data.model.WhatsAppInboundMessageEntity
import com.example.utils.GeminiAiService
import com.example.utils.MaskUtils
import com.example.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class WhatsAppNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val TAG = "WhatsAppAiMonitor"

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        if (sbn == null) return

        val packageName = sbn.packageName ?: return
        // Check if notification is from WhatsApp or WhatsApp Business
        if (packageName != "com.whatsapp" && packageName != "com.whatsapp.w4b") {
            return
        }

        val extras: Bundle? = sbn.notification?.extras
        if (extras == null) return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: text

        val finalMessage = if (bigText.isNotBlank()) bigText else text

        // Filter out generic status notifications
        if (title.isBlank() || finalMessage.isBlank() ||
            title.contains("WhatsApp", ignoreCase = true) ||
            finalMessage.contains("novas mensagens", ignoreCase = true) ||
            finalMessage.contains("mensagens não lidas", ignoreCase = true)
        ) {
            return
        }

        Log.d(TAG, "Interception: Sender = '$title', Message = '$finalMessage'")

        serviceScope.launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val repository = SalonRepository(db.salonDao())

                val config = repository.aiMonitoringConfigFlow.firstOrNull()
                if (config != null && !config.isMonitoringActive) {
                    Log.d(TAG, "Monitoring is disabled in settings.")
                    return@launch
                }

                val services = repository.servicesFlow.firstOrNull() ?: emptyList()
                val professionals = repository.professionalsFlow.firstOrNull() ?: emptyList()
                val settings = repository.settingsFlow.firstOrNull()
                val salonName = settings?.nomeFantasia ?: "Salão de Beleza"

                // Process with Gemini AI + Local NLP fallback
                val aiResult = GeminiAiService.analyzeWhatsAppMessage(
                    senderName = title,
                    senderPhone = "(WhatsApp)",
                    rawMessage = finalMessage,
                    availableServices = services,
                    availableProfessionals = professionals,
                    salonName = salonName
                )

                // Match targets
                val targetProf = professionals.firstOrNull { it.id == aiResult.professionalId }
                    ?: professionals.firstOrNull { it.name.equals(aiResult.professionalName, ignoreCase = true) }
                    ?: professionals.firstOrNull()

                val targetSvc = services.firstOrNull { it.id == aiResult.serviceId }
                    ?: services.firstOrNull { it.name.equals(aiResult.serviceName, ignoreCase = true) }
                    ?: services.firstOrNull()

                val shouldAutoApprove = (config?.autoApproveHighConfidence == true) &&
                        aiResult.confidenceScore >= 0.90f &&
                        aiResult.detectedIntent == "NEW_APPOINTMENT"

                var createdAppointmentId: Long? = null
                val initialStatus = if (shouldAutoApprove) "AUTO_AGENDADO" else "PENDENTE"

                if (shouldAutoApprove) {
                    val newAppt = AppointmentEntity(
                        clientId = 0,
                        clientName = aiResult.clientName,
                        clientPhone = "(WhatsApp)",
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

                val stage = com.example.utils.PreServiceHelper.diagnoseMessage(
                    rawMessage = finalMessage,
                    extractedServiceName = targetSvc?.name ?: aiResult.serviceName,
                    extractedProfessionalName = targetProf?.name ?: aiResult.professionalName,
                    extractedDate = aiResult.date,
                    extractedTime = aiResult.time,
                    detectedIntent = aiResult.detectedIntent
                )

                val activeConfig = config ?: com.example.data.model.AiMonitoringConfigEntity()
                val preServiceReply = com.example.utils.PreServiceHelper.generatePreServiceReply(
                    stage = stage,
                    config = activeConfig,
                    salonName = salonName,
                    clientName = aiResult.clientName,
                    serviceName = targetSvc?.name ?: aiResult.serviceName,
                    professionalName = targetProf?.name ?: aiResult.professionalName,
                    dateFormatted = com.example.utils.MaskUtils.formatDatePtBr(aiResult.date),
                    time = aiResult.time,
                    aiSuggestedReply = aiResult.suggestedReply
                )

                val inboundEntity = WhatsAppInboundMessageEntity(
                    senderName = title,
                    senderPhone = "(WhatsApp)",
                    rawMessage = finalMessage,
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

                if (activeConfig.autoPreServiceEnabled && preServiceReply.isNotBlank()) {
                    repository.insertWhatsAppLog(
                        com.example.data.model.WhatsAppLogEntity(
                            recipientName = aiResult.clientName.ifBlank { title },
                            recipientPhone = "(WhatsApp)",
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

                    val notifMessage = "${aiResult.clientName} enviou: \"$finalMessage\"\n(Serviço: ${targetSvc?.name ?: aiResult.serviceName} em ${MaskUtils.formatDatePtBr(aiResult.date)} às ${aiResult.time})"

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

                    if (config?.notifyAndroidSystem != false) {
                        NotificationHelper.showProfessionalNotification(
                            context = applicationContext,
                            notificationId = inboundId.toInt(),
                            title = notifTitle,
                            message = notifMessage,
                            professionalName = targetProf.name
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in WhatsApp interception", e)
            }
        }
    }
}
