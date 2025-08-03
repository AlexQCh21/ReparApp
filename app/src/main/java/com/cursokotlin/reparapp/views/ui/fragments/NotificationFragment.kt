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
import com.cursokotlin.reparapp.databinding.FragmentNotificationBinding
import com.cursokotlin.reparapp.model.Notificacion
import com.cursokotlin.reparapp.model.Ticket
import com.cursokotlin.reparapp.viewmodel.NotificationViewModel
import com.cursokotlin.reparapp.views.adapter.NotificacionAdapter
import com.cursokotlin.reparapp.views.adapter.NotificacionListener
import com.cursokotlin.reparapp.views.adapter.TicketAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationFragment : Fragment(),NotificacionListener {

    private lateinit var binding: FragmentNotificationBinding
    private lateinit var notificacionAdapter:NotificacionAdapter
    private val viewModel: NotificationViewModel by viewModels()
    private var listaCompleta: List<Notificacion> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationBinding.inflate(inflater, container, false)
        eventListeners()
        initValueComponents()
        ocultarMBotoomNavigationView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        notificacionAdapter = NotificacionAdapter(emptyList(), this)
        binding.rvListaNotificaciones.layoutManager = LinearLayoutManager(requireContext())
        binding.rvListaNotificaciones.adapter = notificacionAdapter

        viewModel.notificaciones.observe(viewLifecycleOwner) { notificaciones ->
            listaCompleta = notificaciones
            notificacionAdapter.actualizarLista(notificaciones)
        }

        viewModel.getNotificaciones()
    }

    private fun initValueComponents() {
        // Aquí puedes inicializar tus componentes más adelante
    }

    private fun eventListeners(){
        binding.imgFlechaBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun ocultarMBotoomNavigationView(){
        // Oculta el BottomNavigationView
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bnv_navegacion_principal)
        bottomNav?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Muestra el BottomNavigationView de nuevo
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bnv_navegacion_principal)
        bottomNav?.visibility = View.VISIBLE
    }

    override fun onTicketClicked(notificacion: Notificacion, position: Int) {

    }

}