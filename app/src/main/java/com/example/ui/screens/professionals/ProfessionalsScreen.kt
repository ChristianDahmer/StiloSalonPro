package com.example.ui.screens.professionals

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
import com.example.data.model.ProfessionalEntity
import com.example.ui.SalonViewModel
import com.example.ui.components.MaskedOutlinedTextField
import com.example.utils.MaskUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalsScreen(
    viewModel: SalonViewModel,
    professionals: List<ProfessionalEntity>,
    isGridView: Boolean,
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var profToEdit by remember { mutableStateOf<ProfessionalEntity?>(null) }

    val filteredProfessionals = remember(professionals, searchQuery) {
        professionals.filter { prof ->
            searchQuery.isBlank() ||
                    prof.name.contains(searchQuery, ignoreCase = true) ||
                    prof.specialty.contains(searchQuery, ignoreCase = true) ||
                    prof.phone.contains(searchQuery)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_professional_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo Profissional")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (filteredProfessionals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Badge,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nenhum profissional encontrado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Toque no botão + para cadastrar novos colaboradores.",
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
                        items(filteredProfessionals, key = { it.id }) { prof ->
                            ProfessionalGridCard(
                                professional = prof,
                                onEdit = { profToEdit = prof },
                                onDelete = { viewModel.deleteProfessional(prof) },
                                onDuplicate = { viewModel.duplicateProfessional(prof) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredProfessionals, key = { it.id }) { prof ->
                            ProfessionalListItem(
                                professional = prof,
                                onEdit = { profToEdit = prof },
                                onDelete = { viewModel.deleteProfessional(prof) },
                                onDuplicate = { viewModel.duplicateProfessional(prof) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        ProfessionalFormDialog(
            professional = null,
            onDismiss = { showAddDialog = false },
            onSave = { newProf ->
                viewModel.addProfessional(newProf)
                showAddDialog = false
            }
        )
    }

    if (profToEdit != null) {
        ProfessionalFormDialog(
            professional = profToEdit,
            onDismiss = { profToEdit = null },
            onSave = { updatedProf ->
                viewModel.updateProfessional(updatedProf)
                profToEdit = null
            }
        )
    }
}

@Composable
fun ProfessionalListItem(
    professional: ProfessionalEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(professional.id) {
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
                    text = professional.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = professional.specialty,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "📞 ${professional.phone} • ✉️ ${professional.email}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Escala: ${professional.workDays} (${professional.startHour} às ${professional.endHour})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Row {
                IconButton(onClick = onDuplicate) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = "Duplicar Perfil/Escala", tint = MaterialTheme.colorScheme.primary)
                }
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
fun ProfessionalGridCard(
    professional: ProfessionalEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(professional.id) {
                detectTapGestures(
                    onDoubleTap = { onEdit() }
                )
            },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = professional.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = professional.specialty,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "📞 ${professional.phone}",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
            Text(
                text = "${professional.startHour} - ${professional.endHour}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onDuplicate, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
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
fun ProfessionalFormDialog(
    professional: ProfessionalEntity?,
    onDismiss: () -> Unit,
    onSave: (ProfessionalEntity) -> Unit
) {
    var name by remember { mutableStateOf(professional?.name ?: "") }
    var specialty by remember { mutableStateOf(professional?.specialty ?: "Hairstylist") }
    var phone by remember { mutableStateOf(professional?.phone ?: "") }
    var email by remember { mutableStateOf(professional?.email ?: "") }
    var workDays by remember { mutableStateOf(professional?.workDays ?: "Seg,Ter,Qua,Qui,Sex,Sáb") }
    var startHour by remember { mutableStateOf(professional?.startHour ?: "08:00") }
    var endHour by remember { mutableStateOf(professional?.endHour ?: "18:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (professional == null) "Novo Profissional" else "Editar Profissional") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome Completo *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = specialty,
                    onValueChange = { specialty = it },
                    label = { Text("Especialidade *") },
                    modifier = Modifier.fillMaxWidth()
                )

                MaskedOutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Telefone",
                    maskType = "PHONE",
                    isRequired = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail Profissional") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = workDays,
                    onValueChange = { workDays = it },
                    label = { Text("Dias de Atendimento") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startHour,
                        onValueChange = { startHour = it },
                        label = { Text("Início (HH:mm)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endHour,
                        onValueChange = { endHour = it },
                        label = { Text("Fim (HH:mm)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newProf = ProfessionalEntity(
                        id = professional?.id ?: 0,
                        name = name,
                        specialty = specialty,
                        phone = phone,
                        email = email,
                        workDays = workDays,
                        startHour = startHour,
                        endHour = endHour
                    )
                    onSave(newProf)
                },
                enabled = name.isNotBlank() && specialty.isNotBlank(),
                modifier = Modifier.testTag("save_professional_button")
            ) {
                Text("Salvar Colaborador")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
