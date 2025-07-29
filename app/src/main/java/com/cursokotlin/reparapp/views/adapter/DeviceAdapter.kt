package com.cursokotlin.reparapp.views.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.model.Dispositivo
import com.bumptech.glide.Glide

class DeviceAdapter(
    private var listaDispositivos: List<Dispositivo>,
):RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {
    inner class DeviceViewHolder(itemViewn:View):RecyclerView.ViewHolder(itemViewn){
        val img: ImageView = itemView.findViewById(R.id.img_dispositivo)
        val nombre: TextView = itemView.findViewById(R.id.tv_modelo_dispositivo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device,parent,false)

        return DeviceViewHolder(view)
    }

    override fun getItemCount(): Int = listaDispositivos.size

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val dispositivo = listaDispositivos[position]

        // Cargar imagen
        Glide.with(holder.itemView.context)
            .load(dispositivo.ruta)
            .placeholder(R.drawable.ic_placeholder)
            .into(holder.img)

        holder.nombre.text = dispositivo.nombre
    }

    fun actualizarLista(nuevaLista: List<Dispositivo>) {
        listaDispositivos = nuevaLista
        notifyDataSetChanged()
    }
}