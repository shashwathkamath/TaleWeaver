package com.kamath.taleweaver.genres.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.genres.data.local.GenreDao
import com.kamath.taleweaver.genres.data.local.GenreEntity
import com.kamath.taleweaver.genres.domain.model.Genre
import com.kamath.taleweaver.genres.domain.repository.GenreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class GenreRepositoryImpl @Inject constructor(
    private val genreDao: GenreDao,
    private val firestore: FirebaseFirestore
) : GenreRepository {

    companion object {
        private const val GENRES_COLLECTION = "genres"
        private const val REFRESH_INTERVAL_MS = 15 * 24 * 60 * 60 * 1000L // 15 days in milliseconds

        // Initial genre data based on existing BookGenre enum
        private val INITIAL_GENRES = listOf(
            Genre(
                id = "fantasy",
                displayName = "Fantasy",
                variations = listOf("Fantasy", "Epic Fantasy", "Urban Fantasy", "Dark Fantasy"),
                color = "#8B5CF6",
                order = 1
            ),
            Genre(
                id = "science_fiction",
                displayName = "Science Fiction",
                variations = listOf("Science Fiction", "Sci-Fi", "SF", "Speculative Fiction"),
                color = "#3B82F6",
                order = 2
            ),
            Genre(
                id = "mystery",
                displayName = "Mystery",
                variations = listOf("Mystery", "Detective", "Crime Fiction", "Whodunit"),
                color = "#6366F1",
                order = 3
            ),
            Genre(
                id = "thriller",
                displayName = "Thriller",
                variations = listOf("Thriller", "Suspense", "Psychological Thriller"),
                color = "#EF4444",
                order = 4
            ),
            Genre(
                id = "romance",
                displayName = "Romance",
                variations = listOf("Romance", "Romantic Fiction", "Love Story"),
                color = "#EC4899",
                order = 5
            ),
            Genre(
                id = "biography",
                displayName = "Biography",
                variations = listOf("Biography", "Autobiography", "Memoir", "Life Story"),
                color = "#F59E0B",
                order = 6
            ),
            Genre(
                id = "history",
                displayName = "History",
                variations = listOf("History", "Historical", "Non-fiction History"),
                color = "#10B981",
                order = 7
            ),
            Genre(
                id = "self_help",
                displayName = "Self-Help",
                variations = listOf("Self-Help", "Self Help", "Personal Development", "Self Improvement"),
                color = "#14B8A6",
                order = 8
            ),
            Genre(
                id = "cookbooks",
                displayName = "Cookbooks",
                variations = listOf("Cookbooks", "Cooking", "Recipe", "Culinary"),
                color = "#F97316",
                order = 9
            ),
            Genre(
                id = "other",
                displayName = "Other",
                variations = listOf("Other", "Miscellaneous", "General"),
                color = "#6B7280",
                order = 10
            )
        )
    }

    override fun getGenres(): Flow<List<Genre>> {
        return genreDao.getAllGenres().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncGenresFromFirestore(): ApiResult<Unit> {
        return try {
            Timber.d("Syncing genres from Firestore...")

            val snapshot = firestore.collection(GENRES_COLLECTION)
                .get()
                .await()

            val genres = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    Genre.fromFirestore(doc.id, data)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to parse genre document: ${doc.id}")
                    null
                }
            }

            if (genres.isEmpty()) {
                Timber.w("No genres found in Firestore")
                return ApiResult.Error("No genres found in Firestore")
            }

            // Save to local database
            val entities = genres.map { GenreEntity.fromDomain(it) }
            genreDao.insertGenres(entities)

            Timber.d("Successfully synced ${genres.size} genres")
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync genres from Firestore")
            ApiResult.Error(e.message ?: "Failed to sync genres")
        }
    }

    override suspend fun getGenreById(id: String): Genre? {
        return genreDao.getGenreById(id)?.toDomain()
    }

    override suspend fun needsRefresh(): Boolean {
        val lastUpdated = genreDao.getLastUpdatedTimestamp() ?: return true
        val now = System.currentTimeMillis()
        val needsRefresh = (now - lastUpdated) > REFRESH_INTERVAL_MS

        Timber.d("Genre cache age: ${(now - lastUpdated) / (24 * 60 * 60 * 1000)} days. Needs refresh: $needsRefresh")
        return needsRefresh
    }

    override suspend fun populateInitialGenres(): ApiResult<Unit> {
        return try {
            Timber.d("Populating initial genres to Firestore...")

            val batch = firestore.batch()
            INITIAL_GENRES.forEach { genre ->
                val docRef = firestore.collection(GENRES_COLLECTION).document(genre.id)
                batch.set(docRef, Genre.toFirestore(genre))
            }

            batch.commit().await()

            Timber.d("Successfully populated ${INITIAL_GENRES.size} genres to Firestore")

            // Also sync to local database
            syncGenresFromFirestore()
        } catch (e: Exception) {
            Timber.e(e, "Failed to populate initial genres")
            ApiResult.Error(e.message ?: "Failed to populate genres")
        }
    }
}
