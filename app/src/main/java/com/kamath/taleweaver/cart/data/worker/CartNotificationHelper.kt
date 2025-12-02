package com.kamath.taleweaver.cart.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kamath.taleweaver.R
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for creating and sending cart reminder notifications
 */
@Singleton
class CartNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val CHANNEL_ID = "cart_reminders"
        private const val CHANNEL_NAME = "Cart Reminders"
        private const val CHANNEL_DESCRIPTION = "Notifications to remind you about items in your cart"
        private const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    /**
     * Creates the notification channel for cart reminders (Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Timber.d("CartNotificationHelper: Notification channel created")
        }
    }

    /**
     * Sends a notification reminding the user about items in their cart
     *
     * @param itemCount Number of items in the cart
     * @param firstItemTitle Title of the first item (optional, for personalization)
     */
    fun sendCartReminderNotification(itemCount: Int, firstItemTitle: String?) {
        try {
            // Build notification content
            val title = "You have items waiting in your cart"
            val text = when {
                itemCount == 1 && firstItemTitle != null -> "\"$firstItemTitle\" is waiting for you!"
                itemCount == 1 -> "1 item is waiting in your cart"
                else -> "$itemCount items are waiting in your cart"
            }

            // Create intent to open the app (you can customize this to open cart directly)
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                // You can add extras here to navigate directly to cart
                // putExtra("navigate_to", "cart")
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Build notification
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your cart icon
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            // Send notification
            val notificationManager = NotificationManagerCompat.from(context)

            // Check if notification permission is granted (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (androidx.core.app.ActivityCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    notificationManager.notify(NOTIFICATION_ID, notification)
                    Timber.d("CartNotificationHelper: Notification sent for $itemCount items")
                } else {
                    Timber.w("CartNotificationHelper: Notification permission not granted")
                }
            } else {
                notificationManager.notify(NOTIFICATION_ID, notification)
                Timber.d("CartNotificationHelper: Notification sent for $itemCount items")
            }

        } catch (e: Exception) {
            Timber.e(e, "CartNotificationHelper: Error sending notification")
        }
    }

    /**
     * Cancels any existing cart reminder notification
     */
    fun cancelCartReminderNotification() {
        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(NOTIFICATION_ID)
            Timber.d("CartNotificationHelper: Notification cancelled")
        } catch (e: Exception) {
            Timber.e(e, "CartNotificationHelper: Error cancelling notification")
        }
    }
}
