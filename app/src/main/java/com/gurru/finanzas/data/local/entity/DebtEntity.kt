package com.gurru.finanzas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gurru.finanzas.data.model.RecurrenceFrequency

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val creditor: String = "",
    val totalAmount: Double,
    val remainingAmount: Double,
    val installmentAmount: Double,
    val frequency: RecurrenceFrequency,
    /** ISO day of week (1=Lunes .. 7=Domingo) for SEMANAL. */
    val dayOfWeek: Int? = null,
    /** Day of month (1..31) for MENSUAL. */
    val dayOfMonth: Int? = null,
    /** EpochDay of a known due date; used as anchor for QUINCENAL or exact date for UNICO. */
    val anchorDateEpochDay: Long,
    val reminderEnabled: Boolean = true,
    val reminderDaysBefore: Int = 1,
    val isPaidOff: Boolean = false
)
