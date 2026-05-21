package com.taleweaver.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.taleweaver.app.cart.data.worker.CartReminderScheduler
import com.taleweaver.app.genres.data.worker.GenreSyncManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class MyApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var genreSyncManager: GenreSyncManager

    @Inject
    lateinit var cartReminderScheduler: CartReminderScheduler

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        // Schedule periodic genre sync
        genreSyncManager.schedulePeriodicSync()

        // Schedule periodic cart reminder
        cartReminderScheduler.scheduleCartReminder()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}