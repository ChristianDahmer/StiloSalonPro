package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ProfessionalEntity
import com.example.data.model.ServiceEntity
import com.example.utils.GeminiAiService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Gold Salão", appName)
    }

    @Test
    fun `test GeminiAiService local NLP intent and entity parsing`() = runBlocking {
        val sampleServices = listOf(
            ServiceEntity(id = 1, name = "Corte Feminino & Escova", description = "Corte e escova profissional", category = "Cabelo", durationMinutes = 60, price = 120.0),
            ServiceEntity(id = 2, name = "Manicure & Pedicure Completa", description = "Cuidados para mãos e pés", category = "Unhas", durationMinutes = 75, price = 85.0),
            ServiceEntity(id = 3, name = "Coloração & Mechas Balayage", description = "Coloração completa com mechas", category = "Cabelo", durationMinutes = 180, price = 350.0)
        )

        val sampleProfessionals = listOf(
            ProfessionalEntity(id = 1, name = "Beatriz Oliveira", specialty = "Hairstylist & Colorista", phone = "(11) 98888-1111", email = "beatriz@salon.com"),
            ProfessionalEntity(id = 2, name = "Carla Mendes", specialty = "Nail Designer & Manicure", phone = "(11) 98888-2222", email = "carla@salon.com")
        )

        val result = GeminiAiService.analyzeWhatsAppMessage(
            senderName = "Mariana Silva",
            senderPhone = "(11) 98765-4321",
            rawMessage = "Olá, gostaria de agendar um corte feminino com a Beatriz amanhã às 14:30. Vocês têm vaga?",
            availableServices = sampleServices,
            availableProfessionals = sampleProfessionals,
            salonName = "Elegance Hair & Beauty"
        )

        assertEquals("NEW_APPOINTMENT", result.detectedIntent)
        assertEquals("Beatriz Oliveira", result.professionalName)
        assertEquals(1L, result.professionalId)
        assertEquals("Corte Feminino & Escova", result.serviceName)
        assertEquals(1L, result.serviceId)
        assertEquals("14:30", result.time)
        assertTrue(result.confidenceScore >= 0.8f)
        assertNotNull(result.suggestedReply)
    }
}
