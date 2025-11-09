package com.kamath.taleweaver.home.account.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kamath.taleweaver.core.domain.UserProfile
import com.kamath.taleweaver.core.util.Constants.USERS_COLLECTION
import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.home.account.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStore: FirebaseFirestore
) : AccountRepository {
    override fun getUserProfile(): Flow<Resource<UserProfile>> = flow {
        emit(Resource.Loading())
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            emit(Resource.Error("User not logged in"))
            return@flow
        }
        val documentSnapshot = firebaseStore
            .collection(USERS_COLLECTION)
            .document(currentUserId)
            .get()
            .await()
        val userProfile = documentSnapshot.toObject(UserProfile::class.java)
        if (userProfile != null) {
            emit(Resource.Success(userProfile))
        } else {
            emit(Resource.Error("Couldn't fetch user profile"))
        }
    }.catch { e ->
        emit(
            Resource.Error(
                e.message.toString() ?: "An unknown error occurred while fetching the user profile"
            )
        )
    }

    override fun logoutUser(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        firebaseAuth.signOut()
        emit(Resource.Success(Unit))
    }.catch { e ->
        emit(Resource.Error(e.message.toString() ?: "An Error occurred while logging out"))
    }
}