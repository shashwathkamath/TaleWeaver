package com.kamath.taleweaver.genres.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.genres.domain.usecase.SyncGenresUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * Background worker for syncing genres from Firestore every 15 days
 */
@HiltWorker
class GenreSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncGenresUseCase: SyncGenresUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Timber.d("Starting periodic genre sync...")

            when (val result = syncGenresUseCase()) {
                is ApiResult.Success -> {
                    Timber.d("Genre sync completed successfully")
                    Result.success()
                }
                is ApiResult.Error -> {
                    Timber.e("Genre sync failed: ${result.message}")
                    Result.retry()
                }
                is ApiResult.Loading -> {
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Genre sync worker failed")
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "genre_sync_work"
    }
}
