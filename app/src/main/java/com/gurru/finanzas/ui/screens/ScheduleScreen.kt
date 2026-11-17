package com.gurru.finanzas.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gurru.finanzas.data.local.entity.DebtEntity
import com.gurru.finanzas.data.local.entity.IncomeEntity
import com.gurru.finanzas.data.model.RecurrenceFrequency
import com.gurru.finanzas.domain.RecurrenceCalculator
import com.gurru.finanzas.ui.FinanceUiState
import com.gurru.finanzas.ui.components.DayOfWeekSelector
import com.gurru.finanzas.ui.components.EmptyStateCard
import com.gurru.finanzas.ui.components.FrequencySelector
import com.gurru.finanzas.ui.components.formatCurrencyMxn
import com.gurru.finanzas.ui.components.formatLocalDateEs
import com.gurru.finanzas.ui.theme.CoralAlert
import com.gurru.finanzas.ui.theme.EmeraldPrimary
import java.time.LocalDate

@Composable
fun ScheduleScreen(
    state: FinanceUiState,
    onSaveIncome: (IncomeEntity) -> Unit,
    onDeleteIncome: (IncomeEntity) -> Unit,
    onSaveDebt: (DebtEntity) -> Unit,
    onPayDebtInstallment: (DebtEntity) -> Unit,
    onDeleteDebt: (DebtEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddIncome by remember { mutableStateOf(false) }
    var showAddDebt by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { if (selectedTab == 0) showAddIncome = true else showAddDebt = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "Agenda Financiera",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Configura tus días de ingreso y vencimientos de deudas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Ingresos (${state.incomes.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Deudas (${state.debts.size})") }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (selectedTab == 0) {
                    if (state.incomes.isEmpty()) {
                        item {
                            EmptyStateCard(
                                title = "Sin ingresos registrados",
                                subtitle = "Toca + para indicar cuánto ganas y qué días te llega el dinero (cada viernes, cada 15 días o mensual)."
                            )
                        }
                    } else {
                        items(state.incomes, key = { it.id }) { income ->
                            IncomeCard(
                                income = income,
                                onDelete = { onDeleteIncome(income) }
                            )
                        }
                    }
                } else {
                    if (state.debts.isEmpty()) {
                        item {
                            EmptyStateCard(
                                title = "Sin deudas agendadas",
                                subtitle = "Toca + para registrar tus deudas, frecuencia de pago y activar recordatorios automáticos."
                            )
                        }
                    } else {
                        items(state.debts, key = { it.id }) { debt ->
                            DebtCard(
                                debt = debt,
                                onPayInstallment = { onPayDebtInstallment(debt) },
                                onDelete = { onDeleteDebt(debt) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddIncome) {
        AddIncomeDialog(
            onDismiss = { showAddIncome = false },
            onSave = {
                onSaveIncome(it)
                showAddIncome = false
            }
        )
    }

    if (showAddDebt) {
        AddDebtDialog(
            onDismiss = { showAddDebt = false },
            onSave = {
                onSaveDebt(it)
                showAddDebt = false
            }
        )
    }
}

@Composable
private fun IncomeCard(
    income: IncomeEntity,
    onDelete: () -> Unit
) {
    val nextPay = RecurrenceCalculator.nextOccurrence(
        income.frequency,
        income.dayOfWeek,
        income.dayOfMonth,
        income.anchorDateEpochDay
    )
    val scheduleDesc = RecurrenceCalculator.describeSchedule(
        income.frequency,
        income.dayOfWeek,
        income.dayOfMonth,
        income.anchorDateEpochDay
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = income.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = scheduleDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (nextPay != null) {
                    Text(
                        text = "Próximo cobro: ${formatLocalDateEs(nextPay)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldPrimary
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "+${formatCurrencyMxn(income.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

@Composable
private fun DebtCard(
    debt: DebtEntity,
    onPayInstallment: () -> Unit,
    onDelete: () -> Unit
) {
    val nextDue = if (!debt.isPaidOff) {
        RecurrenceCalculator.nextOccurrence(
            debt.frequency,
            debt.dayOfWeek,
            debt.dayOfMonth,
            debt.anchorDateEpochDay
        )
    } else null

    val paidRatio = if (debt.totalAmount > 0) {
        ((debt.totalAmount - debt.remainingAmount) / debt.totalAmount).toFloat().coerceIn(0f, 1f)
    } else 1f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = debt.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (debt.reminderEnabled && !debt.isPaidOff) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Recordatorio activo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 6.dp).height(16.dp)
                            )
                        }
                    }
                    Text(
                        text = RecurrenceCalculator.describeSchedule(
                            debt.frequency,
                            debt.dayOfWeek,
                            debt.dayOfMonth,
                            debt.anchorDateEpochDay
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { paidRatio },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Restante: ${formatCurrencyMxn(debt.remainingAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (debt.isPaidOff) EmeraldPrimary else CoralAlert
                )
                Text(
                    text = "Cuota: ${formatCurrencyMxn(debt.installmentAmount)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (nextDue != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Próximo pago: ${formatLocalDateEs(nextDue)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(onClick = onPayInstallment) {
                        Text("Registrar pago")
                    }
                }
            } else if (debt.isPaidOff) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "¡Deuda liquidada!",
                    style = MaterialTheme.typography.labelMedium,
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AddIncomeDialog(
    onDismiss: () -> Unit,
    onSave: (IncomeEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf(RecurrenceFrequency.SEMANAL) }
    var dayOfWeek by remember { mutableIntStateOf(5) } // Viernes por defecto
    var dayOfMonthText by remember { mutableStateOf("15") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo ingreso") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre (ej. Sueldo, Negocio)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Monto neto que recibes (MXN)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("¿Cada cuánto te llega?", style = MaterialTheme.typography.labelMedium)
                FrequencySelector(
                    selected = frequency,
                    onSelect = { frequency = it },
                    includeOneTime = true
                )
                when (frequency) {
                    RecurrenceFrequency.SEMANAL -> {
                        DayOfWeekSelector(
                            selectedIsoDay = dayOfWeek,
                            onSelect = { dayOfWeek = it }
                        )
                    }
                    RecurrenceFrequency.MENSUAL -> {
                        OutlinedTextField(
                            value = dayOfMonthText,
                            onValueChange = { dayOfMonthText = it },
                            label = { Text("Día del mes en que llega (1-31)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    RecurrenceFrequency.QUINCENAL -> {
                        Text(
                            text = "Se agendará cada 14 días tomando hoy como referencia de pago.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    RecurrenceFrequency.UNICO -> {
                        Text(
                            text = "Se registrará como ingreso único con fecha de hoy.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val dom = dayOfMonthText.toIntOrNull()?.coerceIn(1, 31) ?: 15
                    if (name.isNotBlank() && amount > 0.0) {
                        onSave(
                            IncomeEntity(
                                name = name.trim(),
                                amount = amount,
                                frequency = frequency,
                                dayOfWeek = if (frequency == RecurrenceFrequency.SEMANAL) dayOfWeek else null,
                                dayOfMonth = if (frequency == RecurrenceFrequency.MENSUAL) dom else null,
                                anchorDateEpochDay = LocalDate.now().toEpochDay()
                            )
                        )
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun AddDebtDialog(
    onDismiss: () -> Unit,
    onSave: (DebtEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var totalText by remember { mutableStateOf("") }
    var installmentText by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf(RecurrenceFrequency.MENSUAL) }
    var dayOfWeek by remember { mutableIntStateOf(5) }
    var dayOfMonthText by remember { mutableStateOf("15") }
    var reminderEnabled by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agendar deuda") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre (ej. Tarjeta, Préstamo, Auto)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = totalText,
                    onValueChange = { totalText = it },
                    label = { Text("Saldo total actual (MXN)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = installmentText,
                    onValueChange = { installmentText = it },
                    label = { Text("Pago por cuota (MXN)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("¿Cada cuánto la pagas?", style = MaterialTheme.typography.labelMedium)
                FrequencySelector(
                    selected = frequency,
                    onSelect = { frequency = it },
                    includeOneTime = false
                )
                when (frequency) {
                    RecurrenceFrequency.SEMANAL -> {
                        DayOfWeekSelector(
                            selectedIsoDay = dayOfWeek,
                            onSelect = { dayOfWeek = it }
                        )
                    }
                    RecurrenceFrequency.MENSUAL -> {
                        OutlinedTextField(
                            value = dayOfMonthText,
                            onValueChange = { dayOfMonthText = it },
                            label = { Text("Día límite de pago cada mes (1-31)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    else -> {
                        Text(
                            text = "Se agendará cada 14 días a partir de hoy.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Notificarme 1 día antes", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val total = totalText.toDoubleOrNull() ?: 0.0
                    val installment = installmentText.toDoubleOrNull() ?: 0.0
                    val dom = dayOfMonthText.toIntOrNull()?.coerceIn(1, 31) ?: 15
                    if (name.isNotBlank() && total > 0.0 && installment > 0.0) {
                        onSave(
                            DebtEntity(
                                name = name.trim(),
                                totalAmount = total,
                                remainingAmount = total,
                                installmentAmount = installment,
                                frequency = frequency,
                                dayOfWeek = if (frequency == RecurrenceFrequency.SEMANAL) dayOfWeek else null,
                                dayOfMonth = if (frequency == RecurrenceFrequency.MENSUAL) dom else null,
                                anchorDateEpochDay = LocalDate.now().toEpochDay(),
                                reminderEnabled = reminderEnabled,
                                reminderDaysBefore = 1
                            )
                        )
                    }
                }
            ) { Text("Agendar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
