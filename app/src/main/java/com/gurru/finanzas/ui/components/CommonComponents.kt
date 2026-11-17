package com.gurru.finanzas.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurru.finanzas.data.model.RecurrenceFrequency
import com.gurru.finanzas.domain.FinancialSuggestion
import com.gurru.finanzas.domain.SuggestionSeverity
import com.gurru.finanzas.ui.theme.AmberWarning
import com.gurru.finanzas.ui.theme.CoralAlert
import com.gurru.finanzas.ui.theme.EmeraldPrimary
import com.gurru.finanzas.ui.theme.InfoBlue
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val mxLocale = Locale("es", "MX")
private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", mxLocale)

fun formatCurrencyMxn(amount: Double): String {
    val formatted = String.format(mxLocale, "%,.2f", amount)
    return "$$formatted"
}

fun formatLocalDateEs(date: LocalDate): String = date.format(dateFormatter)

@Composable
fun SuggestionCard(
    suggestion: FinancialSuggestion,
    modifier: Modifier = Modifier
) {
    val (accentColor, icon) = when (suggestion.severity) {
        SuggestionSeverity.CRITICAL -> CoralAlert to Icons.Default.Error
        SuggestionSeverity.WARNING -> AmberWarning to Icons.Default.Warning
        SuggestionSeverity.TIP -> InfoBlue to Icons.Default.Lightbulb
        SuggestionSeverity.POSITIVE -> EmeraldPrimary to Icons.Default.CheckCircle
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.10f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = suggestion.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = suggestion.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun FrequencySelector(
    selected: RecurrenceFrequency,
    onSelect: (RecurrenceFrequency) -> Unit,
    includeOneTime: Boolean = true
) {
    val options = if (includeOneTime) {
        RecurrenceFrequency.entries
    } else {
        RecurrenceFrequency.entries.filter { it != RecurrenceFrequency.UNICO }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { freq ->
            FilterChip(
                selected = selected == freq,
                onClick = { onSelect(freq) },
                label = { Text(freq.label, style = MaterialTheme.typography.labelMedium) }
            )
        }
    }
}

@Composable
fun DayOfWeekSelector(
    selectedIsoDay: Int,
    onSelect: (Int) -> Unit
) {
    val days = listOf(
        1 to "Lun",
        2 to "Mar",
        3 to "Mié",
        4 to "Jue",
        5 to "Vie",
        6 to "Sáb",
        7 to "Dom"
    )
    Column {
        Text(
            text = "Día de la semana",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            days.forEach { (iso, label) ->
                FilterChip(
                    selected = selectedIsoDay == iso,
                    onClick = { onSelect(iso) },
                    label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
