package com.gurru.finanzas.data.repository

import com.gurru.finanzas.data.local.GurruDatabase
import com.gurru.finanzas.data.local.entity.DebtEntity
import com.gurru.finanzas.data.local.entity.ExpenseEntity
import com.gurru.finanzas.data.local.entity.IncomeEntity
import com.gurru.finanzas.data.local.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.math.max

class FinanceRepository(private val db: GurruDatabase) {
    private val incomeDao = db.incomeDao()
    private val debtDao = db.debtDao()
    private val expenseDao = db.expenseDao()
    private val savingsGoalDao = db.savingsGoalDao()

    val incomes: Flow<List<IncomeEntity>> = incomeDao.observeAll()
    val debts: Flow<List<DebtEntity>> = debtDao.observeAll()
    val expenses: Flow<List<ExpenseEntity>> = expenseDao.observeAll()
    val savingsGoals: Flow<List<SavingsGoalEntity>> = savingsGoalDao.observeAll()

    suspend fun getActiveIncomesOnce(): List<IncomeEntity> = incomeDao.getActiveOnce()
    suspend fun getActiveDebtsOnce(): List<DebtEntity> = debtDao.getActiveOnce()

    suspend fun saveIncome(income: IncomeEntity) {
        if (income.id == 0L) incomeDao.insert(income) else incomeDao.update(income)
    }

    suspend fun deleteIncome(income: IncomeEntity) = incomeDao.delete(income)

    suspend fun saveDebt(debt: DebtEntity) {
        if (debt.id == 0L) debtDao.insert(debt) else debtDao.update(debt)
    }

    suspend fun registerDebtPayment(debt: DebtEntity, amountPaid: Double = debt.installmentAmount) {
        val newRemaining = max(0.0, debt.remainingAmount - amountPaid)
        debtDao.update(
            debt.copy(
                remainingAmount = newRemaining,
                isPaidOff = newRemaining <= 0.01
            )
        )
    }

    suspend fun deleteDebt(debt: DebtEntity) = debtDao.delete(debt)

    suspend fun saveExpense(expense: ExpenseEntity) {
        if (expense.id == 0L) expenseDao.insert(expense) else expenseDao.update(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) = expenseDao.delete(expense)

    suspend fun saveSavingsGoal(goal: SavingsGoalEntity) {
        if (goal.id == 0L) savingsGoalDao.insert(goal) else savingsGoalDao.update(goal)
    }

    suspend fun addToSavingsGoal(goal: SavingsGoalEntity, amount: Double) {
        savingsGoalDao.update(goal.copy(currentAmount = goal.currentAmount + amount))
    }

    suspend fun deleteSavingsGoal(goal: SavingsGoalEntity) = savingsGoalDao.delete(goal)
}
