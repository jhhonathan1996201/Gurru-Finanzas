package com.gurru.finanzas.domain

import com.gurru.finanzas.data.local.entity.DebtEntity
import com.gurru.finanzas.data.local.entity.ExpenseEntity
import com.gurru.finanzas.data.local.entity.SavingsGoalEntity
import com.gurru.finanzas.data.model.BudgetBucket
import java.time.LocalDate

enum class SuggestionSeverity {
    CRITICAL,
    WARNING,
    TIP,
    POSITIVE
}

data class FinancialSuggestion(
    val id: String,
    val title: String,
    val message: String,
    val severity: SuggestionSeverity
)

data class BudgetRule503020(
    val monthlyIncome: Double,
    val needsSpent: Double,
    val wantsSpent: Double,
    val savingsAndDebtSpent: Double
) {
    val needsTarget: Double get() = monthlyIncome * 0.50
    val wantsTarget: Double get() = monthlyIncome * 0.30
    val savingsDebtTarget: Double get() = monthlyIncome * 0.20
}

object FinancialAdvisorEngine {

    fun computeBudgetRule(
        projection: CashFlowProjection,
        expenses: List<ExpenseEntity>,
        today: LocalDate = LocalDate.now()
    ): BudgetRule503020 {
        val monthExpenses = expenses.filter {
            val d = LocalDate.ofEpochDay(it.dateEpochDay)
            d.year == today.year && d.month == today.month
        }
        val needs = monthExpenses.filter { it.budgetBucket == BudgetBucket.NECESIDAD }.sumOf { it.amount }
        val wants = monthExpenses.filter { it.budgetBucket == BudgetBucket.DESEO }.sumOf { it.amount }
        val savingsDebt = monthExpenses.filter { it.budgetBucket == BudgetBucket.AHORRO_DEUDA }.sumOf { it.amount } +
            projection.monthlyDebtCommitments

        return BudgetRule503020(
            monthlyIncome = projection.monthlyIncome,
            needsSpent = needs,
            wantsSpent = wants,
            savingsAndDebtSpent = savingsDebt
        )
    }

