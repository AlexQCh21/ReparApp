package com.cursokotlin.reparapp.network.storage

import android.net.Uri
import android.util.Log
import com.cursokotlin.reparapp.network.AuthManager
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageManager @Inject constructor(
    private val storage: FirebaseStorage,
    private val authManager: AuthManager
) {
    companion object {
        const val TAG = "StorageManager"
        const val FOLDER_POINTS_IMAGES = "device_images"
    }

    /**
     * Sube la imagen al Storage y devuelve la URL pública
     */
    suspend fun uploadDispositivoImage(uri: Uri, dispositivoId: String): String? {
        return try {
            val uid = authManager.getCurrentUser()?.uid
            if (uid.isNullOrEmpty()) {
                Log.e(TAG, "Usuario no autenticado, no se puede subir imagen")
                return null
            }

            val imageRef = storage.reference
                .child("$FOLDER_POINTS_IMAGES/$uid/${dispositivoId}.jpg")

            imageRef.putFile(uri).await()

            val downloadUrl = imageRef.downloadUrl.await().toString()
            Log.d(TAG, "Imagen subida correctamente: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "Error al subir imagen", e)
            null
        }
    }

    /**
     * Obtiene la URL de descarga de la imagen del dispositivo
     */
    suspend fun getDispositivoImage(dispositivoId: String): String? {
        return try {
            val uid = authManager.getCurrentUser()?.uid
            if (uid.isNullOrEmpty()) {
                Log.e(TAG, "Usuario no autenticado, no se puede obtener imagen")
                return null
            }

            val imageRef = storage.reference
                .child("$FOLDER_POINTS_IMAGES/$uid/${dispositivoId}.jpg")

            val downloadUrl = imageRef.downloadUrl.await().toString()
            Log.d(TAG, "URL de imagen obtenida: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener la imagen", e)
            null
        }
    }

    /**
     * Borra la imagen del dispositivo del Storage
     */
    suspend fun deleteDispositivoImage(dispositivoId: String): Boolean {
        return try {
            val uid = authManager.getCurrentUser()?.uid
            if (uid.isNullOrEmpty()) {
                Log.e(TAG, "Usuario no autenticado, no se puede borrar imagen")
                return false
            }

            val imageRef = storage.reference
                .child("$FOLDER_POINTS_IMAGES/$uid/${dispositivoId}.jpg")

            imageRef.delete().await()
            Log.d(TAG, "Imagen eliminada correctamente")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar la imagen", e)
            false
        }
    }
}
