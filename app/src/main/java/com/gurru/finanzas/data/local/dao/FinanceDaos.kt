package com.gurru.finanzas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gurru.finanzas.data.local.entity.DebtEntity
import com.gurru.finanzas.data.local.entity.ExpenseEntity
import com.gurru.finanzas.data.local.entity.IncomeEntity
import com.gurru.finanzas.data.local.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Query("SELECT * FROM incomes ORDER BY isActive DESC, id DESC")
    fun observeAll(): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM incomes WHERE isActive = 1")
    suspend fun getActiveOnce(): List<IncomeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(income: IncomeEntity): Long

    @Update
    suspend fun update(income: IncomeEntity)

    @Delete
    suspend fun delete(income: IncomeEntity)
}

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY isPaidOff ASC, id DESC")
    fun observeAll(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE isPaidOff = 0")
    suspend fun getActiveOnce(): List<DebtEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(debt: DebtEntity): Long

    @Update
    suspend fun update(debt: DebtEntity)

    @Delete
    suspend fun delete(debt: DebtEntity)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY dateEpochDay DESC, id DESC")
    fun observeAll(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY dateEpochDay DESC")
    fun observeBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)
}

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals ORDER BY id DESC")
    fun observeAll(): Flow<List<SavingsGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: SavingsGoalEntity): Long

    @Update
    suspend fun update(goal: SavingsGoalEntity)

    @Delete
    suspend fun delete(goal: SavingsGoalEntity)
}
