package com.cursokotlin.reparapp.network.services

import android.util.Log
import com.cursokotlin.reparapp.domain.model.AuthError
import com.cursokotlin.reparapp.domain.model.Result
import com.cursokotlin.reparapp.domain.model.flatMap
import com.cursokotlin.reparapp.model.Dispositivo
import com.cursokotlin.reparapp.model.EstadoTicket
import com.cursokotlin.reparapp.model.Notificacion
import com.cursokotlin.reparapp.model.Ticket
import com.cursokotlin.reparapp.network.AuthManager
import com.cursokotlin.reparapp.network.FirestoreManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TicketManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userManager: FirestoreManager,
    private val authManager: AuthManager,
    private val notificacionManager: NotificacionManager
) {

    companion object {
        private const val TAG = "TicketManager"
        private const val COLLECTION_TICKETS = "tickets"
        private const val SUBCOLLECTION_ESTADOS = "estados"
    }

//    suspend fun saveTicket(ticket: Ticket): Result<Ticket> {
//        return try {
//            val docRef = firestore.collection(COLLECTION_TICKETS).document()
//            val ticketWithId = ticket.copy(id = docRef.id)
//
//            val asignacionResult = asignarTicket(ticketWithId)
//            if (asignacionResult is Result.Failure) return Result.Failure(asignacionResult.error)
//
//            firestore.collection(COLLECTION_TICKETS)
//                .document(ticketWithId.id!!)
//                .set(ticketWithId, SetOptions.merge())
//                .await()
//
//            notificar(ticketWithId, "Se te asignó un nuevo ticket")
//
//            Log.d(TAG, "Ticket guardado con éxito: $ticketWithId")
//            Result.Success(ticketWithId) // <-- Devuelve el ticket creado con su id
//        } catch (e: IllegalArgumentException) {
//            Log.e(TAG, "Datos del ticket inválidos", e)
//            Result.Failure(AuthError.InvalidUserData)
//        } catch (e: Exception) {
//            Log.e(TAG, "Error al guardar ticket", e)
//            Result.Failure(AuthError.DatabaseError(e))
//        }
//    }

    suspend fun saveTicket(ticket: Ticket): Result<Ticket> {
        return try {
            val docRef = firestore.collection(COLLECTION_TICKETS).document()
            val ticketWithId = ticket.copy(id = docRef.id)

            val asignacionResult = asignarTicket(ticketWithId)
            if (asignacionResult is Result.Failure) return Result.Failure(asignacionResult.error)

            // Guardamos el ticket en Firestore
            docRef.set(ticketWithId, SetOptions.merge()).await()

            // Volvemos a obtener el ticket desde Firestore para que traiga el timestamp generado por el servidor
            val snapshot = docRef.get().await()
            val ticketConTimestamp = snapshot.toObject(Ticket::class.java)?.copy(id = docRef.id)

            // Notificamos
            if (ticketConTimestamp != null) {
                notificar(ticketConTimestamp, "Se te asignó un nuevo ticket")
                Log.d(TAG, "Ticket guardado con éxito: $ticketConTimestamp")
                Result.Success(ticketConTimestamp)
            } else {
                Result.Failure(AuthError.DatabaseError(Exception("No se pudo obtener el ticket guardado")))
            }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Datos del ticket inválidos", e)
            Result.Failure(AuthError.InvalidUserData)
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar ticket", e)
            Result.Failure(AuthError.DatabaseError(e))
        }
    }


    suspend fun getAll(): Result<List<Ticket>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_TICKETS).get().await()
            val tickets = snapshot.documents.mapNotNull { it.toObject(Ticket::class.java) }

            Log.d(TAG, "Tickets obtenidos con éxito: ${tickets.size}")
            Result.Success(tickets)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Datos del ticket inválidos", e)
            Result.Failure(AuthError.InvalidUserData)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener tickets", e)
            Result.Failure(AuthError.DatabaseError(e))
        }
    }

    suspend fun getMyTickets(): Result<List<Ticket>>{
        return try {
            val uid = authManager.getCurrentUser()?.uid
                ?: return Result.Failure(AuthError.Unauthorized)

            val snapshot = firestore.collection(COLLECTION_TICKETS)
                .whereEqualTo("usuario_id", uid)
                .get()
                .await()

            val lista = snapshot.documents.map { doc ->
                val ticket = doc.toObject(Ticket::class.java)
                ticket?.id = doc.id
                ticket!!
            }

            return Result.Success(lista)
        }catch (e: IllegalArgumentException) {
            Log.e(TAG, "Datos del ticket inválidos", e)
            Result.Failure(AuthError.InvalidUserData)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener tickets", e)
            Result.Failure(AuthError.DatabaseError(e))
        }
    }

    suspend fun updateTicket(ticket: Ticket): Result<Unit> {
        return try {
            val ticketId = ticket.id
            if (ticketId.isNullOrBlank()) {
                Log.e(TAG, "ID del ticket inválido")
                return Result.Failure(AuthError.InvalidUserData)
            }

            firestore.collection(COLLECTION_TICKETS)
                .document(ticketId)
                .set(ticket, SetOptions.merge())
                .await()

            Log.d(TAG, "Ticket actualizado con éxito: $ticket")
            Result.Success(Unit)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Datos del ticket inválidos", e)
            Result.Failure(AuthError.InvalidUserData)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar ticket", e)
            Result.Failure(AuthError.DatabaseError(e))
        }
    }

    suspend fun agregarEstado(ticketId: String, estado: EstadoTicket): Result<Unit> {
        return try {
            val estadoRef = firestore.collection(COLLECTION_TICKETS)
                .document(ticketId)
                .collection(SUBCOLLECTION_ESTADOS)
                .document()

            val estadoConId = estado.copy(id = estadoRef.id)

            estadoRef.set(estadoConId).await()

            Log.d(TAG, "Estado agregado al ticket $ticketId: ${estado.estado}")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al agregar estado al ticket $ticketId", e)
            Result.Failure(AuthError.DatabaseError(e))
        }
    }

    suspend fun obtenerEstados(ticketId: String): Result<List<EstadoTicket>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_TICKETS)
                .document(ticketId)
                .collection(SUBCOLLECTION_ESTADOS)
                .orderBy("fecha_creacion")
                .get()
                .await()

            val estados = snapshot.documents.mapNotNull { it.toObject(EstadoTicket::class.java) }

            Log.d(TAG, "Estados del ticket $ticketId obtenidos con éxito: ${estados.size}")
            Result.Success(estados)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener estados del ticket $ticketId", e)
            Result.Failure(AuthError.DatabaseError(e))
        }
    }

    suspend fun asignarTicket(ticket: Ticket): Result<Unit> {
        // Evita reasignar si ya tiene técnico
        if (!ticket.tecnico.isNullOrBlank()) return Result.Success(Unit)

        return userManager.getAllTecnicos().flatMap { tecnicos ->
            if (tecnicos.isEmpty()) {
                Result.Failure(AuthError.UnknownError("No hay técnicos disponibles"))
            } else {
                try {
                    val snapshot = firestore.collection(COLLECTION_TICKETS).get().await()
                    val tickets = snapshot.toObjects(Ticket::class.java)

                    val ticketsPorTecnico = tickets.groupingBy { it.tecnico }.eachCount()

                    val cargaMinima = tecnicos.minOf { tecnico ->
                        ticketsPorTecnico[tecnico.id] ?: 0
                    }

                    val tecnicosDisponibles = tecnicos.filter {
                        (ticketsPorTecnico[it.id] ?: 0) == cargaMinima
                    }

                    val tecnicoAsignado = tecnicosDisponibles.random()
                    ticket.tecnico = tecnicoAsignado.id

                    Result.Success(Unit)
                } catch (e: Exception) {
                    Log.e(TAG, "Error al asignar ticket", e)
                    Result.Failure(AuthError.DatabaseError(e))
                }
            }
        }
    }

    fun notificar(ticket: Ticket, asunto: String) {
        val notificacion = Notificacion(
            visto = false,
            mensaje = asunto,
            receptorId = ticket.tecnico ?: "",
            asuntoId = ticket.id ?: "",
            tipoUSuario = "tecnico"
        )

        notificacionManager.enviarNotificacion(notificacion) { result ->
            when (result) {
                is Result.Success -> Log.d(TAG, "Notificación enviada correctamente")
                is Result.Failure -> Log.e(TAG, "Error al enviar notificación", result.error.cause)
                else -> {}
            }
        }
    }
}
