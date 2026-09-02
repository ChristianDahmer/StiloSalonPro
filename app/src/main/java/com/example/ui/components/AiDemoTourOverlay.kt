package com.example.ui.components

import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.*

data class DemoStep(
    val title: String,
    val description: String,
    val targetTab: String? = null
)

@Composable
fun AiDemoTourOverlay(
    isDemoActive: Boolean,
    demoSpeed: Float,
    onSpeedChange: (Float) -> Unit,
    onCloseDemo: () -> Unit,
    onNavigateTab: (String) -> Unit
) {
    val context = LocalContext.current
    var currentStepIndex by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsInitialized by remember { mutableStateOf(false) }

    val steps = listOf(
        DemoStep(
            "🌟 Bem-vindo ao Gold Salão (Modo IA & Voz)",
            "Este é o seu assistente inteligente de gestão. Vamos demonstrar os principais recursos guiados por voz e inteligência artificial.",
            "AGENDAMENTOS"
        ),
        DemoStep(
            "📅 Gestão de Agendamentos",
            "Aqui você gerencia todos os agendamentos, status em tempo real, confirmações via WhatsApp e emissão fiscal.",
            "AGENDAMENTOS"
        ),
        DemoStep(
            "👥 Cadastro de Clientes e Serviços",
            "Gerencie clientes e serviços. Importante: em pagamentos por Pix e Cartão, o sistema exige CPF, telefone e e-mail para emissão da NFS-e.",
            "CLIENTES"
        ),
        DemoStep(
            "💰 Caixa & Financeiro",
            "Controle completo de receitas, despesas, pagamentos e fluxo de caixa diário e mensal do salão.",
            "FINANCEIRO"
        ),
        DemoStep(
            "💬 WhatsApp & Automação com IA",
            "Dispare campanhas promocionais, receba solicitações via IA e envie confirmações automáticas aos clientes.",
            "WHATSAPP"
        )
    )

    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("pt", "BR")
                try {
                    val voices = tts?.voices
                    val maleVoice = voices?.firstOrNull { 
                        it.locale.language == "pt" && (it.name.contains("male", ignoreCase = true) || it.name.contains("masculino", ignoreCase = true) || it.name.contains("pt-br-x-", ignoreCase = true))
                    } ?: voices?.firstOrNull { it.locale.language.startsWith("pt") }
                    if (maleVoice != null) {
                        tts?.voice = maleVoice
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                ttsInitialized = true
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    // Update speech rate when demoSpeed changes
    LaunchedEffect(demoSpeed, ttsInitialized) {
        if (ttsInitialized) {
            tts?.setSpeechRate(demoSpeed)
        }
    }

    // Speak current step when step index or playing changes
    LaunchedEffect(currentStepIndex, isPlaying, ttsInitialized) {
        if (isDemoActive && isPlaying && ttsInitialized && currentStepIndex < steps.size) {
            val step = steps[currentStepIndex]
            if (step.targetTab != null) {
                onNavigateTab(step.targetTab)
            }
            val textToSpeak = "${step.title}. ${step.description}"
            tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "DEMO_UTTERANCE")
        }
    }

    if (isDemoActive) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Demonstração IA • Passo ${currentStepIndex + 1} de ${steps.size}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onCloseDemo) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }

                    LinearProgressIndicator(
                        progress = { (currentStepIndex + 1).toFloat() / steps.size },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary
                    )

                    val step = steps[currentStepIndex]
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Speed control
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Velocidade:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                FilterChip(
                                    selected = demoSpeed == speed,
                                    onClick = { onSpeedChange(speed) },
                                    label = { Text("${speed}x", fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (currentStepIndex > 0) currentStepIndex--
                            },
                            enabled = currentStepIndex > 0
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Anterior")
                        }

                        Button(
                            onClick = {
                                isPlaying = !isPlaying
                                if (!isPlaying) {
                                    tts?.stop()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isPlaying) "Pausar" else "Continuar")
                        }

                        Button(
                            onClick = {
                                if (currentStepIndex < steps.size - 1) {
                                    currentStepIndex++
                                } else {
                                    Toast.makeText(context, "Demonstração concluída com sucesso!", Toast.LENGTH_SHORT).show()
                                    onCloseDemo()
                                }
                            }
                        ) {
                            Text(if (currentStepIndex < steps.size - 1) "Próximo" else "Concluir")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
