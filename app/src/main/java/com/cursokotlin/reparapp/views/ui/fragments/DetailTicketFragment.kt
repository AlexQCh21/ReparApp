package com.cursokotlin.reparapp.views.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentDetailTicketBinding
import com.cursokotlin.reparapp.model.Ticket
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class DetailTicketFragment : Fragment() {
    private lateinit var binding: FragmentDetailTicketBinding
    private val args: DetailTicketFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDetailTicketBinding.inflate(inflater,container,false)
        eventListeners()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ticket = args.ticket
        initView(ticket)

        // Puedes usar ticket aquí
    }

    override fun onResume() {
        super.onResume()
        ocultarMBotoomNavigationView()
    }

    private fun eventListeners(){
        binding.imgClose.setOnClickListener {
            findNavController().navigate(
                R.id.homeClientFragment,
                null,
                NavOptions.Builder()
                    .setPopUpTo(R.id.homeClientFragment, false)
                    .build()
            )

        }
    }

    private fun initView(ticket:Ticket){
        val fecha = ticket.createAt?.let {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
        } ?: "Fecha no disponible"

        val hora = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(ticket.createAt)

        binding.tvCodigoTicket.text = ticket.id.toString()
        binding.tvEstado.text = ticket.estadoActual.toString()
        binding.tvDescripcionProblema.text = ticket.descripcion.toString()
        binding.tvFecha.text = fecha.toString()
        binding.tvHora.text = hora.toString()
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

}