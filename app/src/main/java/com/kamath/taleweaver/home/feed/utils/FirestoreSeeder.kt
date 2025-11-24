package com.kamath.taleweaver.home.feed.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.kamath.taleweaver.home.feed.domain.model.BookCondition
import com.kamath.taleweaver.home.feed.domain.model.BookGenre
import com.kamath.taleweaver.home.feed.domain.model.Listing
import com.kamath.taleweaver.home.feed.domain.model.ListingStatus
import com.kamath.taleweaver.home.search.util.ListingGeoHelper
import kotlinx.coroutines.tasks.await
import timber.log.Timber

object FirestoreSeeder {

    fun getSeedData(): List<Listing> {
        return listOf(
            // Listing 1: Sci-Fi classic
            Listing(
                sellerId = "user_sf_101",
                sellerUsername = "SciFiSteve",
                title = "Project Hail Mary",
                author = "Andy Weir",
                isbn = "9780593135204",
                genres = listOf(BookGenre.SCIENCE_FICTION),
                description = "Hardcover, read once. Excellent condition with a thrilling story. From a smoke-free home.",
                //coverImageUrls = listOf("https://firebasestorage.googleapis.com/v0/b/your-project-id.appspot.com/o/seed_images%2Fhail_mary.jpg?alt=media"),
                price = 15.00,
                condition = BookCondition.LIKE_NEW,
                //location = GeoPoint(34.0522, -118.2437), // Los Angeles
                shippingOffered = true,
                status = ListingStatus.AVAILABLE
            ),
            // Listing 2: Fantasy epic
            Listing(
                sellerId = "user_fan_202",
                sellerUsername = "FantasyFanatic",
                title = "The Name of the Wind",
                author = "Patrick Rothfuss",
                isbn = "9780756404741",
                genres = listOf(BookGenre.FANTASY),
                description = "Paperback copy with some wear on the spine. A masterpiece of storytelling. All pages are clean.",
                //coverImageUrls = listOf("https://firebasestorage.googleapis.com/v0/b/your-project-id.appspot.com/o/seed_images%2Fname_of_wind.jpg?alt=media"),
                price = 7.50,
                condition = BookCondition.USED,
                //location = GeoPoint(40.7128, -74.0060), // New York City
                shippingOffered = true,
                status = ListingStatus.AVAILABLE
            ),
            // Listing 3: Historical book, sold
            Listing(
                sellerId = "user_hist_303",
                sellerUsername = "HistoryBuff",
                title = "Sapiens: A Brief History of Humankind",
                author = "Yuval Noah Harari",
                isbn = "9780062316097",
                genres = listOf(BookGenre.HISTORY, BookGenre.BIOGRAPHY),
                description = "Well-read copy with some highlighting in the first few chapters. Great for a student.",
                //coverImageUrls = listOf("https://firebasestorage.googleapis.com/v0/b/your-project-id.appspot.com/o/seed_images%2Fsapiens.jpg?alt=media"),
                price = 5.00,
                condition = BookCondition.ACCEPTABLE,
               // location = GeoPoint(51.5074, -0.1278), // London
                shippingOffered = false,
                status = ListingStatus.SOLD
            )
        )
    }

    suspend fun seedDatabase(db: FirebaseFirestore) {
        val listings = getSeedData()
        val listingsCollection = db.collection("listings") // Use your desired collection name

        // Check if the collection is already populated
        val existingDocs = listingsCollection.limit(1).get().await()
        if (!existingDocs.isEmpty) {
            Timber.d("Database already seeded. Skipping.")
            return
        }

        Timber.d("Seeding database with ${listings.size} listings...")
        try {
            listings.forEach { listing ->
                // Use ListingGeoHelper to save with GeoFirestore support
                val result = ListingGeoHelper.saveListing(db, listing)
                result.onSuccess { id ->
                    Timber.d("Saved listing: $id")
                }.onFailure { error ->
                    Timber.e(error, "Failed to save listing: ${listing.title}")
                }
            }
            Timber.d("Database seeding successful!")
        } catch (e: Exception) {
            Timber.e(e, "Error seeding database")
        }
    }
}