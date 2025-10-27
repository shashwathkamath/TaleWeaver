package com.kamath.taleweaver.home.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.kamath.taleweaver.core.util.FirebaseDiagnostics
import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.home.feed.domain.model.Tale
import com.kamath.taleweaver.home.feed.domain.usecase.GetAllFeed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random


data class FeedScreenState(
    val tales: List<Tale> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false,
    val lastVisibleTale: DocumentSnapshot? = null
)


sealed interface FeedEvent {
    object Refresh : FeedEvent
    object SeedDatabase : FeedEvent
}

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getInitialFeed: GetAllFeed,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedScreenState())
    val uiState = _uiState.asStateFlow()

    init {
        loadInitialFeed()
        viewModelScope.launch {
            val collections = FirebaseDiagnostics.listRootCollections()
            if (collections.isNotEmpty()) {
                FirebaseDiagnostics.sampleCollections(collections)
            }
        }
    }

    fun onEvent(event: FeedEvent) {
        when (event) {
            is FeedEvent.Refresh -> loadInitialFeed()
            is FeedEvent.SeedDatabase -> seedDatabase()
            else -> {}
        }
    }

    private fun loadInitialFeed() {
        viewModelScope.launch {
            getInitialFeed().onEach { result ->
                _uiState.update { currentState ->
                    when (result) {
                        is Resource.Loading -> {
                            currentState.copy(isLoading = true, error = null, tales = emptyList())
                        }

                        is Resource.Success -> {
                            val snapshot = result.data!!
                            val newTales =
                                snapshot.toObjects(Tale::class.java).mapIndexed { index, tale ->
                                    tale.copy(id = snapshot.documents[index].id)
                                }
                            currentState.copy(
                                isLoading = false,
                                tales = newTales,
                                lastVisibleTale = snapshot.documents.lastOrNull(),
                                endReached = snapshot.isEmpty
                            )
                        }

                        is Resource.Error -> {
                            currentState.copy(
                                isLoading = false,
                                error = result.message ?: "An unknown error occurred"
                            )
                        }
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun seedDatabase() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val firestore = Firebase.firestore
            val talesCollection = firestore.collection("tales")
            val batch = firestore.batch()

            try {
                (1..50).forEach { i ->
                    val docRef = talesCollection.document()
                    val taleData = hashMapOf(
                        "authorDisplayName" to "Shashwath Kamath",
                        "authorId" to "FFIOxDZJ9JYgnzAnrIa96Nwin8g1",
                        "authorImageUrl" to "https://www.gravatar.com/avatar/?d=mp", // Default avatar image URL
                        "authorUsername" to "creative_writer",
                        "title" to "The Tale of Chapter #$i",
                        "content" to "The sky bled orange and purple as the last sun of Old Earth dipped below the horizon. This is the story content for tale number $i.",
                        "excerpt" to "The sky bled orange and purple...",
                        // Use FieldValue.serverTimestamp() for the @ServerTimestamp annotation
                        "createdAt" to FieldValue.serverTimestamp(),
                        "isLocked" to (i % 5 == 0), // Lock every 5th tale for variety
                        "isRootTale" to true,
                        "parentTaleId" to null, // Use null for no parent
                        // Add some random numbers for variety
                        "likesCount" to Random.nextInt(5, 250),
                        "readCount" to Random.nextInt(100, 2000),
                        "restacksCount" to Random.nextInt(0, 40),
                        "shareCount" to Random.nextInt(0, 15),
                        "subTalesCount" to Random.nextInt(0, 10)
                    )
                    batch.set(docRef, taleData)
                }
                batch.commit().addOnSuccessListener {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Database seeded successfully!"
                        )
                    }
                    // Optionally, refresh the feed to show the new data
                    loadInitialFeed()
                }.addOnFailureListener { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to seed database: ${e.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "An error occurred: ${e.message}"
                    )
                }
            }
        }
    }
}

