package com.kamath.taleweaver.genres.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kamath.taleweaver.cart.data.local.CartDao
import com.kamath.taleweaver.cart.data.local.CartEntity

/**
 * Room database for local data caching (genres, cart items, etc.)
 */
@Database(
    entities = [GenreEntity::class, CartEntity::class],
    version = 2,
    exportSchema = false
)
abstract class TaleWeaverDatabase : RoomDatabase() {
    abstract fun genreDao(): GenreDao
    abstract fun cartDao(): CartDao

    companion object {
        const val DATABASE_NAME = "taleweaver_database"
    }
}
