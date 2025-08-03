package com.cursokotlin.reparapp.views.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.model.Notificacion

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
        if(notify.tipoUSuario=="tecnico"){
            holder.titulo.text = "Nuevo ticket"
        }else{
            holder.titulo.text = "El técnico ha actualizado tu ticket"
        }
        holder.mensaje.text = notify.mensaje
    }

    fun actualizarLista(nuevaLista: List<Notificacion>) {
        listaNotificaciones = nuevaLista
        notifyDataSetChanged()
    }


}