package com.kamath.taleweaver.cart.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scheduler for cart reminder notifications
 */
@Singleton
class CartReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        // Check cart every 24 hours (minimum periodic work interval is 15 minutes)
        private const val REPEAT_INTERVAL_HOURS = 24L

        // You can adjust these values:
        // For testing, use 15 minutes: REPEAT_INTERVAL_MINUTES = 15L
        // For production, use 24 hours: REPEAT_INTERVAL_HOURS = 24L
    }

    /**
     * Schedules the periodic cart reminder worker
     * This should be called when the app starts
     */
    fun scheduleCartReminder() {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // No network needed
                .setRequiresBatteryNotLow(true) // Only run when battery is not low
                .build()

            val workRequest = PeriodicWorkRequestBuilder<CartReminderWorker>(
                REPEAT_INTERVAL_HOURS,
                TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .addTag(CartReminderWorker.WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                CartReminderWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // Keep existing work if already scheduled
                workRequest
            )

            Timber.d("CartReminderScheduler: Cart reminder scheduled to run every $REPEAT_INTERVAL_HOURS hours")
        } catch (e: Exception) {
            Timber.e(e, "CartReminderScheduler: Error scheduling cart reminder")
        }
    }

    /**
     * Cancels the cart reminder worker
     */
    fun cancelCartReminder() {
        try {
            WorkManager.getInstance(context)
                .cancelUniqueWork(CartReminderWorker.WORK_NAME)
            Timber.d("CartReminderScheduler: Cart reminder cancelled")
        } catch (e: Exception) {
            Timber.e(e, "CartReminderScheduler: Error cancelling cart reminder")
        }
    }
}
