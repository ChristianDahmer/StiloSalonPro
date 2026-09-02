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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.example.ui.screens.clients.ClientFormDialog
import com.example.ui.screens.services.ServiceFormDialog
import com.example.ui.components.MasterPasswordAuthDialog
import com.example.ui.components.StatusBadgeChip
import com.example.utils.MaskUtils
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.util.Calendar
import java.util.Locale
import java.util.Date
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    val haptic = LocalHapticFeedback.current
    var showQuickClientDialog by remember { mutableStateOf(false) }
    val calendarViewMode by viewModel.calendarViewMode.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedProfFilter by viewModel.selectedProfFilter.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
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
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedAppointmentToEdit = null
                    prefilledTimeSlot = "09:00"
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("add_appointment_fab")
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Novo Agendamento",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Google Agenda Style Mode Chips (Dia, Semana, Quinzena, Mês, Ano)
            Surface(
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            "DAY" to "Dia",
                            "WEEK" to "Semana",
                            "FORTNIGHT" to "Quinzena",
                            "MONTH" to "Mês",
                            "YEAR" to "Ano"
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = calendarViewMode == key,
                                onClick = { viewModel.setCalendarViewMode(key) },
                                label = { Text(label, fontSize = 11.sp) },
                                modifier = Modifier.height(26.dp)
                            )
                        }
                    }

                    // Date Navigation Row (compact spacing between Hoje and date)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val newDate = when (calendarViewMode) {
                                    "DAY" -> CalendarHelper.addDays(selectedDate, -1)
                                    "WEEK" -> CalendarHelper.addDays(selectedDate, -7)
                                    "FORTNIGHT" -> CalendarHelper.addDays(selectedDate, -15)
                                    "MONTH" -> CalendarHelper.addMonths(selectedDate, -1)
                                    "YEAR" -> CalendarHelper.addYears(selectedDate, -1)
                                    else -> CalendarHelper.addDays(selectedDate, -1)
                                }
                                viewModel.setSelectedDate(newDate)
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Anterior", modifier = Modifier.size(16.dp))
                        }

                        Button(
                            onClick = {
                                viewModel.setSelectedDate(MaskUtils.getCurrentDateIso())
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 1.dp),
                            modifier = Modifier.height(26.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Text("Hoje", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = {
                                val newDate = when (calendarViewMode) {
                                    "DAY" -> CalendarHelper.addDays(selectedDate, 1)
                                    "WEEK" -> CalendarHelper.addDays(selectedDate, 7)
                                    "FORTNIGHT" -> CalendarHelper.addDays(selectedDate, 15)
                                    "MONTH" -> CalendarHelper.addMonths(selectedDate, 1)
                                    "YEAR" -> CalendarHelper.addYears(selectedDate, 1)
                                    else -> CalendarHelper.addDays(selectedDate, 1)
                                }
                                viewModel.setSelectedDate(newDate)
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Próximo", modifier = Modifier.size(16.dp))
                        }

                        Spacer(modifier = Modifier.width(2.dp))

                        Text(
                            text = when (calendarViewMode) {
                                "MONTH" -> CalendarHelper.getMonthLabel(selectedDate)
                                "YEAR" -> CalendarHelper.getYearLabel(selectedDate)
                                else -> MaskUtils.formatDatePtBr(selectedDate)
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Dashboard Banner (Compact)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Celebration, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(15.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Gold Salão",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            val totalToday = appointments.filter { it.date == selectedDate && !it.isBlock }.size
                            Text(
                                text = "$totalToday agendamento(s) no período.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Calendar Content based on selected mode
            when (calendarViewMode) {
                "DAY" -> {
                    val hours = listOf("08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00")
                    if (isGridView) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 2.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(hours) { hour ->
                                val appsInHour = filteredAppointments.filter { app ->
                                    app.date == selectedDate && app.time.substringBefore(":") == hour.substringBefore(":")
                                }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (appsInHour.isNotEmpty()) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = hour,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        if (appsInHour.isNotEmpty()) {
                                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                appsInHour.forEach { app ->
                                                    val hasNfse = nfseList.any { 
                                                        (it.appointmentId == app.id || (it.serviceName.equals(app.serviceName, ignoreCase = true) && it.clientName.equals(app.clientName, ignoreCase = true))) && it.status == "EMITIDA"
                                                    }
                                                    AppointmentCardItem(
                                                        appointment = app,
                                                        hasNfse = hasNfse,
                                                        onEdit = { requestEditAppointment(app) },
                                                        onDelete = {
                                                             if (hasNfse) requestEditAppointment(app) else viewModel.deleteAppointment(app)
                                                        },
                                                        onDuplicate = { viewModel.duplicateAppointment(app) },
                                                        onCompleteAndIssueNfse = { appointmentToComplete = app },
                                                        onWhatsAppMsg = {
                                                            val msg = viewModel.formatWhatsAppMessage("CONFIRMATION", app.clientName, app.date, app.time, app.serviceName, app.professionalName)
                                                            viewModel.sendWhatsAppDirect(context, app.clientPhone, msg)
                                                        }
                                                    )
                                                }
                                            }
                                        } else {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        prefilledTimeSlot = hour
                                                        showAddDialog = true
                                                    }
                                                    .background(
                                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                                        shape = RoundedCornerShape(6.dp)
                                                    )
                                                    .border(
                                                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                                        shape = RoundedCornerShape(6.dp)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AddCircleOutline,
                                                    contentDescription = "Agendar",
                                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "Livre",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 2.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            items(hours) { hour ->
                                val appsInHour = filteredAppointments.filter { app ->
                                    app.date == selectedDate && app.time.substringBefore(":") == hour.substringBefore(":")
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Left side: Hour
                                    Column(
                                        modifier = Modifier
                                            .width(46.dp)
                                            .padding(top = 2.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = hour,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(24.dp)
                                                .background(MaterialTheme.colorScheme.outlineVariant)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    // Right side: Appointment card or Quick Book slot
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (appsInHour.isNotEmpty()) {
                                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                appsInHour.forEach { app ->
                                                    val hasNfse = nfseList.any { 
                                                        (it.appointmentId == app.id || (it.serviceName.equals(app.serviceName, ignoreCase = true) && it.clientName.equals(app.clientName, ignoreCase = true))) && it.status == "EMITIDA"
                                                    }
                                                    AppointmentListItem(
                                                        appointment = app,
                                                        hasNfse = hasNfse,
                                                        onEdit = { requestEditAppointment(app) },
                                                        onDelete = {
                                                            if (hasNfse) requestEditAppointment(app) else viewModel.deleteAppointment(app)
                                                        },
                                                        onDuplicate = { viewModel.duplicateAppointment(app) },
                                                        onCompleteAndIssueNfse = { appointmentToComplete = app },
                                                        onWhatsAppMsg = {
                                                            val msg = viewModel.formatWhatsAppMessage("CONFIRMATION", app.clientName, app.date, app.time, app.serviceName, app.professionalName)
                                                            viewModel.sendWhatsAppDirect(context, app.clientPhone, msg)
                                                        }
                                                    )
                                                }
                                            }
                                        } else {
                                            // Quick scheduling slot for this hour
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        prefilledTimeSlot = hour
                                                        showAddDialog = true
                                                    }
                                                    .background(
                                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .border(
                                                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AddCircleOutline,
                                                    contentDescription = "Agendar",
                                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Livre — Toque para agendar",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "WEEK", "FORTNIGHT" -> {
                    val daysList = if (calendarViewMode == "WEEK") {
                        CalendarHelper.getDaysOfWeek(selectedDate)
                    } else {
                        CalendarHelper.getDaysOfFortnight(selectedDate)
                    }

                    if (isGridView) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 2.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(daysList) { day ->
                                val dateIso = CalendarHelper.formatIso(day)
                                val appsForDay = appointments.filter { app ->
                                    app.date == dateIso && (selectedProfFilter == null || app.professionalId == selectedProfFilter)
                                }
                                val isSelected = dateIso == selectedDate

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                                    ),
                                    border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.setSelectedDate(dateIso)
                                                    viewModel.setCalendarViewMode("DAY")
                                                }
                                                .padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Icon(
                                                    imageVector = Icons.Default.Today,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${CalendarHelper.getDayOfMonthLabel(day)} (${CalendarHelper.getDayOfWeekLabel(day).substring(0, 3)})",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1
                                                )
                                            }

                                            Text(
                                                text = "${appsForDay.size}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Quick slots for key hours
                                        val quickHours = listOf("09:00", "12:00", "15:00", "18:00")
                                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                            quickHours.forEach { hour ->
                                                val matchApp = appsForDay.find { it.time.substringBefore(":") == hour.substringBefore(":") }
                                                if (matchApp != null) {
                                                    SuggestionChip(
                                                        onClick = { requestEditAppointment(matchApp) },
                                                        label = { Text("🔒 $hour: ${matchApp.clientName.split(" ").firstOrNull()}", fontSize = 10.sp) },
                                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                        ),
                                                        modifier = Modifier.fillMaxWidth().height(24.dp)
                                                    )
                                                } else {
                                                    SuggestionChip(
                                                        onClick = {
                                                            viewModel.setSelectedDate(dateIso)
                                                            prefilledTimeSlot = hour
                                                            showAddDialog = true
                                                        },
                                                        label = { Text(hour, fontSize = 10.sp) },
                                                        modifier = Modifier.fillMaxWidth().height(24.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 2.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(daysList) { day ->
                                val dateIso = CalendarHelper.formatIso(day)
                                val appsForDay = appointments.filter { app ->
                                    app.date == dateIso && (selectedProfFilter == null || app.professionalId == selectedProfFilter)
                                }
                                val isSelected = dateIso == selectedDate

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                                    ),
                                    border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.setSelectedDate(dateIso)
                                                    viewModel.setCalendarViewMode("DAY")
                                                }
                                                .padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Today,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "${CalendarHelper.getDayOfWeekLabel(day)}, ${CalendarHelper.getDayOfMonthLabel(day)}",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            Text(
                                                text = "${appsForDay.size} Agendamento(s)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Quick slots for key hours
                                        val quickHours = listOf("08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00")
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            quickHours.forEach { hour ->
                                                val matchApp = appsForDay.find { it.time.substringBefore(":") == hour.substringBefore(":") }
                                                if (matchApp != null) {
                                                    SuggestionChip(
                                                        onClick = { requestEditAppointment(matchApp) },
                                                        label = { Text("🔒 $hour: ${matchApp.clientName.split(" ").firstOrNull()}", fontSize = 10.sp) },
                                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                        ),
                                                        modifier = Modifier.height(24.dp)
                                                    )
                                                } else {
                                                    SuggestionChip(
                                                        onClick = {
                                                            viewModel.setSelectedDate(dateIso)
                                                            prefilledTimeSlot = hour
                                                            showAddDialog = true
                                                        },
                                                        label = { Text(hour, fontSize = 10.sp) },
                                                        modifier = Modifier.height(24.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "MONTH" -> {
                    val monthDays = CalendarHelper.getDaysForMonthGrid(selectedDate)
                    val weekdays = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    ) {
                        // Weekdays Header
                        Row(modifier = Modifier.fillMaxWidth()) {
                            weekdays.forEach { wd ->
                                Text(
                                    text = wd,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Days Grid (6 weeks)
                        val chunkedDays = monthDays.chunked(7)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            chunkedDays.forEach { week ->
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    week.forEach { day ->
                                        val dateIso = CalendarHelper.formatIso(day)
                                        val appsForDay = appointments.filter { app ->
                                            app.date == dateIso && (selectedProfFilter == null || app.professionalId == selectedProfFilter)
                                        }
                                        val hasApps = appsForDay.isNotEmpty()
                                        val isSelected = dateIso == selectedDate
                                        val isCurrentMonth = CalendarHelper.isSameMonth(day, CalendarHelper.parseIso(selectedDate))
                                        val calToday = Calendar.getInstance()
                                        val isToday = dateIso == CalendarHelper.formatIso(calToday.time)

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .padding(2.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    color = when {
                                                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                                                        hasApps -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                                        isToday -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                                                        else -> Color.Transparent
                                                    }
                                                )
                                                .border(
                                                    width = when {
                                                        isSelected -> 2.dp
                                                        hasApps -> 1.5.dp
                                                        isToday -> 1.5.dp
                                                        else -> 0.dp
                                                    },
                                                    color = when {
                                                        isSelected -> MaterialTheme.colorScheme.primary
                                                        hasApps -> MaterialTheme.colorScheme.primary
                                                        isToday -> MaterialTheme.colorScheme.secondary
                                                        else -> Color.Transparent
                                                    },
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .clickable {
                                                    viewModel.setSelectedDate(dateIso)
                                                    viewModel.setCalendarViewMode("DAY")
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = SimpleDateFormat("d", Locale.getDefault()).format(day),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = if (isSelected || isToday || hasApps) FontWeight.Bold else FontWeight.Normal,
                                                    color = when {
                                                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                                        hasApps -> MaterialTheme.colorScheme.primary
                                                        !isCurrentMonth -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    }
                                                )

                                                if (hasApps) {
                                                    Spacer(modifier = Modifier.height(1.dp))
                                                    Surface(
                                                        color = MaterialTheme.colorScheme.primary,
                                                        shape = CircleShape
                                                    ) {
                                                        Text(
                                                            text = "${appsForDay.size}",
                                                            color = MaterialTheme.colorScheme.onPrimary,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Month Footer: Quick schedule bar for the currently selected date
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "Agendamento rápido para ${MaskUtils.formatDatePtBr(selectedDate)}:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val quickHours = listOf("08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00")
                                    quickHours.forEach { hr ->
                                        val taken = appointments.find { app ->
                                            app.date == selectedDate && app.time.substringBefore(":") == hr.substringBefore(":") && (selectedProfFilter == null || app.professionalId == selectedProfFilter)
                                        }
                                        if (taken != null) {
                                            SuggestionChip(
                                                onClick = { requestEditAppointment(taken) },
                                                label = { Text("🔒 $hr: ${taken.clientName.split(" ").firstOrNull()}", fontSize = 10.sp) },
                                                modifier = Modifier.height(24.dp)
                                            )
                                        } else {
                                            SuggestionChip(
                                                onClick = {
                                                    prefilledTimeSlot = hr
                                                    showAddDialog = true
                                                },
                                                label = { Text(hr, fontSize = 10.sp) },
                                                modifier = Modifier.height(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "YEAR" -> {
                    val months = CalendarHelper.getMonthsOfYear(selectedDate)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(months) { month ->
                            val monthName = SimpleDateFormat("MMMM", Locale("pt", "BR")).format(month).replaceFirstChar { it.uppercase() }
                            val monthIso = CalendarHelper.formatIso(month)
                            val totalApps = appointments.filter { app ->
                                app.date.startsWith(monthIso.substring(0, 7)) && (selectedProfFilter == null || app.professionalId == selectedProfFilter)
                            }.size

                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setSelectedDate(monthIso)
                                        viewModel.setCalendarViewMode("MONTH")
                                    },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = monthName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$totalApps agendamento(s)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
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

    // Add Appointment Dialog (Full Screen with Block option)
    if (showAddDialog) {
        AppointmentFormDialog(
            appointment = null,
            clients = clients,
            services = services,
            professionals = professionals,
            defaultDate = selectedDate,
            defaultTime = prefilledTimeSlot,
            viewModel = viewModel,
            onDismiss = { showAddDialog = false },
            onSave = { newApp ->
                if (newApp.isBlock) {
                    viewModel.addAppointment(newApp, null)
                } else {
                    viewModel.addAppointment(newApp, context)
                }
                showAddDialog = false
            }
        )
    }

    // Edit Appointment Dialog (Full Screen with Block option)
    if (selectedAppointmentToEdit != null) {
        AppointmentFormDialog(
            appointment = selectedAppointmentToEdit,
            clients = clients,
            services = services,
            professionals = professionals,
            defaultDate = selectedAppointmentToEdit?.date ?: selectedDate,
            defaultTime = selectedAppointmentToEdit?.time ?: "10:00",
            viewModel = viewModel,
            onDismiss = { selectedAppointmentToEdit = null },
            onSave = { updatedApp ->
                viewModel.updateAppointment(updatedApp)
                selectedAppointmentToEdit = null
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

    if (showQuickClientDialog) {
        var quickName by remember { mutableStateOf("") }
        var quickPhone by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showQuickClientDialog = false },
            title = { Text("Cadastrar Cliente Rápido", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quickName,
                        onValueChange = { quickName = it },
                        label = { Text("Nome do Cliente") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = quickPhone,
                        onValueChange = { quickPhone = it },
                        label = { Text("Telefone (com DDD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (quickName.isNotBlank()) {
                            viewModel.addClient(
                                ClientEntity(
                                    name = quickName,
                                    phone = quickPhone,
                                    cpf = "",
                                    email = "",
                                    birthDate = ""
                                )
                            )
                            showQuickClientDialog = false
                        }
                    }
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showQuickClientDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cancelar")
                }
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
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            // Header: Client/Service Name + Duration + Status
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
                        text = if (appointment.isBlock) appointment.serviceName else appointment.clientName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    if (hasNfse) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "NF-e Emitida - Bloqueado",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "• ${appointment.durationMinutes}m",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
                StatusBadgeChip(status = appointment.status)
            }

            // Subtitle: Service & Professional (compact)
            if (!appointment.isBlock) {
                Text(
                    text = "${appointment.serviceName} • ${appointment.professionalName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            } else {
                Text(
                    text = "Bloqueio • ${appointment.professionalName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE65100),
                    maxLines = 1
                )
            }

            if (appointment.notes.isNotBlank()) {
                Text(
                    text = "Obs: ${appointment.notes}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )
            }

            // Bottom Row: Price & Payment method + Action buttons aligned on same row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (!appointment.isBlock) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                        Text(
                            text = payLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Action Buttons Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!appointment.isBlock) {
                        IconButton(onClick = onWhatsAppMsg, modifier = Modifier.size(28.dp)) {
                            Icon(painter = painterResource(id = R.drawable.ic_whatsapp), contentDescription = "Enviar WhatsApp", tint = Color.Unspecified, modifier = Modifier.size(16.dp))
                        }

                        IconButton(onClick = onDuplicate, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = "Copiar/Duplicar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }

                        if (appointment.status == "AGENDADO") {
                            IconButton(onClick = onCompleteAndIssueNfse, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = "Concluir & NFS-e", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (hasNfse) Icons.Default.Lock else Icons.Outlined.Edit,
                            contentDescription = if (hasNfse) "Bloqueado por NF-e" else "Editar",
                            tint = if (hasNfse) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Cancelar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
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
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${appointment.durationMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (hasNfse) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Bloqueado",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                StatusBadgeChip(status = appointment.status)
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = if (appointment.isBlock) appointment.serviceName else appointment.clientName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            if (!appointment.isBlock) {
                Text(
                    text = "${appointment.serviceName} • ${appointment.professionalName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
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

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!appointment.isBlock) {
                    IconButton(onClick = onWhatsAppMsg, modifier = Modifier.size(26.dp)) {
                        Icon(painter = painterResource(id = R.drawable.ic_whatsapp), contentDescription = "Enviar WhatsApp", tint = Color.Unspecified, modifier = Modifier.size(14.dp))
                    }
                    IconButton(onClick = onDuplicate, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    }
                    if (appointment.status == "AGENDADO") {
                        IconButton(onClick = onCompleteAndIssueNfse, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
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
    defaultTime: String = "10:00",
    viewModel: SalonViewModel,
    onDismiss: () -> Unit,
    onSave: (AppointmentEntity) -> Unit
) {
    var isBlockMode by remember { mutableStateOf(appointment?.isBlock == true) }

    // Normal Appointment fields
    var selectedClient by remember { mutableStateOf(clients.firstOrNull { it.id == appointment?.clientId } ?: clients.firstOrNull()) }
    var selectedService by remember { mutableStateOf(services.firstOrNull { it.id == appointment?.serviceId } ?: services.firstOrNull()) }
    
    // Shared fields
    var selectedProf by remember { mutableStateOf(professionals.firstOrNull { it.id == appointment?.professionalId } ?: professionals.firstOrNull()) }
    var date by remember { mutableStateOf(appointment?.date ?: defaultDate) }
    var time by remember { mutableStateOf(appointment?.time ?: defaultTime) }
    var paymentMethod by remember { mutableStateOf(if (appointment?.paymentMethod?.isNotBlank() == true) appointment.paymentMethod else "CARTAO") }
    var notes by remember { mutableStateOf(appointment?.notes ?: "") }

    // Block fields
    var blockReason by remember { mutableStateOf(if (appointment?.isBlock == true) appointment.serviceName else "Intervalo / Almoço") }
    var blockDurationMinutes by remember { mutableStateOf((appointment?.durationMinutes ?: 60).toString()) }

    var expandedClientDropdown by remember { mutableStateOf(false) }
    var expandedServiceDropdown by remember { mutableStateOf(false) }

    var showClientFormDialog by remember { mutableStateOf(false) }
    var clientToEdit by remember { mutableStateOf<ClientEntity?>(null) }

    if (showClientFormDialog) {
        ClientFormDialog(
            client = clientToEdit,
            paymentMethod = paymentMethod,
            onDismiss = { showClientFormDialog = false },
            onSave = { savedClient ->
                if (clientToEdit == null) {
                    viewModel.addClientAndGet(savedClient) { newId ->
                        val newClient = savedClient.copy(id = newId)
                        selectedClient = newClient
                        showClientFormDialog = false
                    }
                } else {
                    viewModel.updateClient(savedClient)
                    selectedClient = savedClient
                    showClientFormDialog = false
                }
            }
        )
    }

    var showServiceFormDialog by remember { mutableStateOf(false) }
    var serviceToEdit by remember { mutableStateOf<ServiceEntity?>(null) }

    if (showServiceFormDialog) {
        ServiceFormDialog(
            service = serviceToEdit,
            professionals = professionals,
            onDismiss = { showServiceFormDialog = false },
            onSave = { savedService ->
                if (serviceToEdit == null) {
                    viewModel.addServiceAndGet(savedService) { newId ->
                        val newService = savedService.copy(id = newId)
                        selectedService = newService
                        showServiceFormDialog = false
                    }
                } else {
                    viewModel.updateService(savedService)
                    selectedService = savedService
                    showServiceFormDialog = false
                }
            }
        )
    }

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
                TextButton(
                    onClick = { showDatePicker = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
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
                TextButton(
                    onClick = { showTimePicker = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    val isFormValid = if (isBlockMode) {
        selectedProf != null && blockReason.isNotBlank()
    } else {
        selectedClient != null && selectedService != null && selectedProf != null
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (appointment == null) {
                                if (isBlockMode) "Bloquear Horário" else "Novo Agendamento"
                            } else {
                                if (isBlockMode) "Editar Bloqueio" else "Editar Agendamento"
                            },
                            style = MaterialTheme.typography.titleLarge,
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
                                if (isFormValid) {
                                    if (isBlockMode) {
                                        val blockApp = AppointmentEntity(
                                            id = appointment?.id ?: 0,
                                            clientId = 0,
                                            clientName = "BLOQUEIO DE AGENDA",
                                            clientPhone = "",
                                            serviceId = 0,
                                            serviceName = blockReason.trim(),
                                            professionalId = selectedProf!!.id,
                                            professionalName = selectedProf!!.name,
                                            date = date,
                                            time = time,
                                            durationMinutes = blockDurationMinutes.toIntOrNull() ?: 60,
                                            price = 0.0,
                                            status = "BLOQUEIO",
                                            isBlock = true,
                                            paymentMethod = "",
                                            notes = notes.trim()
                                        )
                                        onSave(blockApp)
                                    } else {
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
                                            isBlock = false,
                                            notes = notes.trim()
                                        )
                                        onSave(app)
                                    }
                                }
                            },
                            enabled = isFormValid,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("save_appointment_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salvar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Segmented Selector: Agendamento de Cliente vs Bloqueio de Horário
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Tipo de Registro:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = !isBlockMode,
                                onClick = { isBlockMode = false },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                                icon = {
                                    Icon(Icons.Default.EventAvailable, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            ) {
                                Text("Agendamento", fontWeight = if (!isBlockMode) FontWeight.Bold else FontWeight.Normal)
                            }
                            SegmentedButton(
                                selected = isBlockMode,
                                onClick = { isBlockMode = true },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                                icon = {
                                    Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            ) {
                                Text("Bloqueio", fontWeight = if (isBlockMode) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }

                if (isBlockMode) {
                    // BLOQUEIO DE HORÁRIO SECTION
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Block, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Opções do Bloqueio de Horário",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            Text(
                                text = "O horário selecionado ficará indisponível para novos agendamentos na agenda do salão.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Motivo do Bloqueio com Atalhos Rápidos
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Motivo do Bloqueio *", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        val commonReasons = listOf(
                            "Intervalo / Almoço",
                            "Folga / Férias",
                            "Reunião / Treinamento",
                            "Manutenção",
                            "Compromisso Pessoal"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            commonReasons.forEach { r ->
                                FilterChip(
                                    selected = blockReason == r,
                                    onClick = { blockReason = r },
                                    label = { Text(r, fontSize = 12.sp) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = blockReason,
                            onValueChange = { blockReason = it },
                            label = { Text("Descrição / Motivo") },
                            leadingIcon = { Icon(Icons.Default.EditNote, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Profissional
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Profissional *", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            professionals.forEach { prof ->
                                FilterChip(
                                    selected = selectedProf?.id == prof.id,
                                    onClick = { selectedProf = prof },
                                    label = { Text(prof.name) },
                                    leadingIcon = if (selectedProf?.id == prof.id) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }
                    }

                    // Data e Horário
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                            label = { Text("Horário de Início *") },
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

                    // Duração do Bloqueio
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Duração do Bloqueio", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        val durations = listOf("30", "60", "90", "120", "240", "480")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            durations.forEach { d ->
                                val label = when (d) {
                                    "240" -> "4 horas (Turno)"
                                    "480" -> "Dia Todo (8h)"
                                    else -> "$d min"
                                }
                                FilterChip(
                                    selected = blockDurationMinutes == d,
                                    onClick = { blockDurationMinutes = d },
                                    label = { Text(label, fontSize = 12.sp) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = blockDurationMinutes,
                            onValueChange = { blockDurationMinutes = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Duração personalizada em minutos") },
                            leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Observações
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Observações adicionais (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                } else {
                    // AGENDAMENTO NORMAL SECTION
                    // Cliente
                    ExposedDropdownMenuBox(
                        expanded = expandedClientDropdown,
                        onExpandedChange = { expandedClientDropdown = !expandedClientDropdown }
                    ) {
                        OutlinedTextField(
                            value = selectedClient?.name ?: "Selecione o Cliente (Dois cliques para cadastrar)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Cliente *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClientDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("appointment_client_dropdown")
                                .pointerInput(selectedClient) {
                                    detectTapGestures(
                                        onDoubleTap = {
                                            clientToEdit = selectedClient
                                            showClientFormDialog = true
                                        }
                                    )
                                }
                        )
                        ExposedDropdownMenu(
                            expanded = expandedClientDropdown,
                            onDismissRequest = { expandedClientDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("➕ Cadastrar Novo Cliente...", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    clientToEdit = null
                                    showClientFormDialog = true
                                    expandedClientDropdown = false
                                }
                            )
                            HorizontalDivider()
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

                    // Serviço
                    ExposedDropdownMenuBox(
                        expanded = expandedServiceDropdown,
                        onExpandedChange = { expandedServiceDropdown = !expandedServiceDropdown }
                    ) {
                        OutlinedTextField(
                            value = selectedService?.let { "${it.name} • ${MaskUtils.formatCurrency(it.price)}" } ?: "Selecione o Serviço (Dois cliques para cadastrar)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Serviço *") },
                            leadingIcon = { Icon(Icons.Default.ContentCut, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedServiceDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("appointment_service_dropdown")
                                .pointerInput(selectedService) {
                                    detectTapGestures(
                                        onDoubleTap = {
                                            serviceToEdit = selectedService
                                            showServiceFormDialog = true
                                        }
                                    )
                                }
                        )
                        ExposedDropdownMenu(
                            expanded = expandedServiceDropdown,
                            onDismissRequest = { expandedServiceDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("➕ Cadastrar Novo Serviço...", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    serviceToEdit = null
                                    showServiceFormDialog = true
                                    expandedServiceDropdown = false
                                }
                            )
                            HorizontalDivider()
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

                    // Profissional
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Profissional Responsável *", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            professionals.forEach { prof ->
                                FilterChip(
                                    selected = selectedProf?.id == prof.id,
                                    onClick = { selectedProf = prof },
                                    label = { Text(prof.name) },
                                    leadingIcon = if (selectedProf?.id == prof.id) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }
                    }

                    // Data e Horário
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
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

                    // Forma de Pagamento
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Forma Prevista de Pagamento", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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

                    // Observações
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Observações do Agendamento") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Save Button
                Button(
                    onClick = {
                        if (isFormValid) {
                            if (isBlockMode) {
                                val blockApp = AppointmentEntity(
                                    id = appointment?.id ?: 0,
                                    clientId = 0,
                                    clientName = "BLOQUEIO DE AGENDA",
                                    clientPhone = "",
                                    serviceId = 0,
                                    serviceName = blockReason.trim(),
                                    professionalId = selectedProf!!.id,
                                    professionalName = selectedProf!!.name,
                                    date = date,
                                    time = time,
                                    durationMinutes = blockDurationMinutes.toIntOrNull() ?: 60,
                                    price = 0.0,
                                    status = "BLOQUEIO",
                                    isBlock = true,
                                    paymentMethod = "",
                                    notes = notes.trim()
                                )
                                onSave(blockApp)
                            } else {
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
                                    isBlock = false,
                                    notes = notes.trim()
                                )
                                onSave(app)
                            }
                        }
                    },
                    enabled = isFormValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBlockMode) "Confirmar Bloqueio de Horário" else "Salvar Agendamento",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
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
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            ) {
                Text("Cancelar")
            }
        }
    )
}

object CalendarHelper {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun parseIso(dateStr: String): Date {
        return try {
            isoFormat.parse(dateStr) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }

    fun formatIso(date: Date): String {
        return isoFormat.format(date)
    }

    fun addDays(dateStr: String, days: Int): String {
        val cal = Calendar.getInstance()
        cal.time = parseIso(dateStr)
        cal.add(Calendar.DAY_OF_YEAR, days)
        return formatIso(cal.time)
    }

    fun addMonths(dateStr: String, months: Int): String {
        val cal = Calendar.getInstance()
        cal.time = parseIso(dateStr)
        cal.add(Calendar.MONTH, months)
        return formatIso(cal.time)
    }

    fun addYears(dateStr: String, years: Int): String {
        val cal = Calendar.getInstance()
        cal.time = parseIso(dateStr)
        cal.add(Calendar.YEAR, years)
        return formatIso(cal.time)
    }

    fun getDayOfWeekLabel(date: Date): String {
        val sdf = SimpleDateFormat("EEE", Locale("pt", "BR"))
        return sdf.format(date).replaceFirstChar { it.uppercase() }
    }

    fun getDayOfMonthLabel(date: Date): String {
        val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
        return sdf.format(date)
    }

    fun getMonthLabel(dateStr: String): String {
        val sdf = SimpleDateFormat("MMMM 'de' yyyy", Locale("pt", "BR"))
        return sdf.format(parseIso(dateStr)).replaceFirstChar { it.uppercase() }
    }

    fun getYearLabel(dateStr: String): String {
        val sdf = SimpleDateFormat("yyyy", Locale.getDefault())
        return sdf.format(parseIso(dateStr))
    }

    fun getStartOfWeek(dateStr: String): Date {
        val cal = Calendar.getInstance()
        cal.time = parseIso(dateStr)
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        return cal.time
    }

    fun getDaysOfWeek(dateStr: String): List<Date> {
        val cal = Calendar.getInstance()
        cal.time = getStartOfWeek(dateStr)
        val list = mutableListOf<Date>()
        for (i in 0 until 7) {
            list.add(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    fun getDaysOfFortnight(dateStr: String): List<Date> {
        val cal = Calendar.getInstance()
        cal.time = parseIso(dateStr)
        val list = mutableListOf<Date>()
        for (i in 0 until 15) {
            list.add(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    fun getDaysForMonthGrid(dateStr: String): List<Date> {
        val cal = Calendar.getInstance()
        cal.time = parseIso(dateStr)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val offset = firstDayOfWeek - Calendar.SUNDAY
        cal.add(Calendar.DAY_OF_YEAR, -offset)
        val list = mutableListOf<Date>()
        for (i in 0 until 42) {
            list.add(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    fun isSameMonth(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        return cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
    }

    fun getMonthsOfYear(dateStr: String): List<Date> {
        val cal = Calendar.getInstance()
        cal.time = parseIso(dateStr)
        val year = cal.get(Calendar.YEAR)
        val list = mutableListOf<Date>()
        for (m in 0 until 12) {
            val cellCal = Calendar.getInstance()
            cellCal.set(Calendar.YEAR, year)
            cellCal.set(Calendar.MONTH, m)
            cellCal.set(Calendar.DAY_OF_MONTH, 1)
            list.add(cellCal.time)
        }
        return list
    }
}
