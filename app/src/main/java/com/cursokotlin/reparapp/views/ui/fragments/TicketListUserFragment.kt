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
import com.cursokotlin.reparapp.databinding.FragmentTicketListUserBinding
import com.cursokotlin.reparapp.model.Ticket
import com.cursokotlin.reparapp.viewmodel.HomeUserViewModel
import com.cursokotlin.reparapp.views.adapter.TicketAdapter
import com.cursokotlin.reparapp.views.adapter.TicketListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TicketListUserFragment : Fragment(),TicketListener {
    private lateinit var binding: FragmentTicketListUserBinding
    private val viewModel: HomeUserViewModel by viewModels()
    private lateinit var ticketAdapter: TicketAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTicketListUserBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        eventsListeners()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ticketAdapter = TicketAdapter(emptyList(), this)
        binding.rvListaTickets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvListaTickets.adapter = ticketAdapter

        viewModel.tickets.observe(viewLifecycleOwner) { tickets ->
            ticketAdapter.actualizarLista(tickets)
        }

        viewModel.getTicketsClient()
    }

    private fun eventsListeners(){
        //Boton para regresar a la vista anterior
        binding.imgFlechaBack.setOnClickListener{
            findNavController().navigateUp()
        }
    }

    override fun onTicketClicked(ticket: Ticket, position: Int) {
        TODO("Not yet implemented")
    }

}