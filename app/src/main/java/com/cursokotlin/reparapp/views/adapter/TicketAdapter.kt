package com.cursokotlin.reparapp.views.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.model.Ticket

class TicketAdapter(
    val listaTickets : List<Ticket>,
    //val firestoreService : FirestoreService,
    val ticketListener: TicketListener,
    val onItemClick: (Ticket) -> Unit):
    RecyclerView.Adapter<TicketAdapter.TicketViewHolder>() {
    class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descripcionTicket: TextView = itemView.findViewById(R.id.tv_descripcion_problema)
        val fechaTicket: TextView = itemView.findViewById(R.id.tv_ticket_fecha_reporte)
        val btnVerDetalle: Button = itemView.findViewById(R.id.btn_ticket_ver_detalle)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ticket, parent, false)
        return TicketViewHolder(view)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val ticket = listaTickets[position]
        //holder.descripcionTicket.text = ticket.descripcion
        //holder.fechaTicket
        holder.btnVerDetalle.setOnClickListener{
            ticketListener.onTicketClicked(ticket, position)
        }
    }

    override fun getItemCount(): Int = listaTickets.size
}

