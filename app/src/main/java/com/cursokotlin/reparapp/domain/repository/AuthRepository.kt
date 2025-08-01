package com.cursokotlin.reparapp.domain.repository

import com.google.firebase.auth.FirebaseUser
import com.cursokotlin.reparapp.domain.model.Result
import com.facebook.AccessToken
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signUp(email: String, password: String): Result<FirebaseUser>
    suspend fun signIn(email: String, password: String): Result<FirebaseUser>
    suspend fun signInWithGoogle(token: String): Result<FirebaseUser>
    suspend fun sendEmailVerification(user: FirebaseUser?=null): Result<Unit>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    fun userValidate(user:FirebaseUser): Boolean
    fun getCurrentUser(): FirebaseUser?
    fun observeCurrentUser(): Flow<FirebaseUser?>



    fun logOut()

    //Facebook
    suspend fun signInWithFacebook(token: AccessToken): Result<FirebaseUser>

}