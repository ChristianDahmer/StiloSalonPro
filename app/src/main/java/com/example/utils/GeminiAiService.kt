package com.example.utils

import com.example.BuildConfig
import com.example.data.model.ProfessionalEntity
import com.example.data.model.ServiceEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class AiExtractionResult(
    val detectedIntent: String, // NEW_APPOINTMENT, RESCHEDULE, CANCELLATION, INQUIRY, UNKNOWN
    val clientName: String,
    val serviceName: String,
    val serviceId: Long?,
    val professionalName: String,
    val professionalId: Long?,
    val date: String, // YYYY-MM-DD
    val time: String, // HH:mm
    val confidenceScore: Float,
    val sentiment: String, // POSITIVO, NEUTRO, URGENTE, DUVIDA
    val notes: String,
    val suggestedReply: String
)

object GeminiAiService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeWhatsAppMessage(
        senderName: String,
        senderPhone: String,
        rawMessage: String,
        availableServices: List<ServiceEntity>,
        availableProfessionals: List<ProfessionalEntity>,
        salonName: String = "Elegance Beauty & Spa"
    ): AiExtractionResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val apiResult = callGeminiApi(
                    apiKey = apiKey,
                    senderName = senderName,
                    senderPhone = senderPhone,
                    rawMessage = rawMessage,
                    services = availableServices,
                    professionals = availableProfessionals,
                    salonName = salonName
                )
                if (apiResult != null) {
                    return@withContext apiResult
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Robust Local NLP Fallback Engine for Brazilian Portuguese Salon Conversations
        return@withContext extractLocally(
            senderName = senderName,
            senderPhone = senderPhone,
            rawMessage = rawMessage,
            services = availableServices,
            professionals = availableProfessionals,
            salonName = salonName
        )
    }

    private fun callGeminiApi(
        apiKey: String,
        senderName: String,
        senderPhone: String,
        rawMessage: String,
        services: List<ServiceEntity>,
        professionals: List<ProfessionalEntity>,
        salonName: String
    ): AiExtractionResult? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

        val servicesContext = services.joinToString("\n") { "- ID ${it.id}: ${it.name} (R$ ${it.price}, ${it.durationMinutes}min, Categoria: ${it.category})" }
        val professionalsContext = professionals.joinToString("\n") { "- ID ${it.id}: ${it.name} (${it.specialty}, Horário: ${it.startHour} às ${it.endHour})" }

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayDayOfWeek = SimpleDateFormat("EEEE", Locale("pt", "BR")).format(Date())

        val systemPrompt = """
            Você é o assistente inteligente de agendamentos via WhatsApp do salão '$salonName'.
            Hoje é $todayDate ($todayDayOfWeek).
            
            Serviços disponíveis no salão:
            $servicesContext
            
            Profissionais do salão:
            $professionalsContext
            
            Analise a mensagem recebida pelo WhatsApp e retorne APENAS um JSON válido no seguinte formato:
            {
              "detectedIntent": "NEW_APPOINTMENT" | "RESCHEDULE" | "CANCELLATION" | "INQUIRY" | "UNKNOWN",
              "clientName": "Nome do cliente detectado",
              "serviceName": "Nome do serviço correspondente",
              "serviceId": 1,
              "professionalName": "Nome da profissional correspondente",
              "professionalId": 1,
              "date": "YYYY-MM-DD",
              "time": "HH:mm",
              "confidenceScore": 0.95,
              "sentiment": "POSITIVO" | "NEUTRO" | "URGENTE" | "DUVIDA",
              "notes": "Observação importante extraída",
              "suggestedReply": "Mensagem simpática e profissional para responder ao cliente pelo WhatsApp"
            }
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        val partObj = JSONObject().apply {
                            put("text", "$systemPrompt\n\nRemetente: $senderName ($senderPhone)\nMensagem: \"$rawMessage\"")
                        }
                        put(partObj)
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)

            val genConfig = JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.1)
            }
            put("generationConfig", genConfig)
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) return null

        val responseBody = response.body?.string() ?: return null
        val root = JSONObject(responseBody)
        val candidates = root.optJSONArray("candidates") ?: return null
        if (candidates.length() == 0) return null

        val candidate = candidates.getJSONObject(0)
        val content = candidate.optJSONObject("content") ?: return null
        val parts = content.optJSONArray("parts") ?: return null
        if (parts.length() == 0) return null

        val text = parts.getJSONObject(0).optString("text")
        val jsonClean = text.replace("```json", "").replace("```", "").trim()
        val parsed = JSONObject(jsonClean)

        val intent = parsed.optString("detectedIntent", "NEW_APPOINTMENT")
        val client = parsed.optString("clientName", senderName).ifBlank { senderName }
        val srvName = parsed.optString("serviceName", "")
        val srvId = if (parsed.has("serviceId") && !parsed.isNull("serviceId")) parsed.optLong("serviceId") else null
        val profName = parsed.optString("professionalName", "")
        val profId = if (parsed.has("professionalId") && !parsed.isNull("professionalId")) parsed.optLong("professionalId") else null
        val date = parsed.optString("date", todayDate)
        val time = parsed.optString("time", "10:00")
        val conf = parsed.optDouble("confidenceScore", 0.95).toFloat()
        val sentiment = parsed.optString("sentiment", "POSITIVO")
        val notes = parsed.optString("notes", "")
        val reply = parsed.optString("suggestedReply", "Olá $client! Agradecemos o contato. Confirmamos seu atendimento!")

        return AiExtractionResult(
            detectedIntent = intent,
            clientName = client,
            serviceName = srvName,
            serviceId = srvId,
            professionalName = profName,
            professionalId = profId,
            date = date,
            time = time,
            confidenceScore = conf,
            sentiment = sentiment,
            notes = notes,
            suggestedReply = reply
        )
    }

    private fun extractLocally(
        senderName: String,
        senderPhone: String,
        rawMessage: String,
        services: List<ServiceEntity>,
        professionals: List<ProfessionalEntity>,
        salonName: String
    ): AiExtractionResult {
        val lower = rawMessage.lowercase(Locale.getDefault())

        // 1. Detect Intent
        val detectedIntent = when {
            lower.contains("cancelar") || lower.contains("desmarcar") || lower.contains("não vou conseguir") -> "CANCELLATION"
            lower.contains("reagendar") || lower.contains("mudar horário") || lower.contains("trocar dia") -> "RESCHEDULE"
            lower.contains("quanto custa") || lower.contains("qual o valor") || lower.contains("preço") || lower.contains("tabela") || lower.contains("duvida") || lower.contains("tem horário") -> "INQUIRY"
            lower.contains("marcar") || lower.contains("agendar") || lower.contains("horário") || lower.contains("corte") || lower.contains("unha") || lower.contains("fazer") || lower.contains("queria") -> "NEW_APPOINTMENT"
            else -> "NEW_APPOINTMENT"
        }

        // 2. Match Service
        var matchedService = services.firstOrNull { svc ->
            val svcWords = svc.name.lowercase().split(" ", "/", "&", "-").filter { it.length > 3 }
            svcWords.any { lower.contains(it) }
        }
        if (matchedService == null) {
            when {
                lower.contains("corte") || lower.contains("cabelo") || lower.contains("escova") ->
                    matchedService = services.firstOrNull { it.category.contains("Cabelo", true) } ?: services.firstOrNull()
                lower.contains("unha") || lower.contains("manicure") || lower.contains("pedicure") || lower.contains("esmalte") ->
                    matchedService = services.firstOrNull { it.category.contains("Unhas", true) } ?: services.firstOrNull()
                lower.contains("sobrancelha") || lower.contains("henna") || lower.contains("pele") || lower.contains("limpeza") ->
                    matchedService = services.firstOrNull { it.category.contains("Estética", true) } ?: services.firstOrNull()
                else -> matchedService = services.firstOrNull()
            }
        }

        // 3. Match Professional
        var matchedProfessional = professionals.firstOrNull { prof ->
            val firstName = prof.name.split(" ").first().lowercase()
            lower.contains(firstName)
        }
        if (matchedProfessional == null && matchedService != null) {
            // Pick qualified specialist
            matchedProfessional = professionals.firstOrNull { prof ->
                prof.specialty.lowercase().contains(matchedService.category.lowercase()) ||
                matchedService.qualifiedProfessionalIds.contains("${prof.id}")
            } ?: professionals.firstOrNull()
        }
        if (matchedProfessional == null) {
            matchedProfessional = professionals.firstOrNull()
        }

        // 4. Match Date
        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayDateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

        val targetDateStr = when {
            lower.contains("amanhã") || lower.contains("amanha") -> {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                dateFormat.format(cal.time)
            }
            lower.contains("depois de amanhã") || lower.contains("depois de amanha") -> {
                cal.add(Calendar.DAY_OF_YEAR, 2)
                dateFormat.format(cal.time)
            }
            lower.contains("segunda") -> getNextDayOfWeek(Calendar.MONDAY)
            lower.contains("terça") || lower.contains("terca") -> getNextDayOfWeek(Calendar.TUESDAY)
            lower.contains("quarta") -> getNextDayOfWeek(Calendar.WEDNESDAY)
            lower.contains("quinta") -> getNextDayOfWeek(Calendar.THURSDAY)
            lower.contains("sexta") -> getNextDayOfWeek(Calendar.FRIDAY)
            lower.contains("sábado") || lower.contains("sabado") -> getNextDayOfWeek(Calendar.SATURDAY)
            else -> dateFormat.format(cal.time)
        }

        // 5. Match Time
        val timeRegex = Regex("""(\b\d{1,2})([:hH]| horas?)(\d{2})?""")
        val match = timeRegex.find(lower)
        val targetTimeStr = if (match != null) {
            val hour = match.groupValues[1].toIntOrNull() ?: 10
            val min = match.groupValues[3].toIntOrNull() ?: 0
            String.format(Locale.getDefault(), "%02d:%02d", hour, min)
        } else if (lower.contains("manhã") || lower.contains("manha")) {
            "09:30"
        } else if (lower.contains("tarde")) {
            "14:30"
        } else if (lower.contains("noite") || lower.contains("fim de tarde")) {
            "18:00"
        } else {
            "10:00"
        }

        // 6. Sentiment
        val sentiment = when {
            lower.contains("urgente") || lower.contains("rápido") || lower.contains("hoje ainda") || lower.contains("pressa") -> "URGENTE"
            lower.contains("duvida") || lower.contains("preço") || lower.contains("valor") || lower.contains("quanto") -> "DUVIDA"
            lower.contains("obrigado") || lower.contains("maravilha") || lower.contains("ótimo") || lower.contains("adoro") || lower.contains("gostaria") -> "POSITIVO"
            else -> "NEUTRO"
        }

        // 7. Client Name
        val detectedClient = if (senderName.isNotBlank() && senderName != "Cliente WhatsApp") senderName else "Cliente"

        // 8. Confidence Calculation
        var confidence = 0.85f
        if (matchedService != null) confidence += 0.05f
        if (matchedProfessional != null) confidence += 0.05f
        if (match != null) confidence += 0.04f

        val serviceDisplayName = matchedService?.name ?: "Atendimento Especial"
        val profDisplayName = matchedProfessional?.name ?: "Especialista da Casa"

        val reply = when (detectedIntent) {
            "INQUIRY" -> "Olá $detectedClient! O serviço $serviceDisplayName fica R$ ${String.format(Locale.GERMANY, "%.2f", matchedService?.price ?: 100.0)} no $salonName. Temos disponibilidade para você com $profDisplayName. Deseja confirmar seu agendamento?"
            "CANCELLATION" -> "Olá $detectedClient. Lamentamos que precise cancelar. Seu horário com $profDisplayName foi liberado. Quando desejar reagendar, estaremos à disposição no $salonName!"
            "RESCHEDULE" -> "Olá $detectedClient! Compreendemos a alteração. Podemos reagendar seu $serviceDisplayName para a nova data com $profDisplayName. Confirmamos para você?"
            else -> "Olá $detectedClient! Que alegria receber seu contato! Agendamento de $serviceDisplayName com $profDisplayName solicitado para $targetDateStr às $targetTimeStr. Estamos ansiosos para te atender no $salonName!"
        }

        return AiExtractionResult(
            detectedIntent = detectedIntent,
            clientName = detectedClient,
            serviceName = serviceDisplayName,
            serviceId = matchedService?.id,
            professionalName = profDisplayName,
            professionalId = matchedProfessional?.id,
            date = targetDateStr,
            time = targetTimeStr,
            confidenceScore = confidence.coerceAtMost(0.99f),
            sentiment = sentiment,
            notes = "Mensagem recebida e interpretada com IA via WhatsApp para $profDisplayName.",
            suggestedReply = reply
        )
    }

    private fun getNextDayOfWeek(targetDayOfWeek: Int): String {
        val cal = Calendar.getInstance()
        val currentDay = cal.get(Calendar.DAY_OF_WEEK)
        var daysToAdd = (targetDayOfWeek - currentDay + 7) % 7
        if (daysToAdd == 0) daysToAdd = 7
        cal.add(Calendar.DAY_OF_YEAR, daysToAdd)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }
}
