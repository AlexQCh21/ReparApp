package com.cursokotlin.reparapp.network.repository

import com.cursokotlin.reparapp.model.Result
import com.cursokotlin.reparapp.model.Usuario
import com.facebook.AccessToken
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signUp(email: String, password: String): Result<FirebaseUser>
    suspend fun signIn(email: String, password: String): Result<FirebaseUser>
    suspend fun signInWithGoogle(token: String): Result<FirebaseUser>
    suspend fun sendEmailVerification(user: FirebaseUser?=null): Result<Unit>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    fun userValidate(user: FirebaseUser): Boolean
    fun getCurrentUser(): FirebaseUser?
    fun observeCurrentUser(): Flow<FirebaseUser?>
    suspend fun getUserData(): Result<Usuario>



    fun logOut()

    //Facebook
    suspend fun signInWithFacebook(token: AccessToken): Result<FirebaseUser>

}