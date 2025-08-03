package com.cursokotlin.reparapp.views.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentHomeTechnicalBinding
import com.cursokotlin.reparapp.model.BaseConocimiento
import com.cursokotlin.reparapp.model.Estado
import com.cursokotlin.reparapp.model.Ticket
import com.cursokotlin.reparapp.viewmodel.BaseConocimientoViewModel
import com.cursokotlin.reparapp.viewmodel.TicketsAsignadoViewModel
import com.cursokotlin.reparapp.views.adapter.BaseConocimientoAdapter
import com.cursokotlin.reparapp.views.adapter.BaseConocimientoListener
import com.cursokotlin.reparapp.views.adapter.TicketAdapter
import com.cursokotlin.reparapp.views.adapter.TicketListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeTechnicalFragment : Fragment(), TicketListener, BaseConocimientoListener {
    private lateinit var binding: FragmentHomeTechnicalBinding
    private val viewModel: TicketsAsignadoViewModel by viewModels()
    private lateinit var ticketAdapter: TicketAdapter

    private val viewModelBase: BaseConocimientoViewModel by viewModels()
    private lateinit var baseConocimientoAdapter: BaseConocimientoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeTechnicalBinding.inflate(inflater, container, false)
        showPerfilTech()
        initComponents()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.tickets.observe(viewLifecycleOwner) { tickets ->
            ticketAdapter.actualizarLista(tickets)
            if (tickets.isNullOrEmpty()) {
                binding.tvNoTickets.visibility = View.VISIBLE
                binding.rvHistorialTickets.visibility = View.GONE
            } else {
                binding.tvNoTickets.visibility = View.GONE
                binding.rvHistorialTickets.visibility = View.VISIBLE
            }
        }


        //Cargar los tickets
        ticketAdapter = TicketAdapter(emptyList(), this)
        binding.rvHistorialTickets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistorialTickets.adapter = ticketAdapter

        viewModel.tickets.observe(viewLifecycleOwner) { tickets ->
            ticketAdapter.actualizarLista(tickets)
        }

        viewModel.getTicketsAsignados()

        //Cargar base de conocimiento
        baseConocimientoAdapter = BaseConocimientoAdapter(emptyList(), this)
        binding.rvBaseConocimientos.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvBaseConocimientos.adapter = baseConocimientoAdapter

        viewModelBase.baseConocimiento.observe(viewLifecycleOwner) { locales ->
            baseConocimientoAdapter.actualizarLista(locales)
        }

        viewModelBase.getBaseConocimientoClient()
    }

    private fun initComponents(){
        viewModel.tickets.observe(viewLifecycleOwner) { tickets ->
            // Agrupa los tickets por su estado actual y cuenta la cantidad en cada grupo
            val conteoPorEstado: Map<String, Int> = tickets.groupingBy { it.estadoActual }.eachCount()

            // Obtén los conteos, si no existe algún estado, será 0
            val asignados = conteoPorEstado[Estado.ASIGNADO.toString()] ?: 0
            val iniciados = conteoPorEstado[Estado.INICIADO.toString()] ?: 0
            val terminados = conteoPorEstado[Estado.TERMINADO.toString()] ?: 0

            // Ejemplo: mostrar en el log
           binding.tvTicketsPendientes.text = asignados.toString()
            binding.tvTicketsAsignados.text = iniciados.toString()
            binding.tvTicketsTerminados.text = terminados.toString()

            // Aquí puedes actualizar tu UI con los valores

            binding.imgUsuarioNotificacion.setOnClickListener {
                findNavController().navigate(R.id.action_homeTechnicalFragment_to_notificationFragment)
            }
        }

        binding.tvVerMas.setOnClickListener {
            findNavController().navigate(R.id.action_homeTechnicalFragment_to_baseConocimientoFragment)
        }
    }

    private fun showPerfilTech(){
        binding.imgUsuarioPerfil.setOnClickListener {
            findNavController().navigate(R.id.action_homeTechnicalFragment_to_profileFragment)
        }
    }

    override fun onTicketClicked(ticket: Ticket, position: Int) {
        val action = HomeTechnicalFragmentDirections.actionHomeTechnicalFragmentToTicketDetailFragment(ticket)
        findNavController().navigate(action)
    }

    override fun onBaseConocimientoClicked(
        baseConocimiento: BaseConocimiento,
        position: Int
    ) {
        val url = baseConocimiento.enlace
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.let {
            ContextCompat.startActivity(it, intent, null)
        }
    }

}