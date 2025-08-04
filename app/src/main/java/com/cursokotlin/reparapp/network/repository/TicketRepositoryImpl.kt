package com.cursokotlin.reparapp.network.repository

import com.cursokotlin.reparapp.model.EstadoTicket
import com.cursokotlin.reparapp.model.Ticket
import com.cursokotlin.reparapp.network.services.TicketManager
import javax.inject.Inject
import javax.inject.Singleton
import com.cursokotlin.reparapp.model.Result

@Singleton
class TicketRepositoryImpl @Inject constructor(
    private val ticketManager: TicketManager
) {

    suspend fun saveTicket(ticket: Ticket): Result<Ticket> {
        return ticketManager.saveTicket(ticket)
    }

    suspend fun updateTicket(ticket: Ticket): Result<Unit> {
        return ticketManager.updateTicket(ticket)
    }

    suspend fun getAllTickets(): Result<List<Ticket>> {
        return ticketManager.getAll()
    }

    suspend fun agregarEstado(ticketId: String, estado: EstadoTicket): Result<Unit> {
        return ticketManager.agregarEstado(ticketId, estado)
    }

    suspend fun getEstadosPorTicket(ticketId: String): Result<List<EstadoTicket>> {
        return ticketManager.obtenerEstados(ticketId)
    }
}
