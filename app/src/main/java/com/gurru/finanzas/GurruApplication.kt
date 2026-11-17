package com.gurru.finanzas

import android.app.Application
import com.gurru.finanzas.data.local.GurruDatabase
import com.gurru.finanzas.data.repository.FinanceRepository
import com.gurru.finanzas.notifications.NotificationHelper
import com.gurru.finanzas.notifications.PaymentReminderWorker

class GurruApplication : Application() {
    val database: GurruDatabase by lazy { GurruDatabase.getInstance(this) }
    val repository: FinanceRepository by lazy { FinanceRepository(database) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        PaymentReminderWorker.scheduleDaily(this)
    }
}
