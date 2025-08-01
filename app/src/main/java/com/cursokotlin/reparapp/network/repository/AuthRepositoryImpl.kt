package com.cursokotlin.reparapp.network.repository

import com.cursokotlin.reparapp.domain.model.AuthError
import com.cursokotlin.reparapp.domain.repository.AuthRepository
import com.cursokotlin.reparapp.network.AuthManager
import com.cursokotlin.reparapp.domain.model.Result
import com.facebook.AccessToken
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authManager: AuthManager
) : AuthRepository {
    override suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        return authManager.signUpUser(email, password)
    }

    override suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return authManager.signInUser(email, password)
    }

    override suspend fun signInWithGoogle(token: String): Result<FirebaseUser> {
        return authManager.signInWithGoogle(token)
    }

    override suspend fun sendEmailVerification(user: FirebaseUser?): Result<Unit> {
        return authManager.sendEmailVerification(user)
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return authManager.sendPasswordReset(email)
    }

    override fun userValidate(user: FirebaseUser): Boolean {
        return authManager.userValidated()
    }

    override fun getCurrentUser(): FirebaseUser? {
        return authManager.getCurrentUser()
    }

    override fun observeCurrentUser(): Flow<FirebaseUser?> {
        return authManager.observeCurrentUser()
    }

    override fun logOut() {
        authManager.logout()
    }

    //Facebook
    override suspend fun signInWithFacebook(token: AccessToken): Result<FirebaseUser> {
        return authManager.signInWithFacebook(token)
    }

}