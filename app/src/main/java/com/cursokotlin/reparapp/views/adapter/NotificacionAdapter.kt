package com.cursokotlin.reparapp.views.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.model.Notificacion
import java.text.SimpleDateFormat
import java.util.Locale

class NotificacionAdapter (
    private var listaNotificaciones: List<Notificacion>,
    private val notificacionListener: NotificacionListener
) : RecyclerView.Adapter<NotificacionAdapter.NotificacionViewHolder>() {

    inner class NotificacionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo: TextView = itemView.findViewById(R.id.tv_titulo)
        val mensaje: TextView = itemView.findViewById(R.id.tv_mensaje)
        val fecha: TextView = itemView.findViewById(R.id.tv_hora)


        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    notificacionListener.onTicketClicked(listaNotificaciones[position], position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificacionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification,parent,false)
        return NotificacionViewHolder(view)
    }

    override fun getItemCount(): Int = listaNotificaciones.size

    override fun onBindViewHolder(holder: NotificacionViewHolder, position: Int) {
        val notify = listaNotificaciones[position]
        holder.titulo.text = notify.asuntoId
        holder.mensaje.text = notify.mensaje
        val fechaHora = notify.createAt?.let { date ->
            val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
            val hora = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(date)
            "$fecha - $hora"
        } ?: "Fecha no disponible"

        holder.fecha.text = fechaHora

    }

    fun actualizarLista(nuevaLista: List<Notificacion>) {
        listaNotificaciones = nuevaLista
            .sortedByDescending { it.createAt }
        notifyDataSetChanged()
    }


}