package com.cursokotlin.reparapp.network

import android.util.Log
import com.cursokotlin.reparapp.domain.model.AuthError
import com.cursokotlin.reparapp.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.cursokotlin.reparapp.domain.model.Result

@Singleton
class FirestoreManager @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "FirestoreManager"
        const val COLLECTION_USERS = "usuarios"
    }

    suspend fun saveUser(user: Usuario): Result<Unit> {
        return try {
            require(user.id.isNotEmpty()) { throw AuthError.InvalidUserData }

            firestore.collection(COLLECTION_USERS)
                .document(user.id)
                .set(user, SetOptions.merge())
                .await()

            Log.d(TAG, "Usuario guardado con éxito: ${user.id}")
            Result.Success(Unit)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Datos de usuario inválidos", e)
            Result.Failure(AuthError.InvalidUserData)
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar usuario", e)
            Result.Failure(AuthError.DatabaseError(e))
        }
    }

    suspend fun getUser(userId: String): Result<Usuario> {
        return try {
            require(userId.isNotEmpty()) { throw AuthError.InvalidUserData }

            val snapshot = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .await()

            if (snapshot.exists()) {
                snapshot.toObject(Usuario::class.java)?.let { user ->
                    Result.Success(user)
                } ?: Result.Failure(AuthError.InvalidUserData)
            } else {
                Result.Failure(AuthError.UserNotFound)
            }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "ID de usuario inválido", e)
            Result.Failure(AuthError.InvalidUserData)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener usuario", e)
            Result.Failure(AuthError.DatabaseError(e))
        }
    }

    suspend fun getUserRole(userId: String): Result<String> {
        return try {
            require(userId.isNotEmpty()) { throw AuthError.InvalidUserData }

            val snapshot = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .await()

            if (snapshot.exists()) {
                val role = snapshot.getString("rol") ?: "cliente"
                Result.Success(role)
            } else {
                Result.Failure(AuthError.UserNotFound)
            }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "ID de usuario inválido", e)
            Result.Failure(AuthError.InvalidUserData)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener rol del usuario", e)
            Result.Failure(AuthError.DatabaseError(e))
        }
    }

    suspend fun getAllTecnicos(): Result<List<Usuario>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_USERS)
                .whereEqualTo("rol", "tecnico")
                .get()
                .await()

            val tecnicos = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Usuario::class.java)?.apply {
                    id = doc.id // Asegúrate de asignar el ID del documento
                }
            }

            Result.Success(tecnicos)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener técnicos", e)
            Result.Failure(AuthError.DatabaseError(e))
        }
    }

    suspend fun userExistsByEmail(email: String): Boolean {
        return try {
            val snapshot = firestore.collection(COLLECTION_USERS)
                .whereEqualTo("email", email)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            Log.e(TAG, "Error al buscar usuario por email", e)
            false // Por seguridad, asumimos que no existe si hay error
        }
    }


}