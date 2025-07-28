package com.cursokotlin.reparapp.network.services

import android.util.Log
import com.cursokotlin.reparapp.domain.model.AuthError
import com.cursokotlin.reparapp.domain.model.Result
import com.cursokotlin.reparapp.model.Dispositivo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DispositivoManager @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val TAG = "DispositivoManager"
        private const val COLLECTION_DEVICES = "dispositivos"
    }

    suspend fun saveDispositivo(dispositivo: Dispositivo): Result<Unit> {
        return try {

            firestore.collection(COLLECTION_DEVICES)
                .document()
                .set(dispositivo, SetOptions.merge())
                .await()

            Log.d(TAG, "Dispositivo guardado con éxito: $dispositivo")
            Result.Success(Unit)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Datos del dispositivo inválidos", e)
            Result.Failure(AuthError.InvalidUserData)
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar dispositivo", e)
            Result.Failure(AuthError.DatabaseError(e))
        }
    }

    suspend fun getAll(): Result<List<Dispositivo>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_DEVICES)
                .get()
                .await()
            val dispositivos = snapshot.documents.mapNotNull { it.toObject(Dispositivo::class.java) }

            Log.d(TAG, "Dispositivos obtenidos con éxito: ${dispositivos.size}")
            Result.Success(dispositivos)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Datos del dispositivo inválidos", e)
            Result.Failure(AuthError.InvalidUserData)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener dispositivos", e)
            Result.Failure(AuthError.DatabaseError(e))
        }
    }

}
