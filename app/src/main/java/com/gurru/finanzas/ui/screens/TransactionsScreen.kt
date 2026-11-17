package com.gurru.finanzas.ui.screens

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gurru.finanzas.data.local.entity.ExpenseEntity
import com.gurru.finanzas.data.model.ExpenseCategory
import com.gurru.finanzas.data.model.ExpenseType
import com.gurru.finanzas.ui.FinanceUiState
import com.gurru.finanzas.ui.components.EmptyStateCard
import com.gurru.finanzas.ui.components.formatCurrencyMxn
import com.gurru.finanzas.ui.components.formatLocalDateEs
import com.gurru.finanzas.ui.theme.AmberWarning
import com.gurru.finanzas.ui.theme.CoralAlert
import java.time.LocalDate

@Composable
fun TransactionsScreen(
    state: FinanceUiState,
    onAddExpense: (ExpenseEntity) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<ExpenseType?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredExpenses = remember(state.expenses, selectedFilter) {
        if (selectedFilter == null) state.expenses
        else state.expenses.filter { it.expenseType == selectedFilter }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar gasto")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Control de Gastos",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Registra gastos hormiga, fijos y variables",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Detector de Gastos Hormiga
            item {
                val hormigaMes = state.projection.currentMonthHormiga
                val hormigaAnio = hormigaMes * 12.0
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AmberWarning.copy(alpha = 0.14f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Detector de Gastos Hormiga",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AmberWarning
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Este mes: ${formatCurrencyMxn(hormigaMes)} MXN",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Proyección anual si mantienes este ritmo: ${formatCurrencyMxn(hormigaAnio)} MXN",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Filtros
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("Todos") }
                    )
                    ExpenseType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedFilter == type,
                            onClick = { selectedFilter = type },
                            label = { Text(type.label) }
                        )
                    }
                }
            }

            if (filteredExpenses.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "Sin gastos registrados",
                        subtitle = "Toca el botón + para anotar un gasto fijo, variable o un gasto hormiga (café, snack, propina)."
                    )
                }
            } else {
                items(filteredExpenses, key = { it.id }) { expense ->
                    ExpenseRow(
                        expense = expense,
                        onDelete = { onDeleteExpense(expense) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            onDismiss = { showAddDialog = false },
            onSave = {
                onAddExpense(it)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ExpenseRow(
    expense: ExpenseEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${expense.expenseType.label} • ${expense.category.label}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatLocalDateEs(LocalDate.ofEpochDay(expense.dateEpochDay)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "-${formatCurrencyMxn(expense.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CoralAlert
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onSave: (ExpenseEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ExpenseType.HORMIGA) }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.CAFE_SNACKS) }

    val quickHormigaPresets = listOf(
        "Café" to 55.0,
        "Snack / Refresco" to 35.0,
        "Propina" to 25.0,
        "App / Delivery" to 80.0
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar gasto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (selectedType == ExpenseType.HORMIGA) {
                    Text(
                        text = "Atajos de gasto hormiga",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickHormigaPresets.forEach { (presetName, presetAmount) ->
                            AssistChip(
                                onClick = {
                                    title = presetName
                                    amountText = presetAmount.toInt().toString()
                                    selectedCategory = ExpenseCategory.CAFE_SNACKS
                                },
                                label = { Text("$presetName ($${presetAmount.toInt()})") }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Concepto (ej. Café, Renta, Súper)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Monto (MXN)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Tipo de gasto",
                    style = MaterialTheme.typography.labelMedium
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExpenseType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.label) }
                        )
                    }
                }

                Text(
                    text = "Categoría",
                    style = MaterialTheme.typography.labelMedium
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExpenseCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amount > 0.0) {
                        onSave(
                            ExpenseEntity(
                                title = title.trim(),
                                amount = amount,
                                expenseType = selectedType,
                                category = selectedCategory,
                                budgetBucket = selectedCategory.defaultBucket,
                                dateEpochDay = LocalDate.now().toEpochDay(),
                                isRecurringFixed = selectedType == ExpenseType.FIJO
                            )
                        )
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
