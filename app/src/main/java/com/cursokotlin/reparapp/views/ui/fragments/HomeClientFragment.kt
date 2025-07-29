package com.cursokotlin.reparapp.views.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentHomeUserBinding
import com.cursokotlin.reparapp.model.Ticket
import com.cursokotlin.reparapp.viewmodel.HomeUserViewModel
import com.cursokotlin.reparapp.views.adapter.TicketAdapter
import com.cursokotlin.reparapp.views.adapter.TicketListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeClientFragment : Fragment(), TicketListener {

    private lateinit var binding: FragmentHomeUserBinding
    private lateinit var ticketAdapter: TicketAdapter
    private val viewModel:HomeUserViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeUserBinding.inflate(inflater, container, false)
        eventsListeners()
        initValueComponents()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ticketAdapter = TicketAdapter(emptyList(), this)
        binding.rvHistorialTickets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistorialTickets.adapter = ticketAdapter

        viewModel.tickets.observe(viewLifecycleOwner) { tickets ->
            ticketAdapter.actualizarLista(tickets)
        }

        viewModel.getTicketsClient()
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

        //Ver notificaciones
        binding.imgUsuarioNotificacion.setOnClickListener {
            findNavController().navigate(R.id.action_homeClientFragment_to_notificationFragment)
        }

        //Ver mis tickets generados
        binding.tvVerTodos.setOnClickListener{
            findNavController().navigate(R.id.action_homeClientFragment_to_ticketListUser)
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

    override fun onTicketClicked(ticket: Ticket, position: Int) {
        //findNavController().navigate(R.id.action_homeClientFragment_to_reportarProblemaFragment)
    }


}