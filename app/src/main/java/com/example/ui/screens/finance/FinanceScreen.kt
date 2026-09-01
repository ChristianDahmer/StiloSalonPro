package com.example.ui.screens.finance

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FinancialTransactionEntity
import com.example.ui.SalonViewModel
import com.example.utils.MaskUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FinanceScreen(
    viewModel: SalonViewModel,
    transactions: List<FinancialTransactionEntity>,
    typeFilter: String?,
    periodFilter: String,
    isGridView: Boolean = false,
    searchQuery: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var selectedTransactionToEdit by remember { mutableStateOf<FinancialTransactionEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var initialDialogType by remember { mutableStateOf("RECEITA") }
    var showDreReportDialog by remember { mutableStateOf(false) }

    // Date filtering logic
    val filteredByPeriod = remember(transactions, periodFilter) {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val todayIso = MaskUtils.getCurrentDateIso()

        transactions.filter { item ->
            when (periodFilter) {
                "HOJE" -> item.date == todayIso
                "ESTE_MES" -> {
                    try {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val d = sdf.parse(item.date)
                        if (d != null) {
                            val c = Calendar.getInstance()
                            c.time = d
                            c.get(Calendar.YEAR) == currentYear && c.get(Calendar.MONTH) == currentMonth
                        } else true
                    } catch (e: Exception) {
                        true
                    }
                }
                "MES_ANTERIOR" -> {
                    try {
                        val prevCal = Calendar.getInstance()
                        prevCal.add(Calendar.MONTH, -1)
                        val prevYear = prevCal.get(Calendar.YEAR)
                        val prevMonth = prevCal.get(Calendar.MONTH)

                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val d = sdf.parse(item.date)
                        if (d != null) {
                            val c = Calendar.getInstance()
                            c.time = d
                            c.get(Calendar.YEAR) == prevYear && c.get(Calendar.MONTH) == prevMonth
                        } else true
                    } catch (e: Exception) {
                        true
                    }
                }
                else -> true // TODOS
            }
        }
    }

    // Type & Search filtering logic
    val filteredTransactions = remember(filteredByPeriod, typeFilter, searchQuery) {
        filteredByPeriod.filter { item ->
            val matchesType = when (typeFilter) {
                "RECEITA" -> item.type == "RECEITA"
                "DESPESA" -> item.type == "DESPESA"
                "PENDENTE" -> item.status == "PENDENTE"
                else -> true
            }

            val matchesSearch = if (searchQuery.isBlank()) true else {
                item.description.contains(searchQuery, ignoreCase = true) ||
                        item.category.contains(searchQuery, ignoreCase = true) ||
                        item.paymentMethod.contains(searchQuery, ignoreCase = true) ||
                        item.notes.contains(searchQuery, ignoreCase = true)
            }

            matchesType && matchesSearch
        }
    }

    // Calculations based on period-filtered transactions
    val totalIncome = remember(filteredByPeriod) {
        filteredByPeriod.filter { it.type == "RECEITA" && it.status == "PAGO" }.sumOf { it.amount }
    }
    val totalExpense = remember(filteredByPeriod) {
        filteredByPeriod.filter { it.type == "DESPESA" && it.status == "PAGO" }.sumOf { it.amount }
    }
    val netBalance = totalIncome - totalExpense

    val pendingReceivables = remember(filteredByPeriod) {
        filteredByPeriod.filter { it.type == "RECEITA" && it.status == "PENDENTE" }.sumOf { it.amount }
    }
    val pendingPayables = remember(filteredByPeriod) {
        filteredByPeriod.filter { it.type == "DESPESA" && it.status == "PENDENTE" }.sumOf { it.amount }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    initialDialogType = "RECEITA"
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_finance")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo Lançamento")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Financial Summary Dashboard
            FinancialSummaryCard(
                netBalance = netBalance,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                pendingReceivables = pendingReceivables,
                pendingPayables = pendingPayables,
                periodLabel = when (periodFilter) {
                    "HOJE" -> "Hoje"
                    "ESTE_MES" -> "Este Mês"
                    "MES_ANTERIOR" -> "Mês Anterior"
                    else -> "Período Geral"
                }
            )

            // Period & Type Filter Chips Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Period Filters + DRE Report Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Período:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )
                        listOf(
                            "ESTE_MES" to "Este Mês",
                            "HOJE" to "Hoje",
                            "MES_ANTERIOR" to "Mês Anterior",
                            "TODOS" to "Tudo"
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = periodFilter == key,
                                onClick = { viewModel.setSelectedFinancePeriodFilter(key) },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 11.sp, maxLines = 1, softWrap = false) },
                                modifier = Modifier.height(26.dp).testTag("filter_period_$key")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    FilledTonalButton(
                        onClick = { showDreReportDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("DRE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Type Filters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tipo:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false
                    )
                    listOf(
                        "TODAS" to "Todas",
                        "RECEITA" to "Receitas",
                        "DESPESA" to "Despesas",
                        "PENDENTE" to "Pendentes"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = (typeFilter ?: "TODAS") == key,
                            onClick = { viewModel.setSelectedFinanceTypeFilter(if (key == "TODAS") null else key) },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 11.sp, maxLines = 1, softWrap = false) },
                            leadingIcon = {
                                when (key) {
                                    "RECEITA" -> Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(12.dp))
                                    "DESPESA" -> Icon(Icons.Default.TrendingDown, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(12.dp))
                                    "PENDENTE" -> Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(12.dp))
                                    else -> null
                                }
                            },
                            modifier = Modifier.height(26.dp).testTag("filter_type_$key")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Main List or Grid Content
            if (filteredTransactions.isEmpty()) {
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
                                    imageVector = Icons.Outlined.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Nenhum lançamento encontrado",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ajuste os filtros acima ou use o botão '+' para registrar movimentações no caixa.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
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
                        items(filteredTransactions, key = { it.id }) { item ->
                            TransactionGridCard(
                                transaction = item,
                                onEdit = { selectedTransactionToEdit = item },
                                onDelete = { viewModel.deleteFinancialTransaction(item) },
                                onToggleStatus = { viewModel.toggleFinancialTransactionStatus(item) },
                                onDuplicate = { viewModel.duplicateFinancialTransaction(item) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 2.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredTransactions, key = { it.id }) { item ->
                            TransactionListItem(
                                transaction = item,
                                onEdit = { selectedTransactionToEdit = item },
                                onDelete = { viewModel.deleteFinancialTransaction(item) },
                                onToggleStatus = { viewModel.toggleFinancialTransactionStatus(item) },
                                onDuplicate = { viewModel.duplicateFinancialTransaction(item) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Transaction Dialog
    if (showAddDialog || selectedTransactionToEdit != null) {
        AddEditTransactionDialog(
            initialTransaction = selectedTransactionToEdit,
            defaultType = initialDialogType,
            onDismiss = {
                showAddDialog = false
                selectedTransactionToEdit = null
            },
            onSave = { transaction ->
                if (selectedTransactionToEdit == null) {
                    viewModel.addFinancialTransaction(transaction)
                    Toast.makeText(context, "Lançamento adicionado ao Caixa!", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.updateFinancialTransaction(transaction)
                    Toast.makeText(context, "Lançamento atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                }
                showAddDialog = false
                selectedTransactionToEdit = null
            }
        )
    }

    // Financial DRE / Summary Report Dialog
    if (showDreReportDialog) {
        FinanceReportDialog(
            transactions = filteredByPeriod,
            periodName = when (periodFilter) {
                "HOJE" -> "Hoje (${MaskUtils.formatDatePtBr(MaskUtils.getCurrentDateIso())})"
                "ESTE_MES" -> "Mês Vigente"
                "MES_ANTERIOR" -> "Mês Anterior"
                else -> "Geral de Todos os Períodos"
            },
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netBalance = netBalance,
            pendingReceivables = pendingReceivables,
            pendingPayables = pendingPayables,
            onDismiss = { showDreReportDialog = false }
        )
    }
}

@Composable
fun FinancialSummaryCard(
    netBalance: Double,
    totalIncome: Double,
    totalExpense: Double,
    pendingReceivables: Double,
    pendingPayables: Double,
    periodLabel: String
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saldo em Caixa ($periodLabel)",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Surface(
                    shape = CircleShape,
                    color = if (netBalance >= 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                ) {
                    Text(
                        text = if (netBalance >= 0) "+ Positivo" else "- Negativo",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = MaskUtils.formatCurrency(netBalance),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Income / Expense Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Income
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E7D32)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Entradas", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1B5E20))
                            Text(MaskUtils.formatCurrency(totalIncome), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }
                }

                // Expense
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFEBEE)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Saídas", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB71C1C))
                            Text(MaskUtils.formatCurrency(totalExpense), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            val total = totalIncome + totalExpense
            if (total > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val incomePercent = (totalIncome / total) * 100
                        Text(
                            text = "${incomePercent.toInt()}% Entradas",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                        val expensePercent = (totalExpense / total) * 100
                        Text(
                            text = "${expensePercent.toInt()}% Saídas",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFB71C1C),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color.LightGray.copy(alpha = 0.3f))
                    ) {
                        val weightIncome = (totalIncome / total).toFloat()
                        val weightExpense = (totalExpense / total).toFloat()
                        if (weightIncome > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(weightIncome)
                                    .background(Color(0xFF2E7D32))
                            )
                        }
                        if (weightExpense > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(weightExpense)
                                    .background(MaterialTheme.colorScheme.error)
                            )
                        }
                    }
                }
            }

            // Pending Receivables / Payables indicator if any
            if (pendingReceivables > 0 || pendingPayables > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (pendingReceivables > 0) {
                        Text(
                            text = "A receber: ${MaskUtils.formatCurrency(pendingReceivables)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (pendingPayables > 0) {
                        Text(
                            text = "A pagar: ${MaskUtils.formatCurrency(pendingPayables)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionListItem(
    transaction: FinancialTransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleStatus: () -> Unit,
    onDuplicate: () -> Unit
) {
    val isIncome = transaction.type == "RECEITA"
    val isPaid = transaction.status == "PAGO"

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(transaction.id) {
                detectTapGestures(onDoubleTap = { onEdit() })
            },
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Icon Box
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (!isPaid) Color(0xFFFFF3E0)
                            else if (isIncome) Color(0xFFE8F5E9)
                            else Color(0xFFFFEBEE)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (!isPaid) Icons.Default.Schedule else if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (!isPaid) Color(0xFFE65100) else if (isIncome) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(1.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = transaction.category,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = transaction.paymentMethod,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        Text(
                            text = MaskUtils.formatDatePtBr(transaction.date),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${if (isIncome) "+" else "-"} ${MaskUtils.formatCurrency(transaction.amount)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (!isPaid) Color(0xFFE65100) else if (isIncome) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = onToggleStatus,
                        label = { Text(if (isPaid) "PAGO" else "PENDENTE", fontSize = 9.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isPaid) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                            labelColor = if (isPaid) Color(0xFF2E7D32) else Color(0xFFE65100)
                        ),
                        border = null,
                        modifier = Modifier.height(20.dp)
                    )

                    IconButton(onClick = onDuplicate, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = "Duplicar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                    }

                    IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Editar", modifier = Modifier.size(13.dp))
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(13.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionGridCard(
    transaction: FinancialTransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleStatus: () -> Unit,
    onDuplicate: () -> Unit
) {
    val isIncome = transaction.type == "RECEITA"
    val isPaid = transaction.status == "PAGO"

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(transaction.id) {
                detectTapGestures(onDoubleTap = { onEdit() })
            },
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (!isPaid) Color(0xFFFFF3E0) else if (isIncome) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (isIncome) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = transaction.type,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isIncome) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                        )
                    }
                }

                AssistChip(
                    onClick = onToggleStatus,
                    label = { Text(if (isPaid) "PAGO" else "PENDENTE", fontSize = 8.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isPaid) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                        labelColor = if (isPaid) Color(0xFF2E7D32) else Color(0xFFE65100)
                    ),
                    border = null,
                    modifier = Modifier.height(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = transaction.description,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${transaction.category} • ${transaction.paymentMethod}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = MaskUtils.formatCurrency(transaction.amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (!isPaid) Color(0xFFE65100) else if (isIncome) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = MaskUtils.formatDatePtBr(transaction.date),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row {
                    IconButton(onClick = onDuplicate, modifier = Modifier.size(22.dp)) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = "Duplicar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(22.dp)) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Editar", modifier = Modifier.size(12.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(22.dp)) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionDialog(
    initialTransaction: FinancialTransactionEntity?,
    defaultType: String,
    onDismiss: () -> Unit,
    onSave: (FinancialTransactionEntity) -> Unit
) {
    var type by remember { mutableStateOf(initialTransaction?.type ?: defaultType) }
    var description by remember { mutableStateOf(initialTransaction?.description ?: "") }
    var amountText by remember { mutableStateOf(if (initialTransaction != null) String.format("%.2f", initialTransaction.amount) else "") }
    var category by remember { mutableStateOf(initialTransaction?.category ?: if (type == "RECEITA") "Serviços" else "Fornecedores") }
    var date by remember { mutableStateOf(initialTransaction?.date ?: MaskUtils.getCurrentDateIso()) }
    var paymentMethod by remember { mutableStateOf(initialTransaction?.paymentMethod ?: "PIX") }
    var status by remember { mutableStateOf(initialTransaction?.status ?: "PAGO") }
    var notes by remember { mutableStateOf(initialTransaction?.notes ?: "") }

    var expandedCategory by remember { mutableStateOf(false) }
    var expandedPayment by remember { mutableStateOf(false) }

    val categories = if (type == "RECEITA") {
        listOf("Serviços", "Vendas de Produtos", "Sinais e Reservas", "Outras Receitas")
    } else {
        listOf("Fornecedores", "Aluguel", "Utilidades", "Comissões", "Salários", "Impostos", "Equipamentos", "Outras Despesas")
    }

    val paymentMethods = listOf("PIX", "CARTAO_CREDITO", "CARTAO_DEBITO", "DINHEIRO", "BOLETO", "OUTROS")

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
                                if (initialTransaction == null) "Novo Lançamento no Caixa" else "Editar Lançamento",
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
                                    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
                                    if (description.isNotBlank() && parsedAmount > 0) {
                                        onSave(
                                            FinancialTransactionEntity(
                                                id = initialTransaction?.id ?: 0,
                                                type = type,
                                                description = description.trim(),
                                                amount = parsedAmount,
                                                category = category,
                                                date = date.trim(),
                                                paymentMethod = paymentMethod,
                                                status = status,
                                                relatedAppointmentId = initialTransaction?.relatedAppointmentId,
                                                relatedNfseId = initialTransaction?.relatedNfseId,
                                                notes = notes.trim()
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .testTag("btn_save_finance"),
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
                    // Type Selector (RECEITA / DESPESA)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = type == "RECEITA",
                            onClick = {
                                type = "RECEITA"
                                category = "Serviços"
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) {
                            Text("Receita (Entrada)", fontSize = 12.sp)
                        }
                        SegmentedButton(
                            selected = type == "DESPESA",
                            onClick = {
                                type = "DESPESA"
                                category = "Fornecedores"
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) {
                            Text("Despesa (Saída)", fontSize = 12.sp)
                        }
                    }

                    // Description Input
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrição / Motivo *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("finance_desc_input")
                    )

                    // Amount Input
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.replace(',', '.') },
                        label = { Text("Valor R$ *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        prefix = { Text("R$ ") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("finance_amount_input")
                    )

                    // Category Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedCategory,
                        onExpandedChange = { expandedCategory = !expandedCategory }
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoria") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCategory,
                            onDismissRequest = { expandedCategory = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        expandedCategory = false
                                    }
                                )
                            }
                        }
                    }

                    // Date & Payment Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Data (AAAA-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        ExposedDropdownMenuBox(
                            expanded = expandedPayment,
                            onExpandedChange = { expandedPayment = !expandedPayment },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = paymentMethod,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Pagamento") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPayment) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedPayment,
                                onDismissRequest = { expandedPayment = false }
                            ) {
                                paymentMethods.forEach { pm ->
                                    DropdownMenuItem(
                                        text = { Text(pm) },
                                        onClick = {
                                            paymentMethod = pm
                                            expandedPayment = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Status Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (status == "PAGO") "Lançamento PAGO / Efetivado" else "Lançamento PENDENTE",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (status == "PAGO") Color(0xFF2E7D32) else Color(0xFFE65100)
                        )
                        Switch(
                            checked = status == "PAGO",
                            onCheckedChange = { isChecked ->
                                status = if (isChecked) "PAGO" else "PENDENTE"
                            }
                        )
                    }

                    // Notes Input
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Observações (Opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun FinanceReportDialog(
    transactions: List<FinancialTransactionEntity>,
    periodName: String,
    totalIncome: Double,
    totalExpense: Double,
    netBalance: Double,
    pendingReceivables: Double,
    pendingPayables: Double,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val marginPercent = if (totalIncome > 0) ((netBalance / totalIncome) * 100) else 0.0

    val reportText = remember(transactions, periodName) {
        val sb = StringBuilder()
        sb.appendLine("==========================================")
        sb.appendLine("    DEMONSTRATIVO DE FLUXO DE CAIXA (DRE)   ")
        sb.appendLine("==========================================")
        sb.appendLine("Período: $periodName")
        sb.appendLine("------------------------------------------")
        sb.appendLine("Receitas Pagas (+):  ${MaskUtils.formatCurrency(totalIncome)}")
        sb.appendLine("Despesas Pagas (-):  ${MaskUtils.formatCurrency(totalExpense)}")
        sb.appendLine("------------------------------------------")
        sb.appendLine("SALDO LÍQUIDO (=):   ${MaskUtils.formatCurrency(netBalance)}")
        sb.appendLine("Margem Operacional:  ${String.format("%.1f", marginPercent)}%")
        sb.appendLine("------------------------------------------")
        sb.appendLine("Contas a Receber:    ${MaskUtils.formatCurrency(pendingReceivables)}")
        sb.appendLine("Contas a Pagar:      ${MaskUtils.formatCurrency(pendingPayables)}")
        sb.appendLine("==========================================")
        sb.appendLine("Lançamentos do Período ($periodName):")
        transactions.take(15).forEach { t ->
            val sign = if (t.type == "RECEITA") "+" else "-"
            val statusTag = if (t.status == "PAGO") "[PAGO]" else "[PENDENTE]"
            sb.appendLine("${t.date} | $sign${MaskUtils.formatCurrency(t.amount)} | ${t.description} $statusTag")
        }
        sb.toString()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Assessment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Relatório Financeiro & DRE")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Resumo analítico do fluxo de caixa referente a: $periodName.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = reportText,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, reportText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Compartilhar Relatório Financeiro")
                        context.startActivity(shareIntent)
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Compartilhar")
                }

                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Relatorio_Financeiro", reportText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Relatório copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copiar DRE")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Fechar")
            }
        }
    )
}
