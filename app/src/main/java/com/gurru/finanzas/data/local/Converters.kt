package com.gurru.finanzas.data.local

import androidx.room.TypeConverter
import com.gurru.finanzas.data.model.BudgetBucket
import com.gurru.finanzas.data.model.ExpenseCategory
import com.gurru.finanzas.data.model.ExpenseType
import com.gurru.finanzas.data.model.RecurrenceFrequency

class Converters {
    @TypeConverter
    fun fromFrequency(value: RecurrenceFrequency): String = value.name

    @TypeConverter
    fun toFrequency(value: String): RecurrenceFrequency = RecurrenceFrequency.valueOf(value)

    @TypeConverter
    fun fromExpenseType(value: ExpenseType): String = value.name

    @TypeConverter
    fun toExpenseType(value: String): ExpenseType = ExpenseType.valueOf(value)

    @TypeConverter
    fun fromBudgetBucket(value: BudgetBucket): String = value.name

    @TypeConverter
    fun toBudgetBucket(value: String): BudgetBucket = BudgetBucket.valueOf(value)

    @TypeConverter
    fun fromExpenseCategory(value: ExpenseCategory): String = value.name

    @TypeConverter
    fun toExpenseCategory(value: String): ExpenseCategory = ExpenseCategory.valueOf(value)
}
