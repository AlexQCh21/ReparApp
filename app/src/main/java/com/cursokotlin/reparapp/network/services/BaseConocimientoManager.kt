package com.cursokotlin.reparapp.network.services

import android.util.Log
import com.cursokotlin.reparapp.domain.model.AuthError
import com.cursokotlin.reparapp.domain.model.Result
import com.cursokotlin.reparapp.model.BaseConocimiento
import com.cursokotlin.reparapp.network.repository.AuthRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BaseConocimientoManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepositoryImpl: AuthRepositoryImpl
) {
    companion object {
        private const val TAG = "BaseConocimientoManager"
        private const val COLLECTION_CONOCIMENTO = "base_conocimiento"
    }


    suspend fun saveBaseConocimineto(baseConocimiento: BaseConocimiento): Result<Unit> {
        return try {
            val docRef = firestore.collection(COLLECTION_CONOCIMENTO).document()
            baseConocimiento.id = docRef.id // ← asignamos el id
            docRef.set(baseConocimiento, SetOptions.merge()).await()

            Log.d(TAG, "Guardado con éxito: $baseConocimiento")
            Result.Success(Unit)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Datos inválidos", e)
            Result.Failure(AuthError.InvalidUserData)
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar", e)
            Result.Failure(AuthError.DatabaseError(e))
        }
    }

    suspend fun getAllBaseConocimiento(): Result<List<BaseConocimiento>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(COLLECTION_CONOCIMENTO).get().await()
            val baseConocimiento = snapshot.documents.mapNotNull { it.toObject(BaseConocimiento::class.java) }
            Result.Success(baseConocimiento)
        } catch (e: Exception) {
            Log.e(BaseConocimientoManager.Companion.TAG, "Error al obtener lista de Base Conocimiento", e)
            Result.Failure(e)
        }
    }
}