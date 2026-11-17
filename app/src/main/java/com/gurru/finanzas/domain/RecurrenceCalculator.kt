package com.gurru.finanzas.domain

import com.gurru.finanzas.data.model.RecurrenceFrequency
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.math.min

object RecurrenceCalculator {

    /**
     * Calcula la próxima fecha de ocurrencia (>= fromDate) según la frecuencia configurada.
     * Devuelve null únicamente si es UNICO y su fecha ya pasó.
     */
    fun nextOccurrence(
        frequency: RecurrenceFrequency,
        dayOfWeek: Int?,
        dayOfMonth: Int?,
        anchorDateEpochDay: Long,
        fromDate: LocalDate = LocalDate.now()
    ): LocalDate? {
        val anchor = LocalDate.ofEpochDay(anchorDateEpochDay)
        return when (frequency) {
            RecurrenceFrequency.SEMANAL -> {
                val targetDow = DayOfWeek.of((dayOfWeek ?: anchor.dayOfWeek.value).coerceIn(1, 7))
                fromDate.with(TemporalAdjusters.nextOrSame(targetDow))
            }

            RecurrenceFrequency.QUINCENAL -> {
                if (!anchor.isBefore(fromDate)) {
                    anchor
                } else {
                    val daysBetween = ChronoUnit.DAYS.between(anchor, fromDate)
                    val periods = (daysBetween + 13) / 14
                    anchor.plusDays(periods * 14)
                }
            }

            RecurrenceFrequency.MENSUAL -> {
                val targetDay = (dayOfMonth ?: anchor.dayOfMonth).coerceIn(1, 31)
                val thisMonthDate = fromDate.withDayOfMonth(min(targetDay, fromDate.lengthOfMonth()))
                if (!thisMonthDate.isBefore(fromDate)) {
                    thisMonthDate
                } else {
                    val nextMonth = fromDate.plusMonths(1)
                    nextMonth.withDayOfMonth(min(targetDay, nextMonth.lengthOfMonth()))
                }
            }

            RecurrenceFrequency.UNICO -> {
                if (!anchor.isBefore(fromDate)) anchor else null
            }
        }
    }

    /**
     * Convierte un monto recurrente a su equivalente mensual promedio.
     */
    fun monthlyEquivalent(amount: Double, frequency: RecurrenceFrequency): Double = when (frequency) {
        RecurrenceFrequency.SEMANAL -> amount * (52.0 / 12.0)
        RecurrenceFrequency.QUINCENAL -> amount * (26.0 / 12.0)
        RecurrenceFrequency.MENSUAL -> amount
        RecurrenceFrequency.UNICO -> 0.0
    }

    fun daysUntil(targetDate: LocalDate, fromDate: LocalDate = LocalDate.now()): Long {
        return ChronoUnit.DAYS.between(fromDate, targetDate)
    }

    fun describeSchedule(
        frequency: RecurrenceFrequency,
        dayOfWeek: Int?,
        dayOfMonth: Int?,
        anchorDateEpochDay: Long
    ): String {
        return when (frequency) {
            RecurrenceFrequency.SEMANAL -> {
                val dow = DayOfWeek.of((dayOfWeek ?: 5).coerceIn(1, 7))
                "Cada ${com.gurru.finanzas.data.model.dayOfWeekToSpanish(dow)}"
            }
            RecurrenceFrequency.QUINCENAL -> "Cada 15 días (quincenal)"
            RecurrenceFrequency.MENSUAL -> "Día ${dayOfMonth ?: 1} de cada mes"
            RecurrenceFrequency.UNICO -> "Fecha única: ${LocalDate.ofEpochDay(anchorDateEpochDay)}"
        }
    }
}
