package com.gurru.finanzas

import com.gurru.finanzas.data.model.RecurrenceFrequency
import com.gurru.finanzas.domain.RecurrenceCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class RecurrenceCalculatorTest {

    @Test
    fun weeklyNextOccurrence_returnsNextFriday() {
        // 2026-11-16 es Lunes
        val monday = LocalDate.of(2026, 11, 16)
        val next = RecurrenceCalculator.nextOccurrence(
            frequency = RecurrenceFrequency.SEMANAL,
            dayOfWeek = DayOfWeek.FRIDAY.value,
            dayOfMonth = null,
            anchorDateEpochDay = monday.toEpochDay(),
            fromDate = monday
        )
        assertEquals(LocalDate.of(2026, 11, 20), next)
    }

    @Test
    fun monthlyNextOccurrence_clampsToFebruaryLength() {
        val feb1 = LocalDate.of(2026, 2, 1)
        val next = RecurrenceCalculator.nextOccurrence(
            frequency = RecurrenceFrequency.MENSUAL,
            dayOfWeek = null,
            dayOfMonth = 31,
            anchorDateEpochDay = feb1.toEpochDay(),
            fromDate = feb1
        )
        assertEquals(LocalDate.of(2026, 2, 28), next)
    }

    @Test
    fun biweeklyNextOccurrence_advancesIn14DaySteps() {
        val anchor = LocalDate.of(2026, 11, 1)
        val today = LocalDate.of(2026, 11, 16)
        val next = RecurrenceCalculator.nextOccurrence(
            frequency = RecurrenceFrequency.QUINCENAL,
            dayOfWeek = null,
            dayOfMonth = null,
            anchorDateEpochDay = anchor.toEpochDay(),
            fromDate = today
        )
        assertEquals(LocalDate.of(2026, 11, 29), next)
    }

    @Test
    fun oneTimeInPast_returnsNull() {
        val past = LocalDate.of(2026, 10, 1)
        val today = LocalDate.of(2026, 11, 16)
        val next = RecurrenceCalculator.nextOccurrence(
            frequency = RecurrenceFrequency.UNICO,
            dayOfWeek = null,
            dayOfMonth = null,
            anchorDateEpochDay = past.toEpochDay(),
            fromDate = today
        )
        assertNull(next)
    }
}
