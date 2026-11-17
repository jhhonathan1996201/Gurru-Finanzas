package com.gurru.finanzas.domain

import com.gurru.finanzas.data.local.entity.DebtEntity
import com.gurru.finanzas.data.local.entity.ExpenseEntity
import com.gurru.finanzas.data.local.entity.IncomeEntity
import com.gurru.finanzas.data.model.ExpenseType
import com.gurru.finanzas.data.model.RecurrenceFrequency
import java.time.LocalDate
import kotlin.math.max

data class ScheduledEvent(
    val title: String,
    val amount: Double,
    val date: LocalDate,
    val daysUntil: Long,
    val isIncome: Boolean
)

data class CashFlowProjection(
    val monthlyIncome: Double,
    val monthlyDebtCommitments: Double,
    val monthlyFixedExpenses: Double,
    val currentMonthExpenses: Double,
    val currentMonthHormiga: Double,
    val estimatedMonthlyBalance: Double,
    val nextIncomeEvent: ScheduledEvent?,
    val upcomingDebtsBeforeNextIncome: List<ScheduledEvent>,
    val safeDailyBudgetUntilNextPay: Double,
    val upcomingEvents: List<ScheduledEvent>
)

object CashFlowProjector {

    fun project(
        incomes: List<IncomeEntity>,
        debts: List<DebtEntity>,
        expenses: List<ExpenseEntity>,
        today: LocalDate = LocalDate.now()
    ): CashFlowProjection {
        val activeIncomes = incomes.filter { it.isActive }
        val activeDebts = debts.filter { !it.isPaidOff && it.remainingAmount > 0.0 }

        val monthlyIncome = activeIncomes.sumOf {
            RecurrenceCalculator.monthlyEquivalent(it.amount, it.frequency)
        } + activeIncomes.filter {
            it.frequency == RecurrenceFrequency.UNICO &&
                LocalDate.ofEpochDay(it.anchorDateEpochDay).month == today.month &&
                LocalDate.ofEpochDay(it.anchorDateEpochDay).year == today.year
        }.sumOf { it.amount }

        val monthlyDebtCommitments = activeDebts.sumOf {
            RecurrenceCalculator.monthlyEquivalent(it.installmentAmount, it.frequency)
        }

        val currentMonthExpensesList = expenses.filter {
            val d = LocalDate.ofEpochDay(it.dateEpochDay)
            d.year == today.year && d.month == today.month
        }

        val currentMonthExpenses = currentMonthExpensesList.sumOf { it.amount }
        val currentMonthHormiga = currentMonthExpensesList
            .filter { it.expenseType == ExpenseType.HORMIGA }
            .sumOf { it.amount }

        val monthlyFixedExpenses = currentMonthExpensesList
            .filter { it.expenseType == ExpenseType.FIJO }
            .sumOf { it.amount }

        val estimatedMonthlyBalance = monthlyIncome - monthlyDebtCommitments - currentMonthExpenses

        val incomeEvents = activeIncomes.mapNotNull { income ->
            val nextDate = RecurrenceCalculator.nextOccurrence(
                income.frequency,
                income.dayOfWeek,
                income.dayOfMonth,
                income.anchorDateEpochDay,
                today
            ) ?: return@mapNotNull null
            ScheduledEvent(
                title = income.name,
                amount = income.amount,
                date = nextDate,
                daysUntil = RecurrenceCalculator.daysUntil(nextDate, today),
                isIncome = true
            )
        }.sortedBy { it.date }

        val debtEvents = activeDebts.mapNotNull { debt ->
            val nextDate = RecurrenceCalculator.nextOccurrence(
                debt.frequency,
                debt.dayOfWeek,
                debt.dayOfMonth,
                debt.anchorDateEpochDay,
                today
            ) ?: return@mapNotNull null
            ScheduledEvent(
                title = debt.name,
                amount = debt.installmentAmount,
                date = nextDate,
                daysUntil = RecurrenceCalculator.daysUntil(nextDate, today),
                isIncome = false
            )
        }.sortedBy { it.date }

        val nextIncome = incomeEvents.firstOrNull()
        val horizonDays = max(1L, nextIncome?.daysUntil ?: 15L)

        val upcomingDebtsBeforePay = if (nextIncome != null) {
            debtEvents.filter { !it.date.isAfter(nextIncome.date) }
        } else {
            debtEvents.filter { it.daysUntil <= 15L }
        }

        val debtsSumUntilPay = upcomingDebtsBeforePay.sumOf { it.amount }
        val proratedAvailable = max(0.0, (estimatedMonthlyBalance / 30.0) * horizonDays - debtsSumUntilPay)
        val safeDaily = proratedAvailable / horizonDays.toDouble()

        val allEvents = (incomeEvents + debtEvents).sortedBy { it.date }

        return CashFlowProjection(
            monthlyIncome = monthlyIncome,
            monthlyDebtCommitments = monthlyDebtCommitments,
            monthlyFixedExpenses = monthlyFixedExpenses,
            currentMonthExpenses = currentMonthExpenses,
            currentMonthHormiga = currentMonthHormiga,
            estimatedMonthlyBalance = estimatedMonthlyBalance,
            nextIncomeEvent = nextIncome,
            upcomingDebtsBeforeNextIncome = upcomingDebtsBeforePay,
            safeDailyBudgetUntilNextPay = safeDaily,
            upcomingEvents = allEvents
        )
    }
}
