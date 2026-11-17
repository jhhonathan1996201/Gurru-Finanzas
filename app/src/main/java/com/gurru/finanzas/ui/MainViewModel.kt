package com.gurru.finanzas.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gurru.finanzas.data.local.entity.DebtEntity
import com.gurru.finanzas.data.local.entity.ExpenseEntity
import com.gurru.finanzas.data.local.entity.IncomeEntity
import com.gurru.finanzas.data.local.entity.SavingsGoalEntity
import com.gurru.finanzas.data.repository.FinanceRepository
import com.gurru.finanzas.domain.BudgetRule503020
import com.gurru.finanzas.domain.CashFlowProjection
import com.gurru.finanzas.domain.CashFlowProjector
import com.gurru.finanzas.domain.FinancialAdvisorEngine
import com.gurru.finanzas.domain.FinancialSuggestion
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FinanceUiState(
    val incomes: List<IncomeEntity> = emptyList(),
    val debts: List<DebtEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val savingsGoals: List<SavingsGoalEntity> = emptyList(),
    val projection: CashFlowProjection = CashFlowProjector.project(emptyList(), emptyList(), emptyList()),
    val budgetRule: BudgetRule503020 = BudgetRule503020(0.0, 0.0, 0.0, 0.0),
    val suggestions: List<FinancialSuggestion> = emptyList()
)

class MainViewModel(private val repository: FinanceRepository) : ViewModel() {

    val uiState: StateFlow<FinanceUiState> = combine(
        repository.incomes,
        repository.debts,
        repository.expenses,
        repository.savingsGoals
    ) { incomes, debts, expenses, goals ->
        val projection = CashFlowProjector.project(incomes, debts, expenses)
        val rule = FinancialAdvisorEngine.computeBudgetRule(projection, expenses)
        val suggestions = FinancialAdvisorEngine.generateSuggestions(projection, debts, expenses, goals)
        FinanceUiState(
            incomes = incomes,
            debts = debts,
            expenses = expenses,
            savingsGoals = goals,
            projection = projection,
            budgetRule = rule,
            suggestions = suggestions
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FinanceUiState()
    )

    fun saveIncome(income: IncomeEntity) = viewModelScope.launch {
        repository.saveIncome(income)
    }

    fun deleteIncome(income: IncomeEntity) = viewModelScope.launch {
        repository.deleteIncome(income)
    }

    fun saveDebt(debt: DebtEntity) = viewModelScope.launch {
        repository.saveDebt(debt)
    }

    fun payDebtInstallment(debt: DebtEntity) = viewModelScope.launch {
        repository.registerDebtPayment(debt, debt.installmentAmount)
    }

    fun deleteDebt(debt: DebtEntity) = viewModelScope.launch {
        repository.deleteDebt(debt)
    }

    fun saveExpense(expense: ExpenseEntity) = viewModelScope.launch {
        repository.saveExpense(expense)
    }

    fun deleteExpense(expense: ExpenseEntity) = viewModelScope.launch {
        repository.deleteExpense(expense)
    }

    fun saveSavingsGoal(goal: SavingsGoalEntity) = viewModelScope.launch {
        repository.saveSavingsGoal(goal)
    }

    fun addToSavingsGoal(goal: SavingsGoalEntity, amount: Double) = viewModelScope.launch {
        repository.addToSavingsGoal(goal, amount)
    }

    fun deleteSavingsGoal(goal: SavingsGoalEntity) = viewModelScope.launch {
        repository.deleteSavingsGoal(goal)
    }

    class Factory(private val repository: FinanceRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repository) as T
        }
    }
}
