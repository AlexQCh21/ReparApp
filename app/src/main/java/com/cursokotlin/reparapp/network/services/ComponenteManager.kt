package com.cursokotlin.reparapp.network.services

import android.util.Log
import com.cursokotlin.reparapp.domain.model.AuthError
import com.cursokotlin.reparapp.model.Componente
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import com.cursokotlin.reparapp.domain.model.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComponenteManager @Inject constructor(
    private val firestore: FirebaseFirestore,
) {

    companion object {
        private const val TAG = "ComponenteManager"
        private const val COLLECTION_COMPONENTES = "componentes"
    }

    suspend fun saveComponente(componente: Componente): Result<Unit> {
        return try {
            val docRef = firestore.collection(COLLECTION_COMPONENTES).document()
            componente.id = docRef.id
            docRef.set(componente, SetOptions.merge()).await()

            Log.d(TAG, "Componente guardado con éxito: $componente")
            Result.Success(Unit)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Datos del componente inválidos", e)
            Result.Failure(AuthError.InvalidUserData)
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar componente", e)
            Result.Failure(AuthError.DatabaseError(e))
        }
    }

    suspend fun getAll(): Result<List<Componente>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_COMPONENTES)
                .get()
                .await()

            val lista = snapshot.documents.map { doc ->
                val componente = doc.toObject(Componente::class.java)
                componente?.id = doc.id
                componente!!
            }
            Result.Success(lista)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener componentes", e)
            Result.Failure(AuthError.DatabaseError(e))
        }
    }

    suspend fun getComponenteById(componenteId: String): Result<Componente> {
        return try {
            val docSnapshot = firestore.collection(COLLECTION_COMPONENTES)
                .document(componenteId)
                .get()
                .await()

            val componente = docSnapshot.toObject(Componente::class.java)
            if (componente != null) {
                componente.id = docSnapshot.id
                Result.Success(componente)
            } else {
                Result.Failure(AuthError.DatabaseError(Exception("No se encontró el componente")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener componente por id", e)
            Result.Failure(AuthError.DatabaseError(e))
        }
    }
}