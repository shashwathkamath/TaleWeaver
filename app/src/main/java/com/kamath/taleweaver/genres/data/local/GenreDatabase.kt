package com.kamath.taleweaver.genres.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database for local genre caching
 */
@Database(
    entities = [GenreEntity::class],
    version = 1,
    exportSchema = false
)
abstract class GenreDatabase : RoomDatabase() {
    abstract fun genreDao(): GenreDao

    companion object {
        const val DATABASE_NAME = "genre_database"
    }
}
