package com.cursokotlin.reparapp.network.services

import android.util.Log
import com.cursokotlin.reparapp.domain.model.AuthError
import com.cursokotlin.reparapp.domain.model.Result
import com.cursokotlin.reparapp.model.Dispositivo
import com.cursokotlin.reparapp.model.Ticket
import com.cursokotlin.reparapp.network.repository.AuthRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DispositivoManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepositoryImpl: AuthRepositoryImpl
) {

    companion object {
        private const val TAG = "DispositivoManager"
        private const val COLLECTION_DEVICES = "dispositivos"
    }

    suspend fun saveDispositivo(dispositivo: Dispositivo): Result<Unit> {
        return try {
            val docRef = firestore.collection(COLLECTION_DEVICES).document()
            dispositivo.id = docRef.id // ← asignamos el id
            docRef.set(dispositivo, SetOptions.merge()).await()

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
            val uid = authRepositoryImpl.getCurrentUser()?.uid
                ?: return Result.Failure(AuthError.Unauthorized)

            val snapshot = firestore.collection(COLLECTION_DEVICES)
                .whereEqualTo("usuarioId", uid)
                .get()
                .await()

            val lista = snapshot.documents.map { doc ->
                val dispositivo = doc.toObject(Dispositivo::class.java)
                dispositivo?.id = doc.id
                dispositivo!!
            }

            Result.Success(lista)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener dispositivos", e)
            Result.Failure(AuthError.DatabaseError(e))
        }
    }

    suspend fun getAllDeviceFree(): Result<List<Dispositivo>> {
        return try {
            val uid = authRepositoryImpl.getCurrentUser()?.uid
                ?: return Result.Failure(AuthError.Unauthorized)

            // 1. Obtener todos los tickets del usuario
            val ticketsSnapshot = firestore.collection(TicketManager.COLLECTION_TICKETS)
                .whereEqualTo("usuario_id", uid)
                .get()
                .await()

            // 2. Filtrar dispositivoId con estado ASIGNADO o INICIADO
            val dispositivosOcupados = ticketsSnapshot.documents
                .mapNotNull { it.toObject(Ticket::class.java) }
                .filter { it.estadoActual == "ASIGNADO" || it.estadoActual == "INICIADO" }
                .map { it.dispositivoId }

            // 3. Obtener dispositivos del usuario
            val dispositivosSnapshot = firestore.collection(COLLECTION_DEVICES)
                .whereEqualTo("usuarioId", uid)
                .get()
                .await()

            // 4. Filtrar dispositivos que NO estén en dispositivosOcupados
            val lista = dispositivosSnapshot.documents.mapNotNull { doc ->
                val dispositivo = doc.toObject(Dispositivo::class.java)
                dispositivo?.let {
                    it.id = doc.id
                    if (!dispositivosOcupados.contains(it.id)) it else null
                }
            }

            Result.Success(lista)

        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener dispositivos", e)
            Result.Failure(AuthError.DatabaseError(e))
        }
    }


    suspend fun getDispositivoById(dispositivoId: String): Result<Dispositivo> {
        return try {
            val docSnapshot = firestore.collection(COLLECTION_DEVICES)
                .document(dispositivoId)
                .get()
                .await()

            val dispositivo = docSnapshot.toObject(Dispositivo::class.java)
            if (dispositivo != null) {
                dispositivo.id = docSnapshot.id
                Result.Success(dispositivo)
            } else {
                Result.Failure(AuthError.DatabaseError(Exception("No se encontró el dispositivo")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener dispositivo por id", e)
            Result.Failure(AuthError.DatabaseError(e))
        }
    }

}
