package com.gurru.finanzas.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.gurru.finanzas.data.local.GurruDatabase
import com.gurru.finanzas.domain.RecurrenceCalculator
import java.time.LocalDate
import java.util.Locale
import java.util.concurrent.TimeUnit

class PaymentReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val db = GurruDatabase.getInstance(applicationContext)
        val today = LocalDate.now()

        val debts = db.debtDao().getActiveOnce().filter { it.reminderEnabled }
        debts.forEach { debt ->
            val nextDue = RecurrenceCalculator.nextOccurrence(
                debt.frequency,
                debt.dayOfWeek,
                debt.dayOfMonth,
                debt.anchorDateEpochDay,
                today
            ) ?: return@forEach

            val daysUntil = RecurrenceCalculator.daysUntil(nextDue, today)
            if (daysUntil in 0L..debt.reminderDaysBefore.toLong()) {
                val whenLabel = when (daysUntil) {
                    0L -> "hoy"
                    1L -> "mañana"
                    else -> "en $daysUntil días"
                }
                val amountStr = String.format(Locale("es", "MX"), "%,.2f", debt.installmentAmount)
                NotificationHelper.showReminderNotification(
                    context = applicationContext,
                    notificationId = (10_000 + debt.id).toInt(),
                    title = "Recordatorio de pago: ${debt.name}",
                    body = "Tu pago de $$amountStr MXN vence $whenLabel."
                )
            }
        }

        val incomes = db.incomeDao().getActiveOnce()
        incomes.forEach { income ->
            val nextPay = RecurrenceCalculator.nextOccurrence(
                income.frequency,
                income.dayOfWeek,
                income.dayOfMonth,
                income.anchorDateEpochDay,
                today
            ) ?: return@forEach

            if (nextPay == today) {
                val amountStr = String.format(Locale("es", "MX"), "%,.2f", income.amount)
                NotificationHelper.showReminderNotification(
                    context = applicationContext,
                    notificationId = (20_000 + income.id).toInt(),
                    title = "Ingreso programado hoy: ${income.name}",
                    body = "Hoy corresponde recibir $$amountStr MXN. ¡Organiza tus apartados!"
                )
            }
        }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "gurru_daily_payment_reminders"

        fun scheduleDaily(context: Context) {
            val request = PeriodicWorkRequestBuilder<PaymentReminderWorker>(
                24, TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
