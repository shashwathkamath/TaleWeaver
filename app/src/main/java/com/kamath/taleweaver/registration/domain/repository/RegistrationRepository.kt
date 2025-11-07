package com.kamath.taleweaver.registration.domain.repository

import com.google.firebase.auth.AuthResult
import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.registration.domain.model.RegistrationData
import kotlinx.coroutines.flow.Flow

interface RegistrationRepository {
    fun registerUser(registrationData: RegistrationData): Flow<Resource<Unit>>
    /**
     * Creates a new user in Firebase Authentication using their email and password.
     *
     * @param registrationData The user's input (email, password) from the registration form.
     * @return A Flow that emits a Resource containing the AuthResult on success, or an error message on failure.
     */
    fun signUpUser(
        registrationData: RegistrationData
    ): Flow<Resource<AuthResult>>

    /**
     * Creates a user profile document in the Firestore 'users' collection.
     *
     * @param userId The unique ID from the successful Firebase Auth registration.
     * @param email The user's email.
     * @param username The user's chosen username.
     * @return A Flow that emits a Resource containing Unit on success, or an error message on failure.
     */
    fun createUserProfile(
        userId:String,
        email:String,
        username:String
    ): Flow<Resource<Unit>>
}