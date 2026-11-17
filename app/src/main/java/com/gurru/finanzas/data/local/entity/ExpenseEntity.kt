package com.gurru.finanzas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gurru.finanzas.data.model.BudgetBucket
import com.gurru.finanzas.data.model.ExpenseCategory
import com.gurru.finanzas.data.model.ExpenseType

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val expenseType: ExpenseType,
    val category: ExpenseCategory,
    val budgetBucket: BudgetBucket,
    val dateEpochDay: Long,
    val isRecurringFixed: Boolean = false,
    val fixedDayOfMonth: Int? = null,
    val notes: String = ""
)
