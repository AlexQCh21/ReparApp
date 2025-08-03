package com.cursokotlin.reparapp.views.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.model.BaseConocimiento

class BaseConocimientoAdapter(
    private var listaOriginal: List<BaseConocimiento>,
    private val baseListener: BaseConocimientoListener
): RecyclerView.Adapter<BaseConocimientoAdapter.BaseConocimientoViewHolder>()
{
    private var listaFiltrada: List<BaseConocimiento> = listaOriginal

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseConocimientoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_know_prueba, parent, false)
        return BaseConocimientoViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: BaseConocimientoViewHolder,
        position: Int
    ) {
        val baseConocimiento = listaFiltrada[position]
        holder.nombreProblema.text = baseConocimiento.titulo
        holder.descripcion.text = baseConocimiento.descripcion
    }

    override fun getItemCount(): Int = listaFiltrada.size

    fun actualizarLista(nuevaLista: List<BaseConocimiento>) {
        listaOriginal = nuevaLista
        listaFiltrada = nuevaLista
        notifyDataSetChanged()
    }

    fun filtrar(texto: String) {
        listaFiltrada = if (texto.isEmpty()) {
            listaOriginal
        } else {
            listaOriginal.filter {
                it.titulo.contains(texto, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }


    inner class BaseConocimientoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreProblema: TextView = itemView.findViewById(R.id.tv_nombre_problema)
        val descripcion: TextView = itemView.findViewById(R.id.tv_descripcion_solucion)
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    baseListener.onBaseConocimientoClicked(listaFiltrada[position], position)
                }
            }
        }
    }
}