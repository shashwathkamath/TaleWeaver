package com.kamath.taleweaver

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.kamath.taleweaver.genres.data.worker.GenreSyncManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class MyApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var genreSyncManager: GenreSyncManager

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        // Schedule periodic genre sync
        genreSyncManager.schedulePeriodicSync()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}