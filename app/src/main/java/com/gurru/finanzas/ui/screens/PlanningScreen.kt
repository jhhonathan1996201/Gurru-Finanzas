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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gurru.finanzas.data.local.entity.SavingsGoalEntity
import com.gurru.finanzas.ui.FinanceUiState
import com.gurru.finanzas.ui.components.EmptyStateCard
import com.gurru.finanzas.ui.components.SuggestionCard
import com.gurru.finanzas.ui.components.formatCurrencyMxn
import com.gurru.finanzas.ui.theme.AmberWarning
import com.gurru.finanzas.ui.theme.CoralAlert
import com.gurru.finanzas.ui.theme.EmeraldPrimary

@Composable
fun PlanningScreen(
    state: FinanceUiState,
    onSaveGoal: (SavingsGoalEntity) -> Unit,
    onAddToGoal: (SavingsGoalEntity, Double) -> Unit,
    onDeleteGoal: (SavingsGoalEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddGoal by remember { mutableStateOf(false) }
    var depositGoal by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Planificación y Metas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Regla 50/30/20, alcancías y asesoría inteligente",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Regla 50/30/20
        item {
            val rule = state.budgetRule
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Regla 50 / 30 / 20",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    BudgetRuleBar(
                        label = "Necesidades (50% ideal)",
                        spent = rule.needsSpent,
                        target = rule.needsTarget
                    )
                    BudgetRuleBar(
                        label = "Deseos y estilo de vida (30% ideal)",
                        spent = rule.wantsSpent,
                        target = rule.wantsTarget
                    )
                    BudgetRuleBar(
                        label = "Ahorro y Deudas (20% mínimo)",
                        spent = rule.savingsAndDebtSpent,
                        target = rule.savingsDebtTarget,
                        higherIsBetter = true
                    )
                }
            }
        }

        // Metas de Ahorro / Alcancías
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Alcancías y Metas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedButton(onClick = { showAddGoal = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Nueva meta", modifier = Modifier.padding(start = 4.dp))
                }
            }
        }

        if (state.savingsGoals.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Sin alcancías aún",
                    subtitle = "Crea una meta para tu fondo de emergencia, un viaje o liquidar una compra."
                )
            }
        } else {
            items(state.savingsGoals, key = { it.id }) { goal ->
                SavingsGoalCard(
                    goal = goal,
                    onDepositClick = { depositGoal = goal },
                    onDelete = { onDeleteGoal(goal) }
                )
            }
        }

        // Todas las sugerencias
        item {
            Text(
                text = "Todas las recomendaciones",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        items(state.suggestions, key = { it.id }) { suggestion ->
            SuggestionCard(suggestion = suggestion)
        }
    }

    if (showAddGoal) {
        AddSavingsGoalDialog(
            onDismiss = { showAddGoal = false },
            onSave = {
                onSaveGoal(it)
                showAddGoal = false
            }
        )
    }

    depositGoal?.let { targetGoal ->
        DepositGoalDialog(
            goal = targetGoal,
            onDismiss = { depositGoal = null },
            onDeposit = { amount ->
                onAddToGoal(targetGoal, amount)
                depositGoal = null
            }
        )
    }
}

@Composable
private fun BudgetRuleBar(
    label: String,
    spent: Double,
    target: Double,
    higherIsBetter: Boolean = false
) {
    val ratio = if (target > 0) (spent / target).toFloat().coerceIn(0f, 1.2f) else 0f
    val barColor: Color = when {
        higherIsBetter -> if (spent >= target) EmeraldPrimary else AmberWarning
        spent > target && target > 0 -> CoralAlert
        else -> EmeraldPrimary
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium)
            Text(
                text = "${formatCurrencyMxn(spent)} / ${formatCurrencyMxn(target)}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { ratio.coerceIn(0f, 1f) },
            color = barColor,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SavingsGoalCard(
    goal: SavingsGoalEntity,
    onDepositClick: () -> Unit,
    onDelete: () -> Unit
) {
    val progress = if (goal.targetAmount > 0) {
        (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
    } else 0f

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
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${formatCurrencyMxn(goal.currentAmount)} de ${formatCurrencyMxn(goal.targetAmount)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = onDepositClick) {
                    Text("+ Abonar")
                }
            }
        }
    }
}

@Composable
private fun AddSavingsGoalDialog(
    onDismiss: () -> Unit,
    onSave: (SavingsGoalEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva alcancía / meta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre (ej. Fondo de emergencia)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Meta a reunir (MXN)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val target = targetText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && target > 0.0) {
                        onSave(SavingsGoalEntity(name = name.trim(), targetAmount = target))
                    }
                }
            ) { Text("Crear") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun DepositGoalDialog(
    goal: SavingsGoalEntity,
    onDismiss: () -> Unit,
    onDeposit: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Abonar a ${goal.name}") },
        text = {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Monto a abonar (MXN)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0.0) onDeposit(amount)
                }
            ) { Text("Abonar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
