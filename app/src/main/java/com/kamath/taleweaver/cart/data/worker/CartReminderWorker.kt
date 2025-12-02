package com.kamath.taleweaver.cart.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kamath.taleweaver.cart.domain.repository.CartRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * Worker that periodically checks the cart and sends reminder notifications
 * if the user has items in their cart that haven't been ordered.
 */
@HiltWorker
class CartReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val cartRepository: CartRepository,
    private val cartNotificationHelper: CartNotificationHelper
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Timber.d("CartReminderWorker: Starting cart check")

            // Get current cart items
            val cartItems = cartRepository.getCartItems().first()
            val itemCount = cartItems.size

            Timber.d("CartReminderWorker: Found $itemCount items in cart")

            // Send notification if cart is not empty
            if (itemCount > 0) {
                cartNotificationHelper.sendCartReminderNotification(
                    itemCount = itemCount,
                    firstItemTitle = cartItems.firstOrNull()?.listing?.title
                )
                Timber.d("CartReminderWorker: Sent reminder notification for $itemCount items")
            } else {
                Timber.d("CartReminderWorker: Cart is empty, no notification sent")
            }

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "CartReminderWorker: Error checking cart")
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "cart_reminder_worker"
    }
}