    fun generateSuggestions(
        projection: CashFlowProjection,
        debts: List<DebtEntity>,
        expenses: List<ExpenseEntity>,
        goals: List<SavingsGoalEntity>,
        today: LocalDate = LocalDate.now()
    ): List<FinancialSuggestion> {
        val suggestions = mutableListOf<FinancialSuggestion>()

        if (projection.monthlyIncome <= 0.0) {
            suggestions += FinancialSuggestion(
                id = "no_income",
                title = "Registra tus ingresos recurrentes",
                message = "Agrega tu sueldo o ingresos (semanal, quincenal o mensual) en la pestaña Agenda para calcular tu presupuesto diario y proyectar tus pagos.",
                severity = SuggestionSeverity.TIP
            )
            return suggestions
        }

        // 1. Recordatorio urgente de deudas próximas (0 a 3 días)
        val urgentDebts = projection.upcomingEvents.filter { !it.isIncome && it.daysUntil in 0..3 }
        if (urgentDebts.isNotEmpty()) {
            val first = urgentDebts.first()
            val whenText = when (first.daysUntil) {
                0L -> "hoy"
                1L -> "mañana"
                else -> "en ${first.daysUntil} días"
            }
            suggestions += FinancialSuggestion(
                id = "urgent_debt_${first.title}",
                title = "Pago próximo: ${first.title}",
                message = "Tu pago de ${formatMxn(first.amount)} vence $whenText. Aparta este monto para evitar recargos.",
                severity = SuggestionSeverity.WARNING
            )
        }

        // 2. Detector de gastos hormiga
        val hormigaMonth = projection.currentMonthHormiga
        val hormigaRatio = hormigaMonth / projection.monthlyIncome
        if (hormigaMonth > 0.0) {
            val annualProjection = hormigaMonth * 12.0
            if (hormigaRatio >= 0.10 || hormigaMonth >= 1500.0) {
                suggestions += FinancialSuggestion(
                    id = "hormiga_high",
                    title = "Alerta de gastos hormiga",
                    message = "Llevas ${formatMxn(hormigaMonth)} en gastos hormiga este mes. Al año esto equivale a ${formatMxn(annualProjection)}. Reducir un 30% te ahorraría ${formatMxn(annualProjection * 0.30)} anuales.",
                    severity = SuggestionSeverity.WARNING
                )
            } else {
                suggestions += FinancialSuggestion(
                    id = "hormiga_ok",
                    title = "Gastos hormiga bajo control",
                    message = "Tus gastos hormiga suman ${formatMxn(hormigaMonth)} este mes (${(hormigaRatio * 100).toInt()}% de tu ingreso). Sigue registrándolos para mantenerlos a raya.",
                    severity = SuggestionSeverity.POSITIVE
                )
            }
        }

        // 3. Balance mensual negativo o muy ajustado
        if (projection.estimatedMonthlyBalance < 0.0) {
            suggestions += FinancialSuggestion(
                id = "negative_balance",
                title = "Déficit financiero detectado",
                message = "Tus deudas y gastos superan tus ingresos mensuales por ${formatMxn(-projection.estimatedMonthlyBalance)}. Prioriza recortar deseos y gastos hormiga esta quincena.",
                severity = SuggestionSeverity.CRITICAL
            )
        } else if (projection.estimatedMonthlyBalance > projection.monthlyIncome * 0.20) {
            suggestions += FinancialSuggestion(
                id = "healthy_surplus",
                title = "Excelente margen de ahorro",
                message = "Tienes un excedente estimado de ${formatMxn(projection.estimatedMonthlyBalance)} este mes. Puedes destinar una parte a tus alcancías o adelantar capital a tu deuda más pequeña.",
                severity = SuggestionSeverity.POSITIVE
            )
        }

        // 4. Regla 50/30/20
        val rule = computeBudgetRule(projection, expenses, today)
        if (rule.wantsSpent > rule.wantsTarget && rule.wantsTarget > 0) {
            suggestions += FinancialSuggestion(
                id = "wants_over_30",
                title = "Deseos por encima del 30%",
                message = "Has gastado ${formatMxn(rule.wantsSpent)} en deseos/estilo de vida (tu límite ideal 50/30/20 es ${formatMxn(rule.wantsTarget)}). Modera salidas y antojos hasta tu próximo pago.",
                severity = SuggestionSeverity.WARNING
            )
        }

        // 5. Estrategia de deudas (Bola de nieve)
        val activeDebts = debts.filter { !it.isPaidOff && it.remainingAmount > 0.0 }
        if (activeDebts.size >= 2) {
            val smallest = activeDebts.minByOrNull { it.remainingAmount }
            if (smallest != null) {
                suggestions += FinancialSuggestion(
                    id = "snowball_debt",
                    title = "Estrategia Bola de Nieve",
                    message = "Tu deuda más cercana a liquidarse es \"${smallest.name}\" (restan ${formatMxn(smallest.remainingAmount)}). Al liquidarla, libera su cuota para acelerar las demás.",
                    severity = SuggestionSeverity.TIP
                )
            }
        }

        // 6. Metas de ahorro
        val activeGoal = goals.firstOrNull { it.currentAmount < it.targetAmount }
        if (activeGoal != null && projection.estimatedMonthlyBalance > 0) {
            val missing = activeGoal.targetAmount - activeGoal.currentAmount
            suggestions += FinancialSuggestion(
                id = "goal_boost",
                title = "Impulsa tu meta: ${activeGoal.name}",
                message = "Te faltan ${formatMxn(missing)} para completar tu alcancía. Con tu flujo actual puedes apartar un abono semanal constante.",
                severity = SuggestionSeverity.TIP
            )
        }

        return suggestions
    }

    private fun formatMxn(amount: Double): String =
        "$${String.format(java.util.Locale("es", "MX"), "%,.0f", amount)} MXN"
}
