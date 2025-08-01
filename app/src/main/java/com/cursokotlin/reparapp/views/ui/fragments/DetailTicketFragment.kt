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

    private fun eventListeners(){
        binding.imgCerrar.setOnClickListener {
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


}