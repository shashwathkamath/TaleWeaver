package com.kamath.taleweaver.genres.data.worker

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
 * Manager for scheduling periodic genre sync work
 */
@Singleton
class GenreSyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedule periodic genre sync (every 15 days)
     */
    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<GenreSyncWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            GenreSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing work if already scheduled
            syncRequest
        )

        Timber.d("Scheduled periodic genre sync (every 15 days)")
    }

    /**
     * Cancel periodic genre sync
     */
    fun cancelSync() {
        workManager.cancelUniqueWork(GenreSyncWorker.WORK_NAME)
        Timber.d("Cancelled periodic genre sync")
    }
}
