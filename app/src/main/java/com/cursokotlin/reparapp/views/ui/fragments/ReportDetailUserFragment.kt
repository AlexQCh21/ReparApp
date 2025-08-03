package com.cursokotlin.reparapp.views.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentReportDetailUserBinding
import com.cursokotlin.reparapp.viewmodel.SharedTicketViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class ReportDetailUserFragment : Fragment() {

    private lateinit var binding: FragmentReportDetailUserBinding
    private val sharedTicketViewModel: SharedTicketViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReportDetailUserBinding.inflate(inflater, container, false)
        eventListeners()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedTicketViewModel.ticket.observe(viewLifecycleOwner) { ticket ->
            ticket?.let {
                binding.tvCodigoTicket.text = it.id

                // Evita null pointer
                it.createAt?.let { fechaHora ->
                    val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fechaHora)
                    val hora = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(fechaHora)

                    binding.tvFecha.text = fecha
                    binding.tvHora.text = hora

                }
                Log.d("FECHA",it.createAt.toString())
                binding.tvCodigoTicket.text = it.id
                binding.tvDescripcionProblema.text = it.descripcion
                binding.tvEstado.text = it.estadoActual
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ocultarMBotoomNavigationView()
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

    override fun onDestroyView() {
        super.onDestroyView()

        // Muestra el BottomNavigationView de nuevo
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bnv_navegacion_principal)
        bottomNav?.visibility = View.VISIBLE
    }

    private fun ocultarMBotoomNavigationView(){
        // Oculta el BottomNavigationView
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bnv_navegacion_principal)
        bottomNav?.visibility = View.GONE
    }


}
