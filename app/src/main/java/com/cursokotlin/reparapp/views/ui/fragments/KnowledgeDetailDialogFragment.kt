package com.cursokotlin.reparapp.views.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.cursokotlin.reparapp.databinding.FragmentKnowledgeDetailDialogBinding
import com.cursokotlin.reparapp.model.BaseConocimiento
import com.bumptech.glide.Glide
import com.cursokotlin.reparapp.R
import java.text.SimpleDateFormat
import java.util.Locale

class KnowledgeDetailDialogFragment(
    private val conocimiento: BaseConocimiento,
    private val nombreTecnico: String
) : DialogFragment() {
    private var _binding: FragmentKnowledgeDetailDialogBinding? = null
    private val binding get()= _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentKnowledgeDetailDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        // Configurar Toolbar
        binding.toolbarKnowledgeDetail.setNavigationOnClickListener{
            dismiss()
        }
        Glide.with(requireContext())
            .load(conocimiento.imagen)
            .placeholder(R.drawable.ic_placeholder)
            .into(binding.imageKnowledge)

        binding.titleKnowledge.text = conocimiento.titulo
        binding.deviceTypeKnowledge.text = "Tipo de dispositivo: ${conocimiento.dispositivo_tipo}"

        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fecha = conocimiento.fecha_publicacion?.let { formatter.format(it) } ?: "sin fecha"
        binding.publishDateKnowledge.text = "Fecha de publicación: $fecha"

        binding.authorKnowledge.text = "Escrito por: $nombreTecnico"
        binding.descriptionKnowledge.text = conocimiento.descripcion
    }

    override fun onStart(){
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}