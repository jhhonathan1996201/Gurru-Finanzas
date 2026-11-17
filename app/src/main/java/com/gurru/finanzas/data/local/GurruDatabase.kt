package com.gurru.finanzas.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gurru.finanzas.data.local.dao.DebtDao
import com.gurru.finanzas.data.local.dao.ExpenseDao
import com.gurru.finanzas.data.local.dao.IncomeDao
import com.gurru.finanzas.data.local.dao.SavingsGoalDao
import com.gurru.finanzas.data.local.entity.DebtEntity
import com.gurru.finanzas.data.local.entity.ExpenseEntity
import com.gurru.finanzas.data.local.entity.IncomeEntity
import com.gurru.finanzas.data.local.entity.SavingsGoalEntity

@Database(
    entities = [
        IncomeEntity::class,
        DebtEntity::class,
        ExpenseEntity::class,
        SavingsGoalEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GurruDatabase : RoomDatabase() {
    abstract fun incomeDao(): IncomeDao
    abstract fun debtDao(): DebtDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun savingsGoalDao(): SavingsGoalDao

    companion object {
        @Volatile
        private var INSTANCE: GurruDatabase? = null

        fun getInstance(context: Context): GurruDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GurruDatabase::class.java,
                    "gurru_finanzas.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
