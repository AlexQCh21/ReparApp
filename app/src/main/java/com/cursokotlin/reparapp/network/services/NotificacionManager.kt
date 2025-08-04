package com.cursokotlin.reparapp.network.services

import com.cursokotlin.reparapp.network.FirestoreManager
import com.google.firebase.firestore.FirebaseFirestore
import com.cursokotlin.reparapp.model.Result
import com.cursokotlin.reparapp.model.Notificacion
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificacionManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userManager: FirestoreManager
) {

    companion object {
        private const val TAG = "NotificacionManager"
        private const val COLLECTION_NOTIFICACIONES = "notificaciones"
    }

    fun enviarNotificacion(notificacion: Notificacion, onResult: (Result<Unit>) -> Unit) {
        firestore.collection(COLLECTION_NOTIFICACIONES)
            .add(notificacion)
            .addOnSuccessListener {
                onResult(Result.Success(Unit))
            }
            .addOnFailureListener { e ->
                onResult(Result.Failure(e))
            }
    }

    suspend fun obtenerNotificacionesPorUsuario(userId: String): Result<List<Notificacion>> {
        return try {
            val snapshot = firestore
                .collection(COLLECTION_NOTIFICACIONES)
                .whereEqualTo("receptorId", userId)
                .get()
                .await()

            val notificaciones = snapshot.documents.mapNotNull { it.toObject(Notificacion::class.java) }
            Result.Success(notificaciones)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    fun marcarNotificacionComoLeida(
        notificacionId: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        firestore.collection(COLLECTION_NOTIFICACIONES)
            .document(notificacionId)
            .update("leido", true)
            .addOnSuccessListener {
                onResult(Result.Success(Unit))
            }
            .addOnFailureListener { e ->
                onResult(Result.Failure(e))
            }
    }

    fun eliminarNotificacion(
        notificacionId: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        firestore.collection(COLLECTION_NOTIFICACIONES)
            .document(notificacionId)
            .delete()
            .addOnSuccessListener {
                onResult(Result.Success(Unit))
            }
            .addOnFailureListener { e ->
                onResult(Result.Failure(e))
            }
    }

}