package com.kamath.taleweaver.home.account.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.kamath.taleweaver.core.domain.UserProfile
import com.kamath.taleweaver.core.util.Constants.LISTINGS_COLLECTION
import com.kamath.taleweaver.core.util.Constants.USERS_COLLECTION
import com.kamath.taleweaver.home.feed.domain.model.Listing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed interface UserProfileState {
    object Loading : UserProfileState
    data class Success(
        val userProfile: UserProfile?,
        val listings: List<Listing> = emptyList(),
        val isLoadingListings: Boolean = false,
        val selectedTab: AccountTab = AccountTab.PROFILE_INFO
    ) : UserProfileState
    data class Error(val message: String) : UserProfileState
}

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<UserProfileState>(UserProfileState.Loading)
    val uiState = _uiState.asStateFlow()

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _uiState.value = UserProfileState.Loading
            try {
                // Fetch user profile
                val userDoc = firestore.collection(USERS_COLLECTION)
                    .document(userId)
                    .get()
                    .await()

                val userProfile = userDoc.toObject(UserProfile::class.java)

                if (userProfile != null) {
                    _uiState.value = UserProfileState.Success(
                        userProfile = userProfile,
                        isLoadingListings = true
                    )

                    // Fetch user's listings
                    loadUserListings(userId)
                } else {
                    _uiState.value = UserProfileState.Error("User not found")
                }
            } catch (e: Exception) {
                _uiState.value = UserProfileState.Error(e.message ?: "Failed to load user profile")
            }
        }
    }

    private suspend fun loadUserListings(userId: String) {
        try {
            val listingsSnapshot = firestore.collection(LISTINGS_COLLECTION)
                .whereEqualTo("sellerId", userId)
                .get()
                .await()

            val listings = listingsSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Listing::class.java)
            }

            val currentState = _uiState.value
            if (currentState is UserProfileState.Success) {
                _uiState.value = currentState.copy(
                    listings = listings,
                    isLoadingListings = false
                )
            }
        } catch (e: Exception) {
            val currentState = _uiState.value
            if (currentState is UserProfileState.Success) {
                _uiState.value = currentState.copy(isLoadingListings = false)
            }
        }
    }

    fun onTabSelected(tab: AccountTab) {
        val currentState = _uiState.value
        if (currentState is UserProfileState.Success) {
            _uiState.value = currentState.copy(selectedTab = tab)
        }
    }
}
