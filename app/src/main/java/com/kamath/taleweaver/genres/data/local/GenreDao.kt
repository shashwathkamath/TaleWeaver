package com.kamath.taleweaver.genres.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Genre table
 */
@Dao
interface GenreDao {

    /**
     * Get all genres ordered by display order
     */
    @Query("SELECT * FROM genres ORDER BY `order` ASC")
    fun getAllGenres(): Flow<List<GenreEntity>>

    /**
     * Get a specific genre by ID
     */
    @Query("SELECT * FROM genres WHERE id = :id")
    suspend fun getGenreById(id: String): GenreEntity?

    /**
     * Insert or update genres (replaces on conflict)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenres(genres: List<GenreEntity>)

    /**
     * Insert or update a single genre
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenre(genre: GenreEntity)

    /**
     * Delete all genres (used when refreshing from Firestore)
     */
    @Query("DELETE FROM genres")
    suspend fun deleteAllGenres(): Unit

    /**
     * Get the last updated timestamp
     */
    @Query("SELECT MAX(updatedAt) FROM genres")
    suspend fun getLastUpdatedTimestamp(): Long?
}
