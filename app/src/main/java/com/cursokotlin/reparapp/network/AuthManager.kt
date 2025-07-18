package com.cursokotlin.reparapp.network

import android.util.Log
import com.cursokotlin.reparapp.domain.model.AuthError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.cursokotlin.reparapp.domain.model.Result


@Singleton
class AuthManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    companion object {
        const val TAG = "AuthManager"
    }

    // Obtener instancia de FirebaseAuth (si es necesario para algún caso específico)
    fun getFirebaseAuthInstance(): FirebaseAuth = firebaseAuth

    // Verifica si hay un usuario logueado (Flow para observación reactiva)
    fun observeCurrentUser(): Flow<FirebaseUser?> = flow {
        while (true) {
            emit(firebaseAuth.currentUser)
            kotlinx.coroutines.delay(1000) // Emite cada segundo (ajustable)
        }
    }

    // Devuelve el usuario actual (sincrónico)
    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    // Registra un usuario con email y password (usando suspend)
    suspend fun signUpUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw AuthError.UnknownError(Exception("Usuario nulo").message)
            Result.Success(user)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            val error = when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> AuthError.InvalidEmail
                "ERROR_WEAK_PASSWORD" -> AuthError.WeakPassword
                else -> AuthError.UnknownError(e.message)
            }
            Result.Failure(error)
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.Failure(AuthError.UserCollision)
        } catch (e: Exception) {
            Result.Failure(AuthError.UnknownError(e.message))
        }
    }

    // Inicia sesión (suspend + Result)
    suspend fun signInUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw AuthError.UnknownError(Exception("Usuario nulo").message)
            Result.Success(user)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            val error = when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> AuthError.InvalidEmail
                "ERROR_WRONG_PASSWORD" -> AuthError.WeakPassword
                else -> AuthError.UnknownError(e.message)
            }
            Result.Failure(error)
        } catch (e: Exception) {
            Result.Failure(AuthError.UnknownError(e.message))
        }
    }

    // Restablecer contraseña (suspend)
    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(AuthError.DatabaseError(e))
        }
    }

    fun userValidated(user: FirebaseUser?=firebaseAuth.currentUser):Boolean{
        return user?.isEmailVerified?:false
    }

    // Verificación del email (suspend)
    suspend fun sendEmailVerification(user: FirebaseUser? = firebaseAuth.currentUser): Result<Unit> {
        return try {
            val targetUser = user ?: throw AuthError.UserNotLoggedIn
            targetUser.sendEmailVerification().await()
            Log.d(TAG, "Email de verificación enviado a ${targetUser.email}")
            Result.Success(Unit)
        } catch (e: AuthError) {
            Log.e(TAG, "Error de autenticación al enviar verificación", e)
            Result.Failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar email de verificación", e)
            Result.Failure(AuthError.DatabaseError(e))
        }
    }

    // Iniciar como anónimo (suspend)
    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.signInAnonymously().await()
            val user = authResult.user ?: throw Exception("Usuario anónimo nulo")
            Result.Success(user)
        } catch (e: Exception) {
            Result.Failure(AuthError.DatabaseError(e))
        }
    }

    // Cerrar sesión (sincrónico)
    fun logout() {
        firebaseAuth.signOut()
    }

    // Eliminar usuario actual (suspend)
    suspend fun deleteCurrentUser(): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: throw Exception("No hay usuario para borrar")
            user.delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(AuthError.DatabaseError(e))
        }
    }
}