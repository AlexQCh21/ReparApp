package com.cursokotlin.reparapp.domain.repository

import com.cursokotlin.reparapp.domain.model.Result
import com.cursokotlin.reparapp.model.Usuario

interface UserRepository {
    suspend fun saveUser(user: Usuario): Result<Unit>
    suspend fun getUser(userId: String): Result<Usuario>
    suspend fun getUserRole(userId: String): Result<String>
}