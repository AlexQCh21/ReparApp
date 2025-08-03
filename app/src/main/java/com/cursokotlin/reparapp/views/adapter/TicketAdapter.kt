package com.cursokotlin.reparapp.views.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.model.Ticket
import java.text.SimpleDateFormat
import java.util.Locale

class TicketAdapter(
    private var listaTickets: List<Ticket>,
    private val ticketListener: TicketListener
) : RecyclerView.Adapter<TicketAdapter.TicketViewHolder>() {

    inner class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val idTicket: TextView = itemView.findViewById(R.id.ticket_codigo)
        val fechaTicket: TextView = itemView.findViewById(R.id.ticket_fecha_reporte)
        val btnVerDetalle: Button = itemView.findViewById(R.id.btn_ticket_ver_detalle)

        init {
               itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    ticketListener.onTicketClicked(listaTickets[position], position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ticket, parent, false)
        return TicketViewHolder(view)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val ticket = listaTickets[position]

        val fechaHora = ticket.createAt?.let { date ->
            val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
            val hora = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(date)
            "$fecha - $hora"
        } ?: "Fecha no disponible"

        holder.fechaTicket.text = fechaHora
        holder.idTicket.text = "TIC-${ticket.id}..."

        holder.btnVerDetalle.setOnClickListener {
            ticketListener.onTicketClicked(ticket, position)
        }
    }

    override fun getItemCount(): Int = listaTickets.size

    fun actualizarLista(nuevaLista: List<Ticket>) {
        listaTickets = nuevaLista
        notifyDataSetChanged()
    }
}


