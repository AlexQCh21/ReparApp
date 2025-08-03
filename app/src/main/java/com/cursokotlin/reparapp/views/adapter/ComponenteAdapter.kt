package com.cursokotlin.reparapp.views.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.model.Componente
import com.cursokotlin.reparapp.model.Local

class ComponenteAdapter(
    private var listaComponente: List<Componente>
) : RecyclerView.Adapter<ComponenteAdapter.ComponenteViewHolder>() {

    inner class ComponenteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo: TextView = itemView.findViewById(R.id.tv_titulo)
        val cantidad: TextView = itemView.findViewById(R.id.tv_cantidad_componente)
        val img: ImageView = itemView.findViewById(R.id.img_refaccion)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ComponenteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_component, parent, false)
        return ComponenteViewHolder(view)
    }

    override fun getItemCount(): Int = listaComponente.size

    override fun onBindViewHolder(holder: ComponenteViewHolder, position: Int) {
        val item = listaComponente[position]
        holder.titulo.text = item.nombre
        holder.cantidad.text = item.cantidad.toString()
        Glide.with(holder.img.context)
            .load(item.url_foto)
            .placeholder(R.drawable.ic_ticket_nav)
            .error(R.drawable.ic_ticket_nav)
            .into(holder.img)
    }

    // Si necesitas actualizar la lista:
    fun updateList(nuevaLista: List<Componente>) {
        listaComponente = nuevaLista
        notifyDataSetChanged()
    }
}