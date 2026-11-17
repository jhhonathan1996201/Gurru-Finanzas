package com.gurru.finanzas

import com.gurru.finanzas.data.local.entity.ExpenseEntity
import com.gurru.finanzas.data.local.entity.IncomeEntity
import com.gurru.finanzas.data.model.BudgetBucket
import com.gurru.finanzas.data.model.ExpenseCategory
import com.gurru.finanzas.data.model.ExpenseType
import com.gurru.finanzas.data.model.RecurrenceFrequency
import com.gurru.finanzas.domain.CashFlowProjector
import com.gurru.finanzas.domain.FinancialAdvisorEngine
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class FinancialAdvisorEngineTest {

    @Test
    fun highAntExpenses_triggersHormigaWarning() {
        val today = LocalDate.of(2026, 11, 16)
        val incomes = listOf(
            IncomeEntity(
                id = 1,
                name = "Sueldo",
                amount = 10_000.0,
                frequency = RecurrenceFrequency.MENSUAL,
                dayOfMonth = 15,
                anchorDateEpochDay = today.toEpochDay()
            )
        )
        val expenses = listOf(
            ExpenseEntity(
                id = 1,
                title = "Cafés y snacks",
                amount = 2_000.0,
                expenseType = ExpenseType.HORMIGA,
                category = ExpenseCategory.CAFE_SNACKS,
                budgetBucket = BudgetBucket.DESEO,
                dateEpochDay = today.toEpochDay()
            )
        )

        val projection = CashFlowProjector.project(incomes, emptyList(), expenses, today)
        val suggestions = FinancialAdvisorEngine.generateSuggestions(
            projection = projection,
            debts = emptyList(),
            expenses = expenses,
            goals = emptyList(),
            today = today
        )

        assertTrue(suggestions.any { it.id == "hormiga_high" })
    }
}
