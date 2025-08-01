package com.cursokotlin.reparapp.views.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.model.Local

class LocalAdapter(
    private var listaLocales: List<Local>,
    private val localListener: LocalListener
) : RecyclerView.Adapter<LocalAdapter.LocalViewHolder>() {

    inner class LocalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.tv_local_nombre)
        val direccion: TextView = itemView.findViewById(R.id.tv_local_direccion)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    localListener.onLocalClicked(listaLocales[position], position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ubication, parent, false)
        return LocalViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocalViewHolder, position: Int) {
        val local = listaLocales[position]
        holder.direccion.text = local.direccion
        holder.nombre.text = local.nombre
    }

    override fun getItemCount(): Int = listaLocales.size

    fun actualizarLista(nuevaLista: List<Local>) {
        listaLocales = nuevaLista
        notifyDataSetChanged()
    }
}
