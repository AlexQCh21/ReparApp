package com.cursokotlin.reparapp.network.services

import android.util.Log
import com.cursokotlin.reparapp.model.AuthError
import com.cursokotlin.reparapp.model.Result
import com.cursokotlin.reparapp.model.flatMap
import com.cursokotlin.reparapp.model.Estado
import com.cursokotlin.reparapp.model.EstadoTicket
import com.cursokotlin.reparapp.model.Notificacion
import com.cursokotlin.reparapp.model.Ticket
import com.cursokotlin.reparapp.network.AuthManager
import com.cursokotlin.reparapp.network.FirestoreManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.Date
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
        const val COLLECTION_TICKETS = "tickets"
        private const val SUBCOLLECTION_ESTADOS = "estados"
    }

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

    suspend fun avanzarEstadoTicket(ticketId: String, comentario: String = "", automatico: Boolean = true): Result<Unit> {
        return try {
            // 1. Obtener el ticket actual
            val ticketRef = firestore.collection(COLLECTION_TICKETS).document(ticketId)
            val ticketSnapshot = ticketRef.get().await()
            val ticket = ticketSnapshot.toObject(Ticket::class.java)
            if (ticket == null) {
                return Result.Failure(AuthError.DatabaseError(Exception("No se encontró el ticket")))
            }

            // 2. Determinar el nuevo estado
            val estadoActual = ticket.estadoActual
            val nuevoEstado = when (estadoActual) {
                Estado.ASIGNADO.toString() -> Estado.INICIADO.toString()
                Estado.INICIADO.toString() -> Estado.TERMINADO.toString()
                else -> estadoActual // No avanza si ya está en TERMINADO u otro
            }
            if (estadoActual == nuevoEstado) {
                return Result.Failure(AuthError.DatabaseError(Exception("El ticket ya está en el estado final")))
            }

            // 3. Actualizar el campo 'ultimo_estado'
            ticketRef.set(mapOf("ultimo_estado" to nuevoEstado), SetOptions.merge()).await()

            // 4. Agregar el nuevo estado a la subcolección 'estados'
            val estadoRef = ticketRef.collection(SUBCOLLECTION_ESTADOS).document()
            val estadoTicket = mapOf(
                "estado" to nuevoEstado,
                "automatico" to automatico,
                "fecha_creacion" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            estadoRef.set(estadoTicket).await()

            // 5. Notificar a ambos
            notificarCambioDeEstado(ticket, nuevoEstado)
            Log.d(TAG, "Estado del ticket $ticketId actualizado automáticamente a $nuevoEstado, subcolección de estados y notificación generada.")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al avanzar el estado del ticket $ticketId", e)
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

    suspend fun getTicketById(ticketId: String): Result<Ticket> {
        return try {
            val doc = firestore.collection(COLLECTION_TICKETS).document(ticketId).get().await()
            val ticket = doc.toObject(Ticket::class.java)?.copy(id = doc.id)
            if (ticket != null) {
                Result.Success(ticket)
            } else {
                Result.Failure(AuthError.DatabaseError(Exception("No se encontró el ticket")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener ticket por id", e)
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

    suspend fun getTicketsAsignados(): Result<List<Ticket>> {
        return try {
            val uid = authManager.getCurrentUser()?.uid
                ?: return Result.Failure(AuthError.Unauthorized)

            val snapshot = firestore.collection(COLLECTION_TICKETS)
                .whereEqualTo("tecnico_id", uid)
                .get()
                .await()

            val lista = snapshot.documents.mapNotNull { doc ->
                val ticket = doc.toObject(Ticket::class.java)
                ticket?.id = doc.id
                ticket
            }

            Result.Success(lista)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Datos del ticket inválidos", e)
            Result.Failure(AuthError.InvalidUserData)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener tickets asignados", e)
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

            //val estadoConId = estado.copy(id = estadoRef.id)

            estadoRef.set(estado).await()

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

                    // Filtra solo tickets con estado "ASIGNADO" o "INICIADO"
                    val ticketsAsignadosOIniciados = tickets.filter {
                        it.estadoActual == Estado.ASIGNADO.toString() || it.estadoActual == Estado.INICIADO.toString()
                    }

                    // Cuenta cuántos tickets de estos tiene cada técnico
                    val cargaPorTecnico = ticketsAsignadosOIniciados.groupingBy { it.tecnico }.eachCount()

                    // Encuentra la carga mínima entre los técnicos
                    val cargaMinima = tecnicos.minOf { tecnico ->
                        cargaPorTecnico[tecnico.id] ?: 0
                    }

                    // Filtra los técnicos que tienen esa carga mínima
                    val tecnicosConMenorCarga = tecnicos.filter {
                        (cargaPorTecnico[it.id] ?: 0) == cargaMinima
                    }

                    // Elige aleatoriamente entre los que tienen menor carga
                    val tecnicoAsignado = tecnicosConMenorCarga.random()
                    ticket.tecnico = tecnicoAsignado.id

                    Result.Success(Unit)
                } catch (e: Exception) {
                    Log.e(TAG, "Error al asignar ticket", e)
                    Result.Failure(AuthError.DatabaseError(e))
                }
            }
        }
    }


//    suspend fun asignarTicket(ticket: Ticket): Result<Unit> {
//        // Evita reasignar si ya tiene técnico
//        if (!ticket.tecnico.isNullOrBlank()) return Result.Success(Unit)
//
//        return userManager.getAllTecnicos().flatMap { tecnicos ->
//            if (tecnicos.isEmpty()) {
//                Result.Failure(AuthError.UnknownError("No hay técnicos disponibles"))
//            } else {
//                try {
//                    val snapshot = firestore.collection(COLLECTION_TICKETS).get().await()
//                    val tickets = snapshot.toObjects(Ticket::class.java)
//
//                    val ticketsPorTecnico = tickets.groupingBy { it.tecnico }.eachCount()
//
//                    val cargaMinima = tecnicos.minOf { tecnico ->
//                        ticketsPorTecnico[tecnico.id] ?: 0
//                    }
//
//                    val tecnicosDisponibles = tecnicos.filter {
//                        (ticketsPorTecnico[it.id] ?: 0) == cargaMinima
//                    }
//
//                    val tecnicoAsignado = tecnicosDisponibles.random()
//                    ticket.tecnico = tecnicoAsignado.id
//
//                    Result.Success(Unit)
//                } catch (e: Exception) {
//                    Log.e(TAG, "Error al asignar ticket", e)
//                    Result.Failure(AuthError.DatabaseError(e))
//                }
//            }
//        }
//    }

    fun notificar(ticket: Ticket, asunto: String) {
        val notificacion = Notificacion(
            visto = false,
            mensaje = asunto,
            receptorId = ticket.tecnico ?: "",
            asuntoId = ticket.id ?: "",
            tipoUsuario = "tecnico"
        )

        notificacionManager.enviarNotificacion(notificacion) { result ->
            when (result) {
                is Result.Success -> Log.d(TAG, "Notificación enviada correctamente")
                is Result.Failure -> Log.e(TAG, "Error al enviar notificación", result.error.cause)
                else -> {}
            }
        }
    }


    //Revertir estado
    suspend fun revertirEstadoTicket(ticketId: String): Result<Unit> {
        try {
            val ticketRef = firestore.collection(COLLECTION_TICKETS).document(ticketId)
            val estadosSnapshot = ticketRef.collection(SUBCOLLECTION_ESTADOS)
                .orderBy("fecha_creacion", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(2)
                .get().await()

            val estados = estadosSnapshot.documents.mapNotNull { it.toObject(EstadoTicket::class.java) }
            if (estados.size < 2) return Result.Failure(AuthError.DatabaseError(Exception("No hay historial suficiente para revertir")))

            val estadoActual = estados[0]
            val estadoAnterior = estados[1]

            // Verifica que el último cambio sea dentro de la hora
            val ahora = Date()
            val haceMenosDeUnaHora = estadoActual.fecha_creacion?.let {
                val diff = ahora.time - it.time
                diff < 60 * 60 * 1000 // 1 hora en milisegundos
            } ?: false

            if (!haceMenosDeUnaHora) {
                return Result.Failure(AuthError.DatabaseError(Exception("No es posible revertir después de 1 hora")))
            }

            // Actualiza el campo 'ultimo_estado' al anterior
            ticketRef.set(mapOf("ultimo_estado" to estadoAnterior.estado), SetOptions.merge()).await()

            // Agrega un registro de "reversión" en la subcolección
            val estadoRevertido = EstadoTicket(
                estado = estadoAnterior.estado,
                automatico = false
            )
            ticketRef.collection(SUBCOLLECTION_ESTADOS).document().set(estadoRevertido).await()
            return Result.Success(Unit)
        } catch (e: Exception) {
            return Result.Failure(AuthError.DatabaseError(e))
        }
    }

    private fun notificarCambioDeEstado(ticket: Ticket, nuevoEstado: String){
        val asunto = "Actualización del ticket"
        val idTicket = ticket.id ?: return

        val mensajeCliente = when(nuevoEstado){
            Estado.ASIGNADO.toString() -> "Tu ticket ha sido asignado a un técnico"
            Estado.INICIADO.toString() -> "El técnico a comenzado a trabajar en tu ticket"
            Estado.TERMINADO.toString() -> "Puedes acercarte a recoger tu dispositivo en la oficina de soporte técnico"
            else -> "El ticket ha cambio de estado a $nuevoEstado"
        }

        val notificacionCliente = Notificacion(
            visto = false,
            mensaje = mensajeCliente,
            receptorId = ticket.cliente,
            tipoUsuario = "cliente",
            asuntoId = idTicket
        )

        notificacionManager.enviarNotificacion(notificacionCliente) { result ->
            when(result){
                is Result.Success -> Log.d(TAG, "Notificación enviada al cliente")
                is Result.Failure -> Log.e(TAG, "Error al notificar al cliente", result.error)
                else -> {}
            }
        }

        ticket.tecnico?.let{ tecnicoId ->
            val mensajeTecnico = when(nuevoEstado){
                Estado.ASIGNADO.toString() -> "Se te ha asignado un nuevo ticket."
                Estado.INICIADO.toString() -> "El ticket ha sido marcado como iniciado"
                Estado.TERMINADO.toString() -> "El ticket ha sido completado"
                else -> "El ticket ha cambio de estado a $nuevoEstado"
            }
            val notificacionTecnico = Notificacion(
                visto = false,
                mensaje = mensajeTecnico,
                receptorId = tecnicoId,
                tipoUsuario = "tecnico",
                asuntoId = idTicket
            )
            notificacionManager.enviarNotificacion(notificacionTecnico) { result ->
                when(result){
                    is Result.Success -> Log.d(TAG, "Notificación enviada al técnico")
                    is Result.Failure -> Log.e(TAG, "Error al notificar al técnico", result.error)
                    else -> {}
                }
            }
        }
    }
}
