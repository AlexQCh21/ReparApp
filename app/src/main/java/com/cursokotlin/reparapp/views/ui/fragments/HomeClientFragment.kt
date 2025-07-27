package com.cursokotlin.reparapp.views.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentHomeUserBinding
import com.cursokotlin.reparapp.viewmodel.HomeUserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeClientFragment : Fragment() {

    private lateinit var binding: FragmentHomeUserBinding
    private val viewModel:HomeUserViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeUserBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        eventsListeners()
        initValueComponents()
        return binding.root
    }

    private fun eventsListeners(){
        binding.imgUsuarioPerfil.setOnClickListener {
            findNavController().navigate(R.id.action_homeClientFragment_to_profileFragment)
        }

        //Agregar ticket
        binding.llAddTicket.setOnClickListener {
            findNavController().navigate(R.id.action_homeClientFragment_to_reportarProblemaFragment)
        }

        //AgregarDispositivo
        binding.llAddDevice.setOnClickListener {
            findNavController().navigate(R.id.action_homeClientFragment_to_agregarDispositivo)
        }
    }

    private fun initValueComponents(){
        //Mostrar el username del usuario
        observeViewModel()

    }

    private fun observeViewModel() {
        viewModel.usuario.observe(viewLifecycleOwner) { usuario ->
            binding.tvUsername.text = usuario?.nombreUsuario ?: "Anónimo"
        }
    }


}