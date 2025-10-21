package com.kamath.taleweaver.registration.data.repository

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.registration.domain.model.User
import com.kamath.taleweaver.registration.domain.repository.RegistrationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class RegistrationRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : RegistrationRepository {
    override fun signUpUser(
        user: User
    ): Flow<Resource<AuthResult>> = callbackFlow {
        trySend(Resource.Loading())
        firebaseAuth
            .createUserWithEmailAndPassword(user.email, user.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        val userData = hashMapOf(
                            "username" to user.username,
                            "email" to user.email,
                            "password" to user.password,
                            "createdAt" to System.currentTimeMillis().toString()
                        )
                        firestore.collection("users")
                            .document(firebaseUser.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                trySend(Resource.Success(task.result))
                            }
                            .addOnFailureListener { exception ->
                                trySend(Resource.Error("Failed to save the data:$exception"))
                            }
                    } else {
                        trySend(Resource.Error("Authentication succeeded but user is null"))
                    }
                } else {
                    trySend(
                        Resource
                            .Error(task.exception?.message ?: "An unknown error occurred")
                    )
                }
            }
        awaitClose {  }
    }
}