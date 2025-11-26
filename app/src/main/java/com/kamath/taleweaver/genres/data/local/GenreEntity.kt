package com.kamath.taleweaver.genres.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kamath.taleweaver.genres.domain.model.Genre

/**
 * Room database entity for storing genres locally
 */
@Entity(tableName = "genres")
data class GenreEntity(
    @PrimaryKey
    val id: String,
    val displayName: String,
    val variations: String,  // Stored as comma-separated string
    val color: String,
    val order: Int,
    val updatedAt: Long
) {
    /**
     * Convert Room entity to domain model
     */
    fun toDomain(): Genre {
        return Genre(
            id = id,
            displayName = displayName,
            variations = variations.split(",").filter { it.isNotBlank() },
            color = color,
            order = order,
            updatedAt = updatedAt
        )
    }

    companion object {
        /**
         * Convert domain model to Room entity
         */
        fun fromDomain(genre: Genre): GenreEntity {
            return GenreEntity(
                id = genre.id,
                displayName = genre.displayName,
                variations = genre.variations.joinToString(","),
                color = genre.color,
                order = genre.order,
                updatedAt = genre.updatedAt
            )
        }
    }
}
