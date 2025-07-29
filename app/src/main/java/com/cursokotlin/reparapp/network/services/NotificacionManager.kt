package com.cursokotlin.reparapp.network.services

import com.cursokotlin.reparapp.network.FirestoreManager
import com.google.firebase.firestore.FirebaseFirestore
import com.cursokotlin.reparapp.domain.model.Result
import com.cursokotlin.reparapp.model.Notificacion
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

    fun obtenerNotificacionesPorUsuario(
        userId: String,
        tipoUsuario: String,
        onResult: (Result<List<Notificacion>>) -> Unit
    ) {
        firestore.collection(COLLECTION_NOTIFICACIONES)
            .whereEqualTo("receptorId", userId)
            .whereEqualTo("tipo_usuario", tipoUsuario)
            .orderBy("fecha_creacion", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val notificaciones = querySnapshot.documents.mapNotNull { doc ->
                    val notif = doc.toObject(Notificacion::class.java)
                    notif?.apply { id = doc.id }
                }
                onResult(Result.Success(notificaciones))
            }
            .addOnFailureListener { e ->
                onResult(Result.Failure(e))
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