package com.example.ui.screens.services

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.NfseEntity
import com.example.data.model.ProfessionalEntity
import com.example.data.model.ServiceEntity
import com.example.ui.SalonViewModel
import com.example.ui.components.MasterPasswordAuthDialog
import com.example.utils.MaskUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    viewModel: SalonViewModel,
    services: List<ServiceEntity>,
    professionals: List<ProfessionalEntity>,
    nfseList: List<NfseEntity> = emptyList(),
    masterPassword: String = "1234",
    isGridView: Boolean,
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var serviceToEdit by remember { mutableStateOf<ServiceEntity?>(null) }
    var serviceLockedToUnlock by remember { mutableStateOf<ServiceEntity?>(null) }

    fun requestEditService(service: ServiceEntity) {
        val hasNfseIssued = nfseList.any { it.serviceName.equals(service.name, ignoreCase = true) && it.status == "EMITIDA" }
        if (hasNfseIssued) {
            serviceLockedToUnlock = service
        } else {
            serviceToEdit = service
        }
    }

    val categories = remember(services) {
        listOf("Todas") + services.map { it.category }.distinct()
    }

    val filteredServices = remember(services, searchQuery, selectedCategoryFilter) {
        services.filter { service ->
            val matchesSearch = searchQuery.isBlank() ||
                    service.name.contains(searchQuery, ignoreCase = true) ||
                    service.description.contains(searchQuery, ignoreCase = true)

            val matchesCategory = selectedCategoryFilter == null || selectedCategoryFilter == "Todas" || service.category == selectedCategoryFilter

            matchesSearch && matchesCategory
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_service_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo Serviço")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category Filter Chips
            Surface(
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Categoria:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    categories.take(4).forEach { cat ->
                        FilterChip(
                            selected = (selectedCategoryFilter == cat) || (cat == "Todas" && selectedCategoryFilter == null),
                            onClick = { selectedCategoryFilter = if (cat == "Todas") null else cat },
                            label = { Text(cat, fontSize = 12.sp, maxLines = 1, softWrap = false) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (filteredServices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.MedicalServices,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nenhum serviço cadastrado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Clique em + para adicionar novos serviços ao salão.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredServices, key = { it.id }) { service ->
                            val hasNfse = nfseList.any { it.serviceName.equals(service.name, ignoreCase = true) && it.status == "EMITIDA" }
                            ServiceGridCard(
                                service = service,
                                hasNfse = hasNfse,
                                onEdit = { requestEditService(service) },
                                onDelete = {
                                    if (hasNfse) requestEditService(service) else viewModel.deleteService(service)
                                },
                                onDuplicate = { viewModel.duplicateService(service) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredServices, key = { it.id }) { service ->
                            val hasNfse = nfseList.any { it.serviceName.equals(service.name, ignoreCase = true) && it.status == "EMITIDA" }
                            ServiceListItem(
                                service = service,
                                hasNfse = hasNfse,
                                onEdit = { requestEditService(service) },
                                onDelete = {
                                    if (hasNfse) requestEditService(service) else viewModel.deleteService(service)
                                },
                                onDuplicate = { viewModel.duplicateService(service) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        ServiceFormDialog(
            service = null,
            professionals = professionals,
            onDismiss = { showAddDialog = false },
            onSave = { newService ->
                viewModel.addService(newService)
                showAddDialog = false
            }
        )
    }

    if (serviceToEdit != null) {
        ServiceFormDialog(
            service = serviceToEdit,
            professionals = professionals,
            onDismiss = { serviceToEdit = null },
            onSave = { updated ->
                viewModel.updateService(updated)
                serviceToEdit = null
            }
        )
    }

    if (serviceLockedToUnlock != null) {
        MasterPasswordAuthDialog(
            masterPasswordActual = masterPassword,
            onDismiss = { serviceLockedToUnlock = null },
            onSuccess = {
                val target = serviceLockedToUnlock
                serviceLockedToUnlock = null
                if (target != null) {
                    serviceToEdit = target
                }
            }
        )
    }
}

@Composable
fun ServiceListItem(
    service: ServiceEntity,
    hasNfse: Boolean = false,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(service.id, hasNfse) {
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = service.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        if (hasNfse) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Nota Emitida - Bloqueado",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    SuggestionChip(
                        onClick = {},
                        label = { Text(service.category, fontSize = 10.sp) },
                        modifier = Modifier.height(20.dp)
                    )
                }

                if (service.description.isNotBlank()) {
                    Text(
                        text = service.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = MaskUtils.formatCurrency(service.price),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "⏱ ${service.durationMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (service.taxCode.isNotBlank()) {
                        Text(
                            text = "LC 116: ${service.taxCode}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDuplicate, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = "Copiar/Duplicar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(26.dp)) {
                    Icon(
                        imageVector = if (hasNfse) Icons.Default.Lock else Icons.Outlined.Edit,
                        contentDescription = if (hasNfse) "Bloqueado por NF-e" else "Editar",
                        tint = if (hasNfse) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(15.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}

@Composable
fun ServiceGridCard(
    service: ServiceEntity,
    hasNfse: Boolean = false,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(service.id, hasNfse) {
                detectTapGestures(
                    onDoubleTap = { onEdit() }
                )
            },
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                if (hasNfse) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Bloqueado por NF-e",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Text(
                text = service.category,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = MaskUtils.formatCurrency(service.price),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${service.durationMinutes}m",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onDuplicate, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (hasNfse) Icons.Default.Lock else Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = if (hasNfse) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(14.dp)
                    )
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
fun ServiceFormDialog(
    service: ServiceEntity?,
    professionals: List<ProfessionalEntity>,
    onDismiss: () -> Unit,
    onSave: (ServiceEntity) -> Unit
) {
    var name by remember { mutableStateOf(service?.name ?: "") }
    var description by remember { mutableStateOf(service?.description ?: "") }
    var price by remember { mutableStateOf(service?.price?.toString() ?: "100.0") }
    var duration by remember { mutableStateOf(service?.durationMinutes?.toString() ?: "60") }
    var category by remember { mutableStateOf(service?.category ?: "Cabelo") }
    var taxCode by remember { mutableStateOf(service?.taxCode ?: "06.01") }

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
                                if (service == null) "Novo Serviço" else "Editar Serviço",
                                fontWeight = FontWeight.Bold
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
                                    val newServ = ServiceEntity(
                                        id = service?.id ?: 0,
                                        name = name,
                                        description = description,
                                        price = price.toDoubleOrNull() ?: 100.0,
                                        durationMinutes = duration.toIntOrNull() ?: 60,
                                        category = category,
                                        taxCode = taxCode
                                    )
                                    onSave(newServ)
                                },
                                enabled = name.isNotBlank() && price.isNotBlank(),
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .testTag("save_service_button")
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
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Informações do Serviço",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome do Serviço *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrição Detalhada") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Valor (R$) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = { Text("Duração (min) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Categoria (Cabelo, Unhas, Estética...)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = taxCode,
                        onValueChange = { taxCode = it },
                        label = { Text("Código LC 116 / Tributação NFS-e") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
