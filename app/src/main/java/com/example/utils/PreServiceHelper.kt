package com.example.utils

import com.example.data.model.AiMonitoringConfigEntity
import java.util.Locale

object PreServiceHelper {

    enum class MissingInfoStage {
        TUDO_INFORMADO,
        FALTA_SERVICO,
        FALTA_PROFISSIONAL,
        FALTA_DATA_HORA,
        INFORMACAO_INCOMPLETA
    }

    fun diagnoseMessage(
        rawMessage: String,
        extractedServiceName: String,
        extractedProfessionalName: String,
        extractedDate: String,
        extractedTime: String,
        detectedIntent: String
    ): MissingInfoStage {
        val lower = rawMessage.lowercase(Locale.getDefault()).trim()

        if (detectedIntent != "NEW_APPOINTMENT") {
            return MissingInfoStage.TUDO_INFORMADO
        }

        val isShortGreeting = lower.length < 15 &&
                (lower.contains("oi") || lower.contains("olá") || lower.contains("ola") ||
                 lower.contains("boa tarde") || lower.contains("bom dia") || lower.contains("boa noite") ||
                 lower.contains("gostaria") || lower.contains("tudo bem"))

        val hasService = extractedServiceName.isNotBlank() &&
                !extractedServiceName.equals("Atendimento Especial", ignoreCase = true) &&
                !extractedServiceName.equals("Serviço", ignoreCase = true)

        val hasProf = extractedProfessionalName.isNotBlank() &&
                !extractedProfessionalName.equals("Especialista da Casa", ignoreCase = true) &&
                !extractedProfessionalName.equals("Especialista", ignoreCase = true)

        val hasDateOrTimeMention = lower.contains("hoje") || lower.contains("amanhã") || lower.contains("amanha") ||
                lower.contains("segunda") || lower.contains("terça") || lower.contains("terca") || lower.contains("quarta") ||
                lower.contains("quinta") || lower.contains("sexta") || lower.contains("sábado") || lower.contains("sabado") ||
                Regex("""(\b\d{1,2})([:hH]| horas?)""").containsMatchIn(lower) || lower.contains("manhã") || lower.contains("tarde") || lower.contains("noite")

        if (isShortGreeting || (!hasService && !hasProf && !hasDateOrTimeMention)) {
            return MissingInfoStage.INFORMACAO_INCOMPLETA
        }

        if (!hasService) {
            return MissingInfoStage.FALTA_SERVICO
        }

        if (!hasProf) {
            return MissingInfoStage.FALTA_PROFISSIONAL
        }

        if (!hasDateOrTimeMention) {
            return MissingInfoStage.FALTA_DATA_HORA
        }

        return MissingInfoStage.TUDO_INFORMADO
    }

    fun generatePreServiceReply(
        stage: MissingInfoStage,
        config: AiMonitoringConfigEntity,
        salonName: String,
        clientName: String,
        serviceName: String,
        professionalName: String,
        dateFormatted: String,
        time: String,
        aiSuggestedReply: String
    ): String {
        val client = if (clientName.isBlank() || clientName == "Cliente WhatsApp") "Cliente" else clientName
        val template = when (stage) {
            MissingInfoStage.FALTA_SERVICO -> config.msgMissingService
            MissingInfoStage.FALTA_PROFISSIONAL -> config.msgMissingProfessional
            MissingInfoStage.FALTA_DATA_HORA -> config.msgMissingDateTime
            MissingInfoStage.INFORMACAO_INCOMPLETA -> config.msgIncompleteInfo
            MissingInfoStage.TUDO_INFORMADO -> if (aiSuggestedReply.isNotBlank()) aiSuggestedReply else config.msgCompleteConfirmation
        }

        return template
            .replace("{salão}", salonName)
            .replace("{salao}", salonName)
            .replace("{cliente}", client)
            .replace("{servico}", if (serviceName.isNotBlank()) serviceName else "Serviço")
            .replace("{profissional}", if (professionalName.isNotBlank()) professionalName else "Especialista")
            .replace("{data}", dateFormatted)
            .replace("{horario}", time)
            .replace("{horário}", time)
    }
}
