package com.example.ui.screens.clients

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var selectedBirthMonth by remember { mutableStateOf<Int?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var clientToEdit by remember { mutableStateOf<ClientEntity?>(null) }

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
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Aniversariantes:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                    val months = listOf(
                        null to "Todos",
                        7 to "Julho",
                        8 to "Agosto",
                        11 to "Novembro"
                    )

                    months.forEach { (m, name) ->
                        FilterChip(
                            selected = selectedBirthMonth == m,
                            onClick = { selectedBirthMonth = m },
                            label = { Text(name, fontSize = 12.sp) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredClients.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.People,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nenhum cliente cadastrado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Toque em + para adicionar novos clientes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredClients, key = { it.id }) { client ->
                            ClientGridCard(
                                client = client,
                                onEdit = { clientToEdit = client },
                                onDelete = { viewModel.deleteClient(client) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredClients, key = { it.id }) { client ->
                            ClientListItem(
                                client = client,
                                onEdit = { clientToEdit = client },
                                onDelete = { viewModel.deleteClient(client) }
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
                viewModel.updateClient(updated)
                clientToEdit = null
            }
        )
    }
}

@Composable
fun ClientListItem(
    client: ClientEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(client.id) {
                detectTapGestures(
                    onDoubleTap = { onEdit() }
                )
            },
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = client.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "CPF: ${client.cpf} • 📞 ${client.phone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "✉️ ${client.email} • 🎂 ${MaskUtils.formatDatePtBr(client.birthDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun ClientGridCard(
    client: ClientEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(client.id) {
                detectTapGestures(
                    onDoubleTap = { onEdit() }
                )
            },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = client.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = client.phone,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "CPF: ${client.cpf}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "🎂 ${MaskUtils.formatDatePtBr(client.birthDate)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (client == null) "Novo Cliente" else "Editar Cliente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome Completo *") },
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
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    label = { Text("Data de Nascimento (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
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
                modifier = Modifier.testTag("save_client_button")
            ) {
                Text("Salvar Cliente")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
