package com.cursokotlin.reparapp.views.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentTicketDetailBinding
import com.cursokotlin.reparapp.model.Ticket
import com.cursokotlin.reparapp.viewmodel.ResultState
import com.cursokotlin.reparapp.viewmodel.TicketsAsignadoViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class TicketDetailFragment : Fragment() {
    private lateinit var binding:FragmentTicketDetailBinding
    private val args: TicketDetailFragmentArgs by navArgs()
    private val viewModel:TicketsAsignadoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTicketDetailBinding.inflate(inflater,container,false)
        eventListener()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ticket = args.ticket
        viewModel.getDispositivoTicket(ticket.dispositivoId)
        initView(ticket)

        // Observa el resultado del cambio de estado
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cambiarEstadoResult.collect { result ->
                    when (result) {
                        is ResultState.Success -> {
                            // Este método debe existir en tu ViewModel y llamar a TicketManager.getTicketById
                            viewModel.getTicketById(ticket.id ?: "")
                            //Toast.makeText(requireContext(), "Estado actualizado", Toast.LENGTH_SHORT).show()
                        }
                        is ResultState.Error -> {
                            Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                        }
                        is ResultState.Loading -> {
                            // Loader opcional
                        }
                    }
                }
            }
        }

        viewModel.ticket.observe(viewLifecycleOwner) { ticketActualizado ->
            binding.tvEstado.text = ticketActualizado.estadoActual
            // Refresca otros datos si necesitas
        }
    }

    override fun onResume() {
        super.onResume()
        ocultarMBotoomNavigationView()
    }

    private fun eventListener(){
        binding.fabAgregarTicket.setOnClickListener {
            mostrarDialogCambioEstado(args.ticket)
        }

        binding.imgClose.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initView(ticket: Ticket){
        val fecha = ticket.createAt?.let {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
        } ?: "Fecha no disponible"

        val hora = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(ticket.createAt)

        binding.tvCodigoTicket.text = ticket.id.toString()
        binding.tvEstado.text = ticket.estadoActual
        binding.tvDescripcionProblema.text = ticket.descripcion
        binding.tvFecha.text = fecha
        binding.tvHora.text = hora.toString()
        binding.tvDescripcionProblema.text = ticket.descripcion


        viewModel.dispositivo.observe(viewLifecycleOwner){dispositivo->
            binding.tvModelo.text = dispositivo.modelo
            binding.tvTipoDispositivo.text = dispositivo.tipo

            Glide.with(requireContext())
                .load(dispositivo.ruta)
                .placeholder(R.drawable.icon_map)
                .into(binding.imgDispositivo)
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

    private fun mostrarDialogCambioEstado(ticket: Ticket) {
        // Inflar la vista personalizada
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.fragmen_estado_popup, null)

        val tvEstadoActual = dialogView.findViewById<TextView>(R.id.tvEstadoActual)
        val tvEstadoCambio = dialogView.findViewById<TextView>(R.id.tvEstadoCambio)
        val btnCancelar = dialogView.findViewById<MaterialButton>(R.id.btnCancelar)
        val btnConfirmar = dialogView.findViewById<Button>(R.id.btnConfirmar)

        // Estado actual
        tvEstadoActual.text = ticket.estadoActual

        // Determinar el siguiente estado
        val siguienteEstado = when(ticket.estadoActual) {
            "ASIGNADO" -> "INICIADO"
            "INICIADO" -> "TERMINADO"
            else -> "TERMINADO" // Si ya es TERMINADO, permanece igual
        }
        tvEstadoCambio.text = siguienteEstado

        // Crear el AlertDialog
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancelar.setOnClickListener { dialog.dismiss() }
        btnConfirmar.setOnClickListener {
            // Llama al método de tu viewModel para cambiar el estado
            viewModel.cambiarEstadoTicket(ticket.id ?: "", comentario = "Se ha actulizado el estado del ticket")
            dialog.dismiss()
        }

        dialog.show()
    }
}