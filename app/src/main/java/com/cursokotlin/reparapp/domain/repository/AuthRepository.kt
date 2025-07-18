package com.cursokotlin.reparapp.domain.repository

import com.google.firebase.auth.FirebaseUser
import com.cursokotlin.reparapp.domain.model.Result
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signUp(email: String, password: String): Result<FirebaseUser>
    suspend fun signIn(email: String, password: String): Result<FirebaseUser>
    suspend fun sendEmailVerification(user: FirebaseUser?=null): Result<Unit>
    suspend fun userValidate(user:FirebaseUser): Boolean
    suspend fun getCurrentUser(): FirebaseUser?
    suspend fun observeCurrentUser(): Flow<FirebaseUser?>
}