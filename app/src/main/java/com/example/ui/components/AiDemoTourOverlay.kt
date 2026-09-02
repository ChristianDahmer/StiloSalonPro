package com.example.ui.components

import android.media.AudioAttributes
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

data class DemoStep(
    val id: String,
    val title: String,
    val moduleBadge: String,
    val description: String,
    val targetTab: String,
    val stepsToAdd: List<String>,
    val formExampleTitle: String,
    val formExampleFields: List<Pair<String, String>>,
    val actionButtonText: String,
    val narrationText: String
)

@OptIn(ExperimentalMaterial3Api::class)
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

    // Humanized voice configuration state
    val availableVoices = remember { mutableStateListOf<Voice>() }
    var selectedVoiceName by remember { mutableStateOf<String?>(null) }
    var voicePitch by remember { mutableStateOf(0.93f) } // Slightly deeper, warm masculine acoustic resonance
    var showVoiceSettings by remember { mutableStateOf(false) }

    val steps = remember {
        listOf(
            DemoStep(
                id = "INTRO",
                title = "Bem-vindo à Demonstração do Gold Salão",
                moduleBadge = "Visão Geral",
                description = "Neste tour interativo guiado por voz humanizada, você verá exatamente como incluir registros em cada módulo do sistema com facilidade.",
                targetTab = "AGENDAMENTOS",
                stepsToAdd = listOf(
                    "Navegue entre os módulos usando a barra de atalhos ou os botões de avançar.",
                    "Cada tela conta com um botão de inclusão rápida com o sinal de mais (+).",
                    "Acompanhe o preenchimento dos campos essenciais e validações automáticas.",
                    "Veja a simulação de salvamento e integração imediata com financeiro e notas fiscais."
                ),
                formExampleTitle = "Visão Geral do Sistema",
                formExampleFields = listOf(
                    "Operação" to "Gestão Completa de Salão de Beleza",
                    "Integrações" to "Agenda, WhatsApp Inteligente, NFS-e e Fluxo de Caixa",
                    "Modo de Demonstração" to "Ativo com Narração em Voz Masculina Humanizada"
                ),
                actionButtonText = "Iniciar Demonstração Passo a Passo",
                narrationText = "Olá! Seja muito bem-vindo ao Gold Salão. Eu sou o seu assistente virtual e vou acompanhar você em uma demonstração prática e completa... Em cada módulo, vou te mostrar onde clicar e como preencher os dados para cadastrar seus agendamentos, clientes, serviços, lançamentos de caixa e notas fiscais com total rapidez. Vamos começar?"
            ),
            DemoStep(
                id = "AGENDAMENTOS",
                title = "Agendamentos & Agenda",
                moduleBadge = "Módulo 1 • Agenda",
                description = "Controle de horários, encaixes, comandas abertas e status de atendimentos em tempo real.",
                targetTab = "AGENDAMENTOS",
                stepsToAdd = listOf(
                    "Passo 1: Toque no botão flutuante [+] (Novo Agendamento) no canto inferior direito.",
                    "Passo 2: Selecione o Cliente cadastrado (ou toque para cadastrar na hora).",
                    "Passo 3: Escolha o Serviço desejado e o Profissional responsável.",
                    "Passo 4: Selecione a Data no calendário, o Horário disponível e a Forma de Pagamento.",
                    "Passo 5: Toque em 'Confirmar Agendamento' para salvar na grade e liberar lembrete no WhatsApp."
                ),
                formExampleTitle = "Simulação: Novo Agendamento",
                formExampleFields = listOf(
                    "Cliente" to "Maria Oliveira",
                    "Serviço" to "Corte Feminino & Escova Modeladora",
                    "Profissional" to "Juliana Lima (Cabeleireira Master)",
                    "Data e Horário" to "Hoje às 14:30",
                    "Valor Total" to "R$ 120,00 (Pix)"
                ),
                actionButtonText = "Confirmar Agendamento",
                narrationText = "Aqui no módulo de Agendamentos, registrar um novo horário é super simples e rápido... Primeiro, dê um toque no botão com o sinal de mais, no canto inferior direito. Em seguida, selecione a cliente, o serviço e o profissional responsável... Depois, escolha a data, o melhor horário na grade e a forma de pagamento... Por fim, toque em Confirmar Agendamento. O horário fica imediatamente reservado na sua grade e o lembrete automático do WhatsApp já fica pronto para envio!"
            ),
            DemoStep(
                id = "CLIENTES",
                title = "Gestão de Clientes",
                moduleBadge = "Módulo 2 • Clientes",
                description = "Cadastro centralizado de clientes, histórico de serviços realizados, aniversariantes e dados fiscais.",
                targetTab = "CLIENTES",
                stepsToAdd = listOf(
                    "Passo 1: Toque no botão [+] (Novo Cliente) na barra superior ou flutuante.",
                    "Passo 2: Digite o Nome Completo e o Telefone WhatsApp com DDD.",
                    "Passo 3: Informe o CPF e o E-mail (obrigatórios em pagamentos por Pix/Cartão para NFS-e).",
                    "Passo 4: Insira a Data de Nascimento para o envio automático de felicitações.",
                    "Passo 5: Toque em 'Salvar Cliente' para finalizar o cadastro."
                ),
                formExampleTitle = "Simulação: Cadastro de Cliente",
                formExampleFields = listOf(
                    "Nome Completo" to "Fernanda Santos",
                    "Telefone / WhatsApp" to "(11) 98765-4321",
                    "CPF" to "234.567.890-12",
                    "E-mail" to "fernanda.santos@email.com",
                    "Aniversário" to "15/10 (Campanhas automáticas ativadas)"
                ),
                actionButtonText = "Salvar Cliente",
                narrationText = "No módulo de Clientes, manter a sua base atualizada garante um atendimento personalizado e agiliza suas notas fiscais... Para cadastrar um cliente, toque no botão com o sinal de mais no topo da tela. Digite o nome completo e o WhatsApp com o DDD... É muito importante também preencher o CPF e o e-mail, pois eles são exigidos para a emissão da NFS-e. Você pode também adicionar a data de aniversário para felicitações automáticas... Agora, basta tocar em Salvar Cliente!"
            ),
            DemoStep(
                id = "SERVICOS",
                title = "Catálogo de Serviços",
                moduleBadge = "Módulo 3 • Serviços",
                description = "Menu completo de procedimentos com preços tabelados, duração média e categorias organizadas.",
                targetTab = "SERVICOS",
                stepsToAdd = listOf(
                    "Passo 1: Toque no botão [+] (Novo Serviço) na tela de catálogo.",
                    "Passo 2: Digite o Nome do Serviço (ex: Hidratação Profunda, Coloração, Manicure).",
                    "Passo 3: Selecione a Categoria (Cabelo, Unhas, Estética, Sobrancelhas ou Barba).",
                    "Passo 4: Defina o Preço sugerido em reais (R$) e o Tempo de atendimento (em minutos).",
                    "Passo 5: Toque em 'Salvar Serviço' para disponibilizá-lo na comanda e agendamentos."
                ),
                formExampleTitle = "Simulação: Novo Serviço",
                formExampleFields = listOf(
                    "Nome do Serviço" to "Hidratação & Reconstrução Capilar",
                    "Categoria" to "Cabelo",
                    "Preço" to "R$ 150,00",
                    "Duração Estimada" to "50 minutos",
                    "Status" to "Ativo no catálogo"
                ),
                actionButtonText = "Salvar Serviço",
                narrationText = "No catálogo de Serviços, você organiza o cardápio de procedimentos do seu salão... Para incluir um novo serviço, dê um toque no botão de mais. Preencha o nome do procedimento, como hidratação capilar, coloração ou manicure, e escolha a categoria... Em seguida, defina o preço sugerido e a duração estimada em minutos... Toque em Salvar Serviço, e ele já estará imediatamente disponível para comandas e novos agendamentos!"
            ),
            DemoStep(
                id = "PROFISSIONAIS",
                title = "Equipe & Profissionais",
                moduleBadge = "Módulo 4 • Profissionais",
                description = "Gerenciamento dos membros da equipe, especialidades atendidas, escala e cálculo de comissões.",
                targetTab = "PROFISSIONAIS",
                stepsToAdd = listOf(
                    "Passo 1: Toque no botão [+] (Novo Profissional) na aba de equipe.",
                    "Passo 2: Preencha o Nome e o WhatsApp do profissional parceiro.",
                    "Passo 3: Escolha as Especialidades atendidas e a porcentagem padrão de comissão (%).",
                    "Passo 4: Selecione uma Cor de destaque para identificar os agendamentos na grade.",
                    "Passo 5: Toque em 'Salvar Profissional' para habilitar a escala de atendimentos."
                ),
                formExampleTitle = "Simulação: Novo Profissional",
                formExampleFields = listOf(
                    "Nome" to "Juliana Lima",
                    "Especialidade" to "Cabeleireira & Colorista Master",
                    "Comissão Padrão" to "45% sobre os serviços realizados",
                    "WhatsApp" to "(11) 97777-8888",
                    "Cor da Agenda" to "Rosa Dourado (#E08E79)"
                ),
                actionButtonText = "Salvar Profissional",
                narrationText = "Na gestão de Profissionais, você cadastra sua equipe e define as regras de cada parceiro... Para cadastrar um colaborador, toque no botão de mais. Insira o nome, o WhatsApp e as especialidades atendidas... Depois, defina a porcentagem de comissão e escolha uma cor de destaque para identificar a coluna desse profissional na agenda... Finalize tocando em Salvar Profissional, e a escala dele já estará ativa!"
            ),
            DemoStep(
                id = "FINANCEIRO",
                title = "Gestão Financeira & Caixa",
                moduleBadge = "Módulo 5 • Financeiro",
                description = "Fluxo de caixa diário, entradas de atendimentos, despesas operacionais e demonstrativo de lucros.",
                targetTab = "FINANCEIRO",
                stepsToAdd = listOf(
                    "Passo 1: Toque em 'Nova Movimentação' ou no botão [+] no painel de caixa.",
                    "Passo 2: Selecione o Tipo: 'Entrada / Receita' (+) ou 'Saída / Despesa' (-).",
                    "Passo 3: Preencha a Descrição (ex: Compra de Produtos, Conta de Luz ou Atendimento).",
                    "Passo 4: Insira o Valor (R$), selecione a Forma de Pagamento e a Categoria contábil.",
                    "Passo 5: Toque em 'Salvar Lançamento' para atualizar o saldo do caixa instantaneamente."
                ),
                formExampleTitle = "Simulação: Lançamento Financeiro",
                formExampleFields = listOf(
                    "Tipo de Lançamento" to "Receita (+ Entrada de Caixa)",
                    "Descrição" to "Pacote Completo Mechas & Tratamento",
                    "Valor" to "R$ 380,00",
                    "Forma de Pagamento" to "Pix à Vista",
                    "Categoria" to "Procedimentos de Salão"
                ),
                actionButtonText = "Salvar Lançamento",
                narrationText = "Aqui no módulo Financeiro, você tem o controle total do caixa do seu salão... Para criar um lançamento, toque em Nova Movimentação. Escolha se é uma Entrada de receita ou uma Saída de despesa operacional... Digite a descrição, informe o valor, selecione a categoria e o meio de pagamento... Ao tocar em Salvar Lançamento, seu saldo e os relatórios de lucratividade são recalculados no mesmo instante!"
            ),
            DemoStep(
                id = "NFSE",
                title = "Plataforma NFS-e (Notas Fiscais)",
                moduleBadge = "Módulo 6 • Fiscal",
                description = "Emissão e controle de Notas Fiscais de Serviços Eletrônicas com padrão nacional da Receita Federal.",
                targetTab = "NFSE",
                stepsToAdd = listOf(
                    "Passo 1: Toque em 'Emitir Nova NFS-e' ou clique no ícone fiscal ao concluir um atendimento.",
                    "Passo 2: Confirme os dados fiscais do Tomador (Nome, CPF e E-mail carregados do cadastro).",
                    "Passo 3: Descreva os serviços prestados e verifique o código de tributação municipal.",
                    "Passo 4: Confira o Valor do Serviço, a alíquota de ISS e deduções legais.",
                    "Passo 5: Toque em 'Transmitir e Emitir NFS-e' para emitir o XML e PDF oficiais."
                ),
                formExampleTitle = "Simulação: Emissão de NFS-e",
                formExampleFields = listOf(
                    "Tomador do Serviço" to "Fernanda Santos (CPF: 234.567.890-12)",
                    "Discriminação" to "Serviços de Cabeleireiro, Corte e Hidratação",
                    "Valor dos Serviços" to "R$ 150,00",
                    "Alíquota ISS" to "3,00%",
                    "Status da Nota" to "Pronto para Transmissão na Prefeitura"
                ),
                actionButtonText = "Transmitir e Emitir NFS-e",
                narrationText = "No módulo de NFS-e, emitir as notas fiscais dos atendimentos é simples e totalmente seguro... Ao concluir um serviço ou tocar em Emitir Nova NFS-e, os dados cadastrais do cliente com CPF e e-mail já são carregados automaticamente... Você confere a descrição do serviço, a alíquota municipal de ISS e o valor... Em seguida, basta tocar em Transmitir e Emitir NFS-e para gerar o documento fiscal oficial da prefeitura!"
            ),
            DemoStep(
                id = "WHATSAPP",
                title = "WhatsApp & Automação com IA",
                moduleBadge = "Módulo 7 • Mensagens",
                description = "Comunicação inteligente, confirmações automáticas de agendamentos e campanhas com auxílio de IA.",
                targetTab = "WHATSAPP",
                stepsToAdd = listOf(
                    "Passo 1: Toque em 'Nova Mensagem' ou 'Criar Campanha' no painel do WhatsApp.",
                    "Passo 2: Selecione o Cliente destinatário ou grupo (ex: Clientes Inativos há 30 dias).",
                    "Passo 3: Escolha um Modelo Pré-definido ou toque em 'Gerar Mensagem com IA'.",
                    "Passo 4: Revise o texto personalizado com nome do cliente, serviço e data.",
                    "Passo 5: Toque em 'Enviar via WhatsApp' para abrir a conversa com mensagem pronta em 1 clique."
                ),
                formExampleTitle = "Simulação: Disparo de WhatsApp",
                formExampleFields = listOf(
                    "Destinatário" to "Maria Oliveira (11) 98765-4321",
                    "Gatilho" to "Lembrete Automático de Horário",
                    "Mensagem Sugerida" to "Olá Maria! Lembramos do seu horário de Corte hoje às 14:30 no Gold Salão. Até breve!",
                    "Status" to "Pronto para envio imediato"
                ),
                actionButtonText = "Enviar via WhatsApp",
                narrationText = "No módulo de WhatsApp, a inteligência artificial ajuda a encantar seus clientes e reduzir faltas... Para enviar uma mensagem, toque em Nova Mensagem e selecione o cliente. Você pode escolher um modelo pronto ou usar nossa inteligência artificial para criar uma mensagem calorosa e personalizada... Em seguida, toque em Enviar via WhatsApp. A conversa se abre diretamente no aplicativo oficial com o texto pronto em um único clique!"
            ),
            DemoStep(
                id = "CONFIGURANCOES",
                title = "Configurações do Salão",
                moduleBadge = "Módulo 8 • Configurações",
                description = "Ajustes de dados cadastrais, temas visuais, senha de segurança, ordem do menu e preferências gerais.",
                targetTab = "CONFIGURANCOES",
                stepsToAdd = listOf(
                    "Passo 1: Acesse a aba 'Configurações' no menu ou toque no ícone de engrenagem.",
                    "Passo 2: Atualize os dados cadastrais (Nome Fantasia, Razão Social, CNPJ e Telefone).",
                    "Passo 3: Escolha a Paleta de Cores do sistema (Rose Gold, Royal Violet, Emerald Glam, etc.).",
                    "Passo 4: Defina a Senha Mestre de Proteção para autorizar exclusões e estornos de caixa.",
                    "Passo 5: Toque em 'Salvar Configurações' para aplicar instantaneamente em todo o app."
                ),
                formExampleTitle = "Simulação: Configurações do Salão",
                formExampleFields = listOf(
                    "Nome Fantasia" to "Gold Salão & Estética",
                    "Razão Social" to "Gold Salão Beleza e Bem Estar Ltda",
                    "CNPJ" to "12.345.678/0001-90",
                    "Paleta Visual" to "Rose Gold Luxury",
                    "Senha Mestre" to "•••• (Proteção Ativa)"
                ),
                actionButtonText = "Salvar Configurações do Salão",
                narrationText = "Nas Configurações, você deixa o sistema com a identidade do seu salão... Aqui você atualiza o nome fantasia, a razão social, o CNPJ e telefones. Você pode também escolher sua paleta de cores favorita, como Rose Gold ou Royal Violet, e configurar a senha de segurança para proteger cancelamentos e o caixa... Para concluir, é só tocar em Salvar Configurações!"
            )
        )
    }

    // Initialize TTS with high-quality media audio pipeline and natural male voice detection
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("pt", "BR")

                // High fidelity speech routing (avoids low-bitrate phone call audio compression)
                try {
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                    tts?.setAudioAttributes(audioAttributes)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Scan available voices and prioritize natural humanized male Brazilian voices
                try {
                    val voices = tts?.voices?.filter { v ->
                        val lang = v.locale.language.lowercase()
                        lang == "pt" || lang.startsWith("pt")
                    }?.sortedWith(
                        compareByDescending<Voice> { v ->
                            // Prioritize high/very high quality neural synthesis models
                            v.quality
                        }.thenByDescending { v ->
                            val name = v.name.lowercase()
                            var score = 0
                            if (name.contains("network") || name.contains("neural") || name.contains("wavenet")) score += 3
                            if (name.contains("male") || name.contains("masculino") || name.contains("pt-br-x-ybd") || name.contains("pt-br-x-ptd") || name.contains("pt-br-x-afs")) score += 4
                            if (name.contains("female") || name.contains("feminino")) score -= 2
                            score
                        }
                    ) ?: emptyList()

                    availableVoices.clear()
                    availableVoices.addAll(voices)

                    // Find the best humanized male voice
                    val bestVoice = voices.firstOrNull { v ->
                        val name = v.name.lowercase()
                        name.contains("male") || name.contains("masculino") ||
                        name.contains("pt-br-x-ybd") || name.contains("pt-br-x-ptd") ||
                        name.contains("pt-br-x-afs") || (!name.contains("female") && !name.contains("feminino"))
                    } ?: voices.firstOrNull()

                    if (bestVoice != null) {
                        tts?.voice = bestVoice
                        selectedVoiceName = bestVoice.name
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Set warm, humanized pitch & smooth conversational speech rate
                tts?.setPitch(voicePitch)
                tts?.setSpeechRate(demoSpeed)
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

    // React to speed, pitch, and voice changes
    LaunchedEffect(demoSpeed, voicePitch, selectedVoiceName, ttsInitialized) {
        if (ttsInitialized && tts != null) {
            tts?.setSpeechRate(demoSpeed)
            tts?.setPitch(voicePitch)
            if (selectedVoiceName != null) {
                val voiceToApply = tts?.voices?.firstOrNull { it.name == selectedVoiceName }
                if (voiceToApply != null) {
                    tts?.voice = voiceToApply
                }
            }
        }
    }

    // Speak narration and navigate when step changes
    LaunchedEffect(currentStepIndex, isPlaying, ttsInitialized) {
        if (isDemoActive && isPlaying && ttsInitialized && currentStepIndex < steps.size) {
            val step = steps[currentStepIndex]
            onNavigateTab(step.targetTab)
            tts?.speak(step.narrationText, TextToSpeech.QUEUE_FLUSH, null, "DEMO_UTTERANCE_${step.id}")
        }
    }

    if (isDemoActive) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(horizontal = 12.dp, vertical = 14.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.90f)
                    .padding(bottom = 6.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Top Header with indicator and close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.RecordVoiceOver,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Guia Interativo com Voz Humanizada",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = "Voz Masculina Natural",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Passo ${currentStepIndex + 1} de ${steps.size} • ${steps[currentStepIndex].moduleBadge}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = onCloseDemo) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar Demonstração")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { (currentStepIndex + 1).toFloat() / steps.size },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Horizontal Quick-Jump Chips for all modules
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        steps.forEachIndexed { index, s ->
                            FilterChip(
                                selected = currentStepIndex == index,
                                onClick = { currentStepIndex = index },
                                label = {
                                    Text(
                                        text = s.moduleBadge,
                                        fontSize = 11.sp,
                                        fontWeight = if (currentStepIndex == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (currentStepIndex == index) {
                                    {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val step = steps[currentStepIndex]

                    // Scrollable content area
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Title and Description
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = step.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = step.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // STEP-BY-STEP INCLUSION GUIDE CARD
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AddCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Como Incluir Registro neste Módulo:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                step.stepsToAdd.forEach { stepText ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "•",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = stepText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // VISUAL SIMULATION OF THE REGISTRATION FORM
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.EditNote,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = step.formExampleTitle,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("Exemplo Real", fontSize = 10.sp) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Visibility,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    )
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                step.formExampleFields.forEach { (label, value) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(0.45f)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier
                                                .weight(0.55f)
                                                .border(
                                                    0.5.dp,
                                                    MaterialTheme.colorScheme.outlineVariant,
                                                    RoundedCornerShape(8.dp)
                                                )
                                        ) {
                                            Text(
                                                text = value,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Simulated Action Button
                                Button(
                                    onClick = {
                                        Toast.makeText(
                                            context,
                                            "Ação demonstrativa: No módulo real, clique em [+] para abrir este formulário!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = step.actionButtonText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        // Humanized Voice Customization Section (Expandable)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
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
                                            Icons.Default.Tune,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Ajustes da Voz Humanizada",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    TextButton(onClick = { showVoiceSettings = !showVoiceSettings }) {
                                        Text(if (showVoiceSettings) "Recolher" else "Personalizar")
                                        Icon(
                                            if (showVoiceSettings) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null
                                        )
                                    }
                                }

                                AnimatedVisibility(visible = showVoiceSettings) {
                                    Column(
                                        modifier = Modifier.padding(top = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Timbre / Tom Masculino
                                        Text(
                                            text = "Timbre & Ressonância Masculina:",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf(
                                                0.88f to "Grave Encorpado",
                                                0.93f to "Natural & Acolhedor",
                                                1.00f to "Neutro / Padrão"
                                            ).forEach { (pitch, label) ->
                                                FilterChip(
                                                    selected = voicePitch == pitch,
                                                    onClick = { voicePitch = pitch },
                                                    label = { Text(label, fontSize = 11.sp) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }

                                        // Cadência / Velocidade
                                        Text(
                                            text = "Cadência de Fala (Velocidade):",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf(
                                                0.85f to "Pausado (0.85x)",
                                                0.95f to "Conversacional (0.95x)",
                                                1.0f to "Normal (1.0x)",
                                                1.25f to "Rápido (1.25x)"
                                            ).forEach { (speed, label) ->
                                                FilterChip(
                                                    selected = demoSpeed == speed,
                                                    onClick = { onSpeedChange(speed) },
                                                    label = { Text(label, fontSize = 10.sp) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }

                                        // Available voice profiles if multiple are installed on device
                                        if (availableVoices.size > 1) {
                                            Text(
                                                text = "Perfil de Voz Detectado no Sistema:",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                availableVoices.take(4).forEachIndexed { idx, voice ->
                                                    val isSelected = selectedVoiceName == voice.name
                                                    val displayName = if (voice.name.contains("ybd", true) || voice.name.contains("male", true)) {
                                                        "Voz Masculina Neural ${idx + 1}"
                                                    } else {
                                                        "Voz Natural ${idx + 1}"
                                                    }
                                                    FilterChip(
                                                        selected = isSelected,
                                                        onClick = { selectedVoiceName = voice.name },
                                                        label = { Text(displayName, fontSize = 11.sp) }
                                                    )
                                                }
                                            }
                                        }

                                        // Test Voice Button
                                        OutlinedButton(
                                            onClick = {
                                                if (ttsInitialized) {
                                                    tts?.stop()
                                                    tts?.speak(
                                                        "Olá! Esta é a voz masculina humanizada do seu assistente Gold Salão. O tom e a cadência foram afinados para soar acolhedor e profissional.",
                                                        TextToSpeech.QUEUE_FLUSH,
                                                        null,
                                                        "TEST_VOICE"
                                                    )
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Ouvir Amostra com estes Ajustes", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Audio Playback Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Headphones,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Áudio Humanizado (${if (demoSpeed == 0.95f) "Conversacional" else "${demoSpeed}x"})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilledTonalIconButton(
                                onClick = {
                                    if (ttsInitialized) {
                                        tts?.stop()
                                        tts?.speak(
                                            step.narrationText,
                                            TextToSpeech.QUEUE_FLUSH,
                                            null,
                                            "REPLAY_${step.id}"
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Replay,
                                    contentDescription = "Repetir Narração",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bottom Navigation Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (currentStepIndex > 0) {
                                    currentStepIndex--
                                }
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
                                } else {
                                    tts?.speak(
                                        step.narrationText,
                                        TextToSpeech.QUEUE_FLUSH,
                                        null,
                                        "RESUME_${step.id}"
                                    )
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
                            Text(if (isPlaying) "Pausar" else "Ouvir")
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
