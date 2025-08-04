package com.cursokotlin.reparapp.network.repository

import com.cursokotlin.reparapp.model.Usuario
import com.cursokotlin.reparapp.network.FirestoreManager
import com.cursokotlin.reparapp.model.Result
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestoreManager: FirestoreManager
) : UserRepository {
    override suspend fun saveUser(user: Usuario): Result<Unit> {
        return firestoreManager.saveUser(user)
    }

    override suspend fun getUser(userId: String): Result<Usuario> {
        return firestoreManager.getUser(userId)
    }

    override suspend fun getUserRole(userId: String): Result<String> {
        return firestoreManager.getUserRole(userId)
    }
}