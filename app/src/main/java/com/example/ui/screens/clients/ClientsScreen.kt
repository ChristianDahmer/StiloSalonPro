package com.example.ui.screens.clients

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.PersonAdd
import com.example.data.model.ClientEntity
import com.example.ui.SalonViewModel
import com.example.ui.components.MaskedOutlinedTextField
import com.example.utils.MaskUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    viewModel: SalonViewModel,
    clients: List<ClientEntity>,
    isGridView: Boolean,
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var selectedBirthMonth by remember { mutableStateOf<Int?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var clientToEdit by remember { mutableStateOf<ClientEntity?>(null) }

    val handleCopyClient: (ClientEntity) -> Unit = { client ->
        clipboardManager.setText(
            AnnotatedString("Nome: ${client.name}\nCPF: ${client.cpf}\nTelefone: ${client.phone}\nE-mail: ${client.email}\nNascimento: ${client.birthDate}")
        )
        clientToEdit = client.copy(id = 0, name = "${client.name} (Cópia)")
    }

    val filteredClients = remember(clients, searchQuery, selectedBirthMonth) {
        clients.filter { client ->
            val matchesSearch = searchQuery.isBlank() ||
                    client.name.contains(searchQuery, ignoreCase = true) ||
                    client.cpf.contains(searchQuery) ||
                    client.phone.contains(searchQuery)

            val matchesMonth = if (selectedBirthMonth != null) {
                val parts = client.birthDate.split("-")
                val month = parts.getOrNull(1)?.toIntOrNull()
                month == selectedBirthMonth
            } else true

            matchesSearch && matchesMonth
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_client_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo Cliente")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Birth Month Filter Chips
            Surface(
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Aniversariantes:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)

                    val months = listOf(
                        null to "Todos",
                        1 to "Janeiro",
                        2 to "Fevereiro",
                        3 to "Março",
                        4 to "Abril",
                        5 to "Maio",
                        6 to "Junho",
                        7 to "Julho",
                        8 to "Agosto",
                        9 to "Setembro",
                        10 to "Outubro",
                        11 to "Novembro",
                        12 to "Dezembro"
                    )

                    months.forEach { (m, name) ->
                        FilterChip(
                            selected = selectedBirthMonth == m,
                            onClick = { selectedBirthMonth = m },
                            label = { Text(name, fontSize = 11.sp, maxLines = 1, softWrap = false) },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (filteredClients.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.People,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Nenhum cliente cadastrado",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Comece adicionando seu primeiro cliente para gerenciar agendamentos.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    clientToEdit = null
                                    showAddDialog = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cadastrar Cliente", fontSize = 13.sp)
                            }
                        }
                    }
                }
            } else {
                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 2.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredClients, key = { it.id }) { client ->
                            ClientGridCard(
                                client = client,
                                onCopy = { handleCopyClient(client) },
                                onEdit = { clientToEdit = client },
                                onDelete = { viewModel.deleteClient(client) },
                                onWhatsAppBirthday = {
                                    viewModel.sendWhatsAppDirect(
                                        context,
                                        client.phone,
                                        "Olá ${client.name}! 🎂 A equipe do Salão te deseja um Feliz Aniversário! Para comemorar, preparamos um cupom especial de 20% de desconto em qualquer serviço neste mês. Venha celebrar com a gente! ✨"
                                    )
                                }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 2.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredClients, key = { it.id }) { client ->
                            ClientListItem(
                                client = client,
                                onCopy = { handleCopyClient(client) },
                                onEdit = { clientToEdit = client },
                                onDelete = { viewModel.deleteClient(client) },
                                onWhatsAppBirthday = {
                                    viewModel.sendWhatsAppDirect(
                                        context,
                                        client.phone,
                                        "Olá ${client.name}! 🎂 A equipe do Salão te deseja um Feliz Aniversário! Para comemorar, preparamos um cupom especial de 20% de desconto em qualquer serviço neste mês. Venha celebrar com a gente! ✨"
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        ClientFormDialog(
            client = null,
            onDismiss = { showAddDialog = false },
            onSave = { newClient ->
                viewModel.addClient(newClient)
                showAddDialog = false
            }
        )
    }

    if (clientToEdit != null) {
        ClientFormDialog(
            client = clientToEdit,
            onDismiss = { clientToEdit = null },
            onSave = { updated ->
                if (updated.id == 0L) {
                    viewModel.addClient(updated)
                } else {
                    viewModel.updateClient(updated)
                }
                clientToEdit = null
            }
        )
    }
}

@Composable
fun ClientListItem(
    client: ClientEntity,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onWhatsAppBirthday: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(client.id) {
                detectTapGestures(
                    onDoubleTap = { onEdit() }
                )
            },
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = client.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "CPF: ${client.cpf} • 📞 ${client.phone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = "✉️ ${client.email} • 🎂 ${MaskUtils.formatDatePtBr(client.birthDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onWhatsAppBirthday, modifier = Modifier.size(26.dp)) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = "Enviar Cupom de Aniversário via WhatsApp",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(15.dp)
                    )
                }
                IconButton(onClick = onCopy, modifier = Modifier.size(26.dp)) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = "Copiar Cliente",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Editar", modifier = Modifier.size(15.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}

@Composable
fun ClientGridCard(
    client: ClientEntity,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onWhatsAppBirthday: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(client.id) {
                detectTapGestures(
                    onDoubleTap = { onEdit() }
                )
            },
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = client.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = client.phone,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "CPF: ${client.cpf}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1
            )
            Text(
                text = "🎂 ${MaskUtils.formatDatePtBr(client.birthDate)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onWhatsAppBirthday, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = "Cupom Aniversário",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(14.dp)
                    )
                }
                IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = "Copiar Cliente",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}
 
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientFormDialog(
    client: ClientEntity?,
    onDismiss: () -> Unit,
    onSave: (ClientEntity) -> Unit
) {
    var name by remember { mutableStateOf(client?.name ?: "") }
    var cpf by remember { mutableStateOf(client?.cpf ?: "") }
    var phone by remember { mutableStateOf(client?.phone ?: "") }
    var email by remember { mutableStateOf(client?.email ?: "") }
    var birthDate by remember { mutableStateOf(client?.birthDate ?: "1995-05-15") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                if (client == null) "Novo Cliente" else "Editar Cliente",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Fechar")
                            }
                        },
                        actions = {
                            Button(
                                onClick = {
                                    val newC = ClientEntity(
                                        id = client?.id ?: 0,
                                        name = name,
                                        cpf = cpf,
                                        phone = phone,
                                        email = email,
                                        birthDate = birthDate
                                    )
                                    onSave(newC)
                                },
                                enabled = name.isNotBlank() && phone.isNotBlank(),
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .testTag("save_client_button"),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("Salvar")
                            }
                        }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Informações do Cliente",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome Completo *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    MaskedOutlinedTextField(
                        value = cpf,
                        onValueChange = { cpf = it },
                        label = "CPF",
                        maskType = "CPF",
                        isRequired = true
                    )

                    MaskedOutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Telefone com WhatsApp",
                        maskType = "PHONE",
                        isRequired = true
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-mail") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = { birthDate = it },
                        label = { Text("Data de Nascimento (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
