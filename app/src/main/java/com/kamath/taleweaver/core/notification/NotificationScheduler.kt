package com.kamath.taleweaver.core.notification

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    fun scheduleRatingReminder(
        context: Context,
        orderId: String,
        sellerName: String,
        daysUntilDelivery: Long
    ) {
        val inputData = Data.Builder()
            .putString(RatingReminderWorker.KEY_ORDER_ID, orderId)
            .putString(RatingReminderWorker.KEY_SELLER_NAME, sellerName)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<RatingReminderWorker>()
            .setInitialDelay(daysUntilDelivery, TimeUnit.DAYS)
            .setInputData(inputData)
            .addTag("rating_reminder_$orderId")
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    fun cancelRatingReminder(context: Context, orderId: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag("rating_reminder_$orderId")
    }
}
