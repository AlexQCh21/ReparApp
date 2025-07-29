package com.cursokotlin.reparapp.network.storage

import android.net.Uri
import android.util.Log
import com.cursokotlin.reparapp.network.AuthManager
import com.cursokotlin.reparapp.network.storage.StorageManager.Companion.FOLDER_POINTS_IMAGES
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TicketStorageManager @Inject constructor(
    private val storage: FirebaseStorage,
    private val authManager: AuthManager
) {
    companion object {
        const val TAG = "TicketStorageManager"
        const val FOLDER_TICKETS_IMAGES = "ticket_images"
    }

    /**
     * Sube la imagen al Storage y devuelve la URL pública
     */
    suspend fun uploadDispositivoImage(uri: Uri, ticketId: String): String? {
        return try {
            val uid = authManager.getCurrentUser()?.uid
            if (uid.isNullOrEmpty()) {
                Log.e(TicketStorageManager.TAG, "Usuario no autenticado, no se puede subir imagen")
                return null
            }

            val imageRef = storage.reference
                .child("$FOLDER_POINTS_IMAGES/$uid/${ticketId}.jpg")

            imageRef.putFile(uri).await()

            val downloadUrl = imageRef.downloadUrl.await().toString()
            Log.d(TicketStorageManager.TAG, "Imagen subida correctamente: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            Log.e(TicketStorageManager.TAG, "Error al subir imagen", e)
            null
        }
    }


}