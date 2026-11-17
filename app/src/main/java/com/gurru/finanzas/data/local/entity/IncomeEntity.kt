package com.gurru.finanzas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gurru.finanzas.data.model.RecurrenceFrequency

@Entity(tableName = "incomes")
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Double,
    val frequency: RecurrenceFrequency,
    /** ISO day of week (1=Lunes .. 7=Domingo) for SEMANAL. */
    val dayOfWeek: Int? = null,
    /** Day of month (1..31) for MENSUAL. */
    val dayOfMonth: Int? = null,
    /** EpochDay of a known pay date; used as anchor for QUINCENAL or exact date for UNICO. */
    val anchorDateEpochDay: Long,
    val isActive: Boolean = true,
    val notes: String = ""
)
