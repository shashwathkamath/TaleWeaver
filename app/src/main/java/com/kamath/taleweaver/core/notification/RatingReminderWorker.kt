package com.kamath.taleweaver.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kamath.taleweaver.R
import com.kamath.taleweaver.MainActivity

class RatingReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val orderId = inputData.getString(KEY_ORDER_ID) ?: return Result.failure()
        val sellerName = inputData.getString(KEY_SELLER_NAME) ?: "seller"

        showRatingNotification(orderId, sellerName)

        return Result.success()
    }

    private fun showRatingNotification(orderId: String, sellerName: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Rating Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications to remind you to rate sellers"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to open rating screen when notification is clicked
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_NAVIGATE_TO_RATING, true)
            putExtra(EXTRA_ORDER_ID, orderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            orderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("How was your purchase?")
            .setContentText("Rate your experience with $sellerName")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(orderId.hashCode(), notification)
    }

    companion object {
        const val CHANNEL_ID = "rating_reminder_channel"
        const val KEY_ORDER_ID = "order_id"
        const val KEY_SELLER_NAME = "seller_name"
        const val EXTRA_NAVIGATE_TO_RATING = "navigate_to_rating"
        const val EXTRA_ORDER_ID = "order_id"
    }
}
