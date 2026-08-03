package com.example.ui.screens.appointments

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppointmentEntity
import com.example.data.model.ClientEntity
import com.example.data.model.NfseEntity
import com.example.data.model.ProfessionalEntity
import com.example.data.model.ServiceEntity
import com.example.ui.SalonViewModel
import com.example.ui.components.MasterPasswordAuthDialog
import com.example.ui.components.StatusBadgeChip
import com.example.utils.MaskUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    viewModel: SalonViewModel,
    appointments: List<AppointmentEntity>,
    clients: List<ClientEntity>,
    services: List<ServiceEntity>,
    professionals: List<ProfessionalEntity>,
    nfseList: List<NfseEntity> = emptyList(),
    masterPassword: String = "1234",
    isGridView: Boolean,
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendarViewMode by viewModel.calendarViewMode.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedProfFilter by viewModel.selectedProfFilter.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var selectedAppointmentToEdit by remember { mutableStateOf<AppointmentEntity?>(null) }
    var appointmentLockedToUnlock by remember { mutableStateOf<AppointmentEntity?>(null) }
    var appointmentToComplete by remember { mutableStateOf<AppointmentEntity?>(null) }
    var prefilledTimeSlot by remember { mutableStateOf("09:00") }

    fun requestEditAppointment(app: AppointmentEntity) {
        val hasNfse = nfseList.any { 
            (it.appointmentId == app.id || (it.serviceName.equals(app.serviceName, ignoreCase = true) && it.clientName.equals(app.clientName, ignoreCase = true))) && it.status == "EMITIDA"
        }
        if (hasNfse) {
            appointmentLockedToUnlock = app
        } else {
            selectedAppointmentToEdit = app
        }
    }

    // Filter appointments
    val filteredAppointments = remember(appointments, searchQuery, selectedDate, selectedProfFilter) {
        appointments.filter { app ->
            val matchesSearch = searchQuery.isBlank() ||
                    app.clientName.contains(searchQuery, ignoreCase = true) ||
                    app.serviceName.contains(searchQuery, ignoreCase = true) ||
                    app.professionalName.contains(searchQuery, ignoreCase = true) ||
                    app.paymentMethod.contains(searchQuery, ignoreCase = true)

            val matchesDate = app.date == selectedDate || calendarViewMode == "MONTH"
            val matchesProf = selectedProfFilter == null || app.professionalId == selectedProfFilter

            matchesSearch && matchesDate && matchesProf
        }
    }

    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { showBlockDialog = true },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.testTag("block_slot_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Block, contentDescription = "Bloquear Horário")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bloquear", style = MaterialTheme.typography.labelMedium)
                    }
                }

                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_appointment_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Novo Agendamento")
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Google Agenda Style Mode Chips (Dia, 3 Dias, Semana, Mês)
            Surface(
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Visão Agenda:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("DAY" to "Dia", "3DAYS" to "3 Dias", "WEEK" to "Semana", "MONTH" to "Mês").forEach { (key, label) ->
                                FilterChip(
                                    selected = calendarViewMode == key,
                                    onClick = { viewModel.setCalendarViewMode(key) },
                                    label = { Text(label, fontSize = 12.sp) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Date & Professional Filters Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Date selector
                        OutlinedButton(
                            onClick = {
                                // Toggle simple date shift for demo
                                val dateParts = selectedDate.split("-")
                                val day = dateParts.getOrNull(2)?.toIntOrNull() ?: 30
                                val nextDay = if (day >= 31) 1 else day + 1
                                val formatted = "2026-07-${if (nextDay < 10) "0$nextDay" else nextDay}"
                                viewModel.setSelectedDate(formatted)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = MaskUtils.formatDatePtBr(selectedDate),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        // Professional filter dropdown / chip
                        FilterChip(
                            selected = selectedProfFilter != null,
                            onClick = {
                                if (selectedProfFilter != null) {
                                    viewModel.setSelectedProfFilter(null)
                                } else {
                                    viewModel.setSelectedProfFilter(professionals.firstOrNull()?.id)
                                }
                            },
                            label = {
                                val profName = professionals.firstOrNull { it.id == selectedProfFilter }?.name ?: "Todos Profissionais"
                                Text(profName, maxLines = 1, fontSize = 12.sp)
                            },
                            trailingIcon = {
                                if (selectedProfFilter != null) {
                                    Icon(Icons.Default.Close, contentDescription = "Limpar filtro", modifier = Modifier.size(14.dp))
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Appointments List or Slot Grid
            if (filteredAppointments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nenhum agendamento para este filtro",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Toque no botão + para criar um novo agendamento ou bloqueio.",
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
                        items(filteredAppointments, key = { it.id }) { item ->
                            val hasNfse = nfseList.any { 
                                (it.appointmentId == item.id || (it.serviceName.equals(item.serviceName, ignoreCase = true) && it.clientName.equals(item.clientName, ignoreCase = true))) && it.status == "EMITIDA"
                            }
                            AppointmentCardItem(
                                appointment = item,
                                hasNfse = hasNfse,
                                onEdit = { requestEditAppointment(item) },
                                onDelete = {
                                    if (hasNfse) requestEditAppointment(item) else viewModel.deleteAppointment(item)
                                },
                                onDuplicate = { viewModel.duplicateAppointment(item) },
                                onCompleteAndIssueNfse = { appointmentToComplete = item },
                                onWhatsAppMsg = {
                                    val msg = viewModel.formatWhatsAppMessage("CONFIRMATION", item.clientName, item.date, item.time, item.serviceName, item.professionalName)
                                    viewModel.sendWhatsAppDirect(context, item.clientPhone, msg)
                                }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredAppointments, key = { it.id }) { item ->
                            val hasNfse = nfseList.any { 
                                (it.appointmentId == item.id || (it.serviceName.equals(item.serviceName, ignoreCase = true) && it.clientName.equals(item.clientName, ignoreCase = true))) && it.status == "EMITIDA"
                            }
                            AppointmentListItem(
                                appointment = item,
                                hasNfse = hasNfse,
                                onEdit = { requestEditAppointment(item) },
                                onDelete = {
                                    if (hasNfse) requestEditAppointment(item) else viewModel.deleteAppointment(item)
                                },
                                onDuplicate = { viewModel.duplicateAppointment(item) },
                                onCompleteAndIssueNfse = { appointmentToComplete = item },
                                onWhatsAppMsg = {
                                    val msg = viewModel.formatWhatsAppMessage("CONFIRMATION", item.clientName, item.date, item.time, item.serviceName, item.professionalName)
                                    viewModel.sendWhatsAppDirect(context, item.clientPhone, msg)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (appointmentLockedToUnlock != null) {
        MasterPasswordAuthDialog(
            masterPasswordActual = masterPassword,
            onDismiss = { appointmentLockedToUnlock = null },
            onSuccess = {
                val target = appointmentLockedToUnlock
                appointmentLockedToUnlock = null
                if (target != null) {
                    selectedAppointmentToEdit = target
                }
            }
        )
    }

    // Add Appointment Dialog
    if (showAddDialog) {
        AppointmentFormDialog(
            appointment = null,
            clients = clients,
            services = services,
            professionals = professionals,
            defaultDate = selectedDate,
            onDismiss = { showAddDialog = false },
            onSave = { newApp ->
                viewModel.addAppointment(newApp, context)
                showAddDialog = false
            }
        )
    }

    // Edit Appointment Dialog
    if (selectedAppointmentToEdit != null) {
        AppointmentFormDialog(
            appointment = selectedAppointmentToEdit,
            clients = clients,
            services = services,
            professionals = professionals,
            defaultDate = selectedDate,
            onDismiss = { selectedAppointmentToEdit = null },
            onSave = { updatedApp ->
                viewModel.updateAppointment(updatedApp)
                selectedAppointmentToEdit = null
            }
        )
    }

    // Block Schedule Dialog
    if (showBlockDialog) {
        BlockSlotDialog(
            professionals = professionals,
            defaultDate = selectedDate,
            onDismiss = { showBlockDialog = false },
            onSave = { blockApp ->
                viewModel.addAppointment(blockApp, null)
                showBlockDialog = false
            }
        )
    }

    // Complete Appointment & Select Payment Method Dialog
    if (appointmentToComplete != null) {
        CompleteAppointmentDialog(
            appointment = appointmentToComplete!!,
            onDismiss = { appointmentToComplete = null },
            onConfirmComplete = { selectedPaymentMethod ->
                viewModel.completeAndIssueNfse(appointmentToComplete!!, context, selectedPaymentMethod)
                appointmentToComplete = null
            }
        )
    }
}

@Composable
fun AppointmentListItem(
    appointment: AppointmentEntity,
    hasNfse: Boolean = false,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onCompleteAndIssueNfse: () -> Unit,
    onWhatsAppMsg: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(appointment.id, hasNfse) {
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
            // Time Badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (appointment.isBlock) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.primaryContainer
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = appointment.time,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (appointment.isBlock) Color(0xFFE65100) else MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${appointment.durationMinutes} min",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (appointment.isBlock) Color(0xFFE65100) else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Main Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (appointment.isBlock) appointment.serviceName else appointment.clientName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (hasNfse) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "NF-e Emitida - Bloqueado",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    StatusBadgeChip(status = appointment.status)
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (!appointment.isBlock) {
                    Text(
                        text = "${appointment.serviceName} • ${appointment.professionalName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = MaskUtils.formatCurrency(appointment.price),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        val payLabel = when (appointment.paymentMethod.uppercase()) {
                            "PIX" -> "⚡ Pix"
                            "DINHEIRO" -> "💵 Dinheiro"
                            "OUTROS" -> "📌 Outros"
                            else -> "💳 Cartão"
                        }
                        SuggestionChip(
                            onClick = {},
                            label = { Text(payLabel, fontSize = 10.sp) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                } else {
                    Text(
                        text = "Profissional: ${appointment.professionalName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE65100)
                    )
                }

                if (appointment.notes.isNotBlank()) {
                    Text(
                        text = "Obs: ${appointment.notes}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!appointment.isBlock) {
                        IconButton(onClick = onWhatsAppMsg, modifier = Modifier.size(32.dp)) {
                            Icon(painter = painterResource(id = R.drawable.ic_whatsapp), contentDescription = "Enviar WhatsApp", tint = Color.Unspecified, modifier = Modifier.size(18.dp))
                        }

                        IconButton(onClick = onDuplicate, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = "Copiar/Duplicar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }

                        if (appointment.status == "AGENDADO") {
                            IconButton(onClick = onCompleteAndIssueNfse, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = "Concluir & NFS-e", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (hasNfse) Icons.Default.Lock else Icons.Outlined.Edit,
                            contentDescription = if (hasNfse) "Bloqueado por NF-e" else "Editar",
                            tint = if (hasNfse) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Cancelar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentCardItem(
    appointment: AppointmentEntity,
    hasNfse: Boolean = false,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onCompleteAndIssueNfse: () -> Unit,
    onWhatsAppMsg: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(appointment.id, hasNfse) {
                detectTapGestures(
                    onDoubleTap = { onEdit() }
                )
            },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appointment.time,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hasNfse) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Bloqueado",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    StatusBadgeChip(status = appointment.status)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (appointment.isBlock) appointment.serviceName else appointment.clientName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            if (!appointment.isBlock) {
                Text(
                    text = appointment.serviceName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = appointment.professionalName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = MaskUtils.formatCurrency(appointment.price),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "BLOQUEIO DE HORÁRIO",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFE65100)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!appointment.isBlock) {
                    IconButton(onClick = onWhatsAppMsg, modifier = Modifier.size(28.dp)) {
                        Icon(painter = painterResource(id = R.drawable.ic_whatsapp), contentDescription = "Enviar WhatsApp", tint = Color.Unspecified, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDuplicate, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                    if (appointment.status == "AGENDADO") {
                        IconButton(onClick = onCompleteAndIssueNfse, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentFormDialog(
    appointment: AppointmentEntity?,
    clients: List<ClientEntity>,
    services: List<ServiceEntity>,
    professionals: List<ProfessionalEntity>,
    defaultDate: String,
    onDismiss: () -> Unit,
    onSave: (AppointmentEntity) -> Unit
) {
    var selectedClient by remember { mutableStateOf(clients.firstOrNull { it.id == appointment?.clientId } ?: clients.firstOrNull()) }
    var selectedService by remember { mutableStateOf(services.firstOrNull { it.id == appointment?.serviceId } ?: services.firstOrNull()) }
    var selectedProf by remember { mutableStateOf(professionals.firstOrNull { it.id == appointment?.professionalId } ?: professionals.firstOrNull()) }
    var date by remember { mutableStateOf(appointment?.date ?: defaultDate) }
    var time by remember { mutableStateOf(appointment?.time ?: "10:00") }
    var paymentMethod by remember { mutableStateOf(appointment?.paymentMethod ?: "CARTAO") }
    var notes by remember { mutableStateOf(appointment?.notes ?: "") }

    var expandedClientDropdown by remember { mutableStateOf(false) }
    var expandedServiceDropdown by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(
        initialHour = time.split(":").getOrNull(0)?.toIntOrNull() ?: 10,
        initialMinute = time.split(":").getOrNull(1)?.toIntOrNull() ?: 0,
        is24Hour = true
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = java.util.Calendar.getInstance()
                        cal.timeInMillis = millis
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        date = sdf.format(cal.time)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Selecionar Horário") },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val formattedHour = String.format("%02d", timePickerState.hour)
                    val formattedMin = String.format("%02d", timePickerState.minute)
                    time = "$formattedHour:$formattedMin"
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (appointment == null) "Novo Agendamento" else "Editar Agendamento") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Client Dropdown Autocomplete
                ExposedDropdownMenuBox(
                    expanded = expandedClientDropdown,
                    onExpandedChange = { expandedClientDropdown = !expandedClientDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedClient?.name ?: "Selecione o Cliente",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cliente *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClientDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("appointment_client_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = expandedClientDropdown,
                        onDismissRequest = { expandedClientDropdown = false }
                    ) {
                        clients.forEach { client ->
                            DropdownMenuItem(
                                text = { Text("${client.name} (${MaskUtils.formatPhone(client.phone)})") },
                                onClick = {
                                    selectedClient = client
                                    expandedClientDropdown = false
                                }
                            )
                        }
                    }
                }

                // Service Dropdown Autocomplete
                ExposedDropdownMenuBox(
                    expanded = expandedServiceDropdown,
                    onExpandedChange = { expandedServiceDropdown = !expandedServiceDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedService?.let { "${it.name} - ${MaskUtils.formatCurrency(it.price)}" } ?: "Selecione o Serviço",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Serviço *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedServiceDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("appointment_service_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = expandedServiceDropdown,
                        onDismissRequest = { expandedServiceDropdown = false }
                    ) {
                        services.forEach { service ->
                            DropdownMenuItem(
                                text = { Text("${service.name} • ${MaskUtils.formatCurrency(service.price)} (${service.durationMinutes} min)") },
                                onClick = {
                                    selectedService = service
                                    expandedServiceDropdown = false
                                }
                            )
                        }
                    }
                }

                // Professional Selector (Required & Visible)
                Column {
                    Text("Profissional Responsável *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        professionals.forEach { prof ->
                            FilterChip(
                                selected = selectedProf?.id == prof.id,
                                onClick = { selectedProf = prof },
                                label = { Text(prof.name.split(" ").firstOrNull() ?: prof.name) },
                                leadingIcon = if (selectedProf?.id == prof.id) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }

                // Payment Method
                Column {
                    Text("Forma de Pagamento", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        listOf("CARTAO" to "Cartão", "PIX" to "Pix", "DINHEIRO" to "Dinheiro", "OUTROS" to "Outros").forEachIndexed { index, (key, label) ->
                            SegmentedButton(
                                selected = paymentMethod == key,
                                onClick = { paymentMethod = key },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = 4)
                            ) {
                                Text(label, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Date & Time Native Picker Triggers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = MaskUtils.formatDatePtBr(date),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Data *") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Selecionar Data")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showDatePicker = true }
                    )

                    OutlinedTextField(
                        value = time,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Horário *") },
                        trailingIcon = {
                            IconButton(onClick = { showTimePicker = true }) {
                                Icon(Icons.Default.Schedule, contentDescription = "Selecionar Horário")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showTimePicker = true }
                    )
                }

                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações do Agendamento") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedClient != null && selectedService != null && selectedProf != null) {
                        val app = AppointmentEntity(
                            id = appointment?.id ?: 0,
                            clientId = selectedClient!!.id,
                            clientName = selectedClient!!.name,
                            clientPhone = selectedClient!!.phone,
                            serviceId = selectedService!!.id,
                            serviceName = selectedService!!.name,
                            professionalId = selectedProf!!.id,
                            professionalName = selectedProf!!.name,
                            date = date,
                            time = time,
                            durationMinutes = selectedService!!.durationMinutes,
                            price = selectedService!!.price,
                            status = appointment?.status ?: "AGENDADO",
                            paymentMethod = paymentMethod,
                            notes = notes
                        )
                        onSave(app)
                    }
                },
                enabled = selectedClient != null && selectedService != null && selectedProf != null,
                modifier = Modifier.testTag("save_appointment_button")
            ) {
                Text("Salvar Agendamento")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun BlockSlotDialog(
    professionals: List<ProfessionalEntity>,
    defaultDate: String,
    onDismiss: () -> Unit,
    onSave: (AppointmentEntity) -> Unit
) {
    var selectedProf by remember { mutableStateOf(professionals.firstOrNull()) }
    var reason by remember { mutableStateOf("Intervalo / Almoço / Manutenção") }
    var time by remember { mutableStateOf("12:00") }
    var duration by remember { mutableStateOf("60") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bloquear Horário de Agenda") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Selecione o Profissional:")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    professionals.forEach { prof ->
                        FilterChip(
                            selected = selectedProf?.id == prof.id,
                            onClick = { selectedProf = prof },
                            label = { Text(prof.name.split(" ").firstOrNull() ?: prof.name) }
                        )
                    }
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo do Bloqueio") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Horário") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duração (min)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedProf != null) {
                        val blockApp = AppointmentEntity(
                            clientId = 0,
                            clientName = "BLOQUEIO DE AGENDA",
                            clientPhone = "",
                            serviceId = 0,
                            serviceName = reason,
                            professionalId = selectedProf!!.id,
                            professionalName = selectedProf!!.name,
                            date = defaultDate,
                            time = time,
                            durationMinutes = duration.toIntOrNull() ?: 60,
                            price = 0.0,
                            status = "BLOQUEIO",
                            isBlock = true,
                            notes = reason
                        )
                        onSave(blockApp)
                    }
                },
                modifier = Modifier.testTag("save_block_button")
            ) {
                Text("Confirmar Bloqueio")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun CompleteAppointmentDialog(
    appointment: AppointmentEntity,
    onDismiss: () -> Unit,
    onConfirmComplete: (String) -> Unit
) {
    var selectedPaymentMethod by remember { mutableStateOf(if (appointment.paymentMethod.isNotBlank()) appointment.paymentMethod else "CARTAO") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Concluir Atendimento & NFS-e", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Cliente: ${appointment.clientName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("Serviço: ${appointment.serviceName}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Valor Total: ${MaskUtils.formatCurrency(appointment.price)}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Selecione a Forma de Pagamento:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                val methods = listOf(
                    "CARTAO" to "💳 Cartão (Crédito/Débito)",
                    "PIX" to "⚡ Pix (QR Code/Chave)",
                    "DINHEIRO" to "💵 Dinheiro (Espécie)",
                    "OUTROS" to "📌 Outros Meios"
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    methods.forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPaymentMethod = key }
                                .background(
                                    if (selectedPaymentMethod == key) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedPaymentMethod == key,
                                onClick = { selectedPaymentMethod = key }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                val isEligible = selectedPaymentMethod.uppercase() in listOf("CARTAO", "PIX", "CARTÃO")
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isEligible) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isEligible) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isEligible) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEligible)
                                "NFS-e Automática: Pagamento via Cartão/Pix será enviado à plataforma para emissão de Nota Fiscal."
                            else
                                "Sem Emissão Fiscal: Pagamentos em Dinheiro/Outros NÃO serão enviados para emissão de nota.",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isEligible) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmComplete(selectedPaymentMethod) },
                modifier = Modifier.testTag("confirm_completion_button")
            ) {
                Text("Confirmar Conclusão")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
