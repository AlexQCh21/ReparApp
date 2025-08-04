package com.cursokotlin.reparapp.network.services

import android.net.Uri
import android.util.Log
import com.cursokotlin.reparapp.model.Local
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import com.cursokotlin.reparapp.model.Result

@Singleton
class LocalManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    companion object {
        private const val TAG = "LocalManager"
        private const val COLLECTION_LOCAL = "locales"
        private const val STORAGE_PATH = "locales_images"
    }

    suspend fun createLocal(local: Local, uris: List<Uri>): Result<Void?> = withContext(Dispatchers.IO) {
        try {
            val docRef = firestore.collection(COLLECTION_LOCAL).document()
            local.id = docRef.id

            val uploadedUrls = uploadImages(docRef.id, uris)
            if (uploadedUrls.size >= 3) {
                local.foto_p = uploadedUrls[0]
                local.foto_r_1 = uploadedUrls[1]
                local.foto_r_2 = uploadedUrls[2]
            }

            docRef.set(local).await()
            Result.Success(null)
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear local", e)
            Result.Failure(e)
        }
    }

    suspend fun getLocal(id: String): Result<Local?> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(COLLECTION_LOCAL).document(id).get().await()
            val local = snapshot.toObject(Local::class.java)
            Result.Success(local)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener local", e)
            Result.Failure(e)
        }
    }

    suspend fun getAllLocales(): Result<List<Local>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(COLLECTION_LOCAL).get().await()
            val locales = snapshot.documents.mapNotNull { it.toObject(Local::class.java) }
            Result.Success(locales)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener lista de locales", e)
            Result.Failure(e)
        }
    }

    suspend fun updateLocal(local: Local, uris: List<Uri>? = null): Result<Void?> = withContext(Dispatchers.IO) {
        try {
            val docRef = firestore.collection(COLLECTION_LOCAL).document(local.id ?: return@withContext Result.Failure(Exception("ID nulo")))

            if (!uris.isNullOrEmpty()) {
                val uploadedUrls = uploadImages(local.id!!, uris)
                if (uploadedUrls.size >= 3) {
                    local.foto_p = uploadedUrls[0]
                    local.foto_r_1 = uploadedUrls[1]
                    local.foto_r_2 = uploadedUrls[2]
                }
            }

            docRef.set(local).await()
            Result.Success(null)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar local", e)
            Result.Failure(e)
        }
    }

    suspend fun deleteLocal(id: String): Result<Void?> = withContext(Dispatchers.IO) {
        try {
            firestore.collection(COLLECTION_LOCAL).document(id).delete().await()
            Result.Success(null)
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar local", e)
            Result.Failure(e)
        }
    }

    private suspend fun uploadImages(localId: String, uris: List<Uri>): List<String> = coroutineScope {
        val urls = mutableListOf<String>()
        for ((index, uri) in uris.withIndex()) {
            val path = "$STORAGE_PATH/$localId/image_$index.jpg"
            val ref = storage.reference.child(path)
            val uploadTask = ref.putFile(uri).await()
            val url = ref.downloadUrl.await().toString()
            urls.add(url)
        }
        urls
    }
}
