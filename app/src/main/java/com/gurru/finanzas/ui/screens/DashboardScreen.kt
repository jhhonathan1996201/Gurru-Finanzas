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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurru.finanzas.domain.ScheduledEvent
import com.gurru.finanzas.ui.FinanceUiState
import com.gurru.finanzas.ui.components.EmptyStateCard
import com.gurru.finanzas.ui.components.SuggestionCard
import com.gurru.finanzas.ui.components.formatCurrencyMxn
import com.gurru.finanzas.ui.components.formatLocalDateEs
import com.gurru.finanzas.ui.theme.CoralAlert
import com.gurru.finanzas.ui.theme.EmeraldPrimary

@Composable
fun DashboardScreen(
    state: FinanceUiState,
    modifier: Modifier = Modifier
) {
    val projection = state.projection

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Gurru Finanzas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tu centro de control financiero",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Tarjeta principal de flujo de caja
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Balance mensual proyectado",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${formatCurrencyMxn(projection.estimatedMonthlyBalance)} MXN",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val nextPay = projection.nextIncomeEvent
                    val payInfo = if (nextPay != null) {
                        val daysLabel = when (nextPay.daysUntil) {
                            0L -> "hoy"
                            1L -> "mañana"
                            else -> "en ${nextPay.daysUntil} días"
                        }
                        "Próximo ingreso (${nextPay.title}): $daysLabel • Presupuesto diario seguro: ${formatCurrencyMxn(projection.safeDailyBudgetUntilNextPay)}"
                    } else {
                        "Registra un ingreso en Agenda para calcular tu presupuesto diario seguro."
                    }
                    Text(
                        text = payInfo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f)
                    )
                }
            }
        }

        // Cuadrícula resumen 2x2
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricMiniCard(
                        title = "Ingresos / mes",
                        value = formatCurrencyMxn(projection.monthlyIncome),
                        accent = EmeraldPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Deudas / mes",
                        value = formatCurrencyMxn(projection.monthlyDebtCommitments),
                        accent = CoralAlert,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricMiniCard(
                        title = "Gastos del mes",
                        value = formatCurrencyMxn(projection.currentMonthExpenses),
                        accent = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Gastos hormiga",
                        value = formatCurrencyMxn(projection.currentMonthHormiga),
                        accent = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Sugerencias inteligentes
        item {
            Text(
                text = "Sugerencias inteligentes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (state.suggestions.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Sin alertas por ahora",
                    subtitle = "Agrega tus ingresos, deudas y gastos para recibir recomendaciones personalizadas."
                )
            }
        } else {
            items(state.suggestions.take(3), key = { it.id }) { suggestion ->
                SuggestionCard(suggestion = suggestion)
            }
        }

        // Próximos movimientos agendados
        item {
            Text(
                text = "Próximos pagos e ingresos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (projection.upcomingEvents.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Agenda vacía",
                    subtitle = "Ve a la pestaña Agenda para programar tus días de cobro y fechas de pago de deudas."
                )
            }
        } else {
            items(projection.upcomingEvents.take(6)) { event ->
                ScheduledEventRow(event = event)
            }
        }
    }
}

@Composable
private fun MetricMiniCard(
    title: String,
    value: String,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
    }
}

@Composable
private fun ScheduledEventRow(event: ScheduledEvent) {
    val color = if (event.isIncome) EmeraldPrimary else CoralAlert
    val icon = if (event.isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward
    val daysText = when (event.daysUntil) {
        0L -> "Hoy"
        1L -> "Mañana"
        else -> "En ${event.daysUntil} días"
    }

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = color)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.height(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${formatLocalDateEs(event.date)} • $daysText",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Text(
                text = "${if (event.isIncome) "+" else "-"}${formatCurrencyMxn(event.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
