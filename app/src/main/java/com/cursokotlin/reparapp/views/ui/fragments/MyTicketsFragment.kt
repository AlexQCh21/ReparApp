package com.cursokotlin.reparapp.views.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentMyTicketsBinding
import com.cursokotlin.reparapp.model.Estado
import com.cursokotlin.reparapp.model.Ticket
import com.cursokotlin.reparapp.viewmodel.TicketsAsignadoViewModel
import com.cursokotlin.reparapp.views.adapter.TicketAdapter
import com.cursokotlin.reparapp.views.adapter.TicketListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyTicketsFragment : Fragment(),TicketListener {
    private lateinit var binding: FragmentMyTicketsBinding
    private val viewModel: TicketsAsignadoViewModel by viewModels()
    private lateinit var ticketAdapter: TicketAdapter
    private var listaCompleta: List<Ticket> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMyTicketsBinding.inflate(inflater,container,false)
        eventsListeners()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ticketAdapter = TicketAdapter(emptyList(), this)
        binding.rvListaTickets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvListaTickets.adapter = ticketAdapter

        viewModel.tickets.observe(viewLifecycleOwner) { tickets ->
            listaCompleta = tickets
            ticketAdapter.actualizarLista(tickets)
        }

        viewModel.getTicketsAsignados()
    }

    private fun eventsListeners(){
        //Boton para regresar a la vista anterior
//        binding.imgFlechaBack.setOnClickListener{
//            findNavController().navigateUp()
//        }

        binding.imgFilter.setOnClickListener {
            mostrarDialogFiltro()
        }

    }

    override fun onTicketClicked(ticket: Ticket, position: Int) {
        val action = MyTicketsFragmentDirections.actionMyTicketsFragmentToTicketDetailFragment(ticket)
        findNavController().navigate(action)
    }

    private fun mostrarDialogFiltro(){
        val opciones = arrayOf("Todos", "Asignado", "Iniciado", "Terminado")
        AlertDialog.Builder(requireContext())
            .setTitle("Filtrar Tickets")
            .setItems(opciones) { _, which ->
                val estado = when (which) {
                    0 -> "todos"
                    1 -> Estado.ASIGNADO.name
                    2 -> Estado.INICIADO.name
                    3 -> Estado.TERMINADO.name
                    else -> "todos"
                }
                filtrarTickets(estado)
            }
            .show()
    }

    private fun filtrarTickets(estado: String) {
        val listaFiltrada = when (estado) {
            "todos" -> listaCompleta
            else -> listaCompleta.filter { it.estadoActual == estado }
        }
        ticketAdapter.actualizarLista(listaFiltrada)
    }

}