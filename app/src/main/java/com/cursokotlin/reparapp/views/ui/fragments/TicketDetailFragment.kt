package com.cursokotlin.reparapp.views.ui.fragments

import android.os.Bundle
import android.util.Log
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
    private lateinit var binding: FragmentTicketDetailBinding
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
        viewModel.checarSiPuedeRevertir(ticket.id ?: "")

        // Observa el resultado del cambio de estado
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cambiarEstadoResult.collect { result ->
                    when (result) {
                        is ResultState.Success -> {
                            viewModel.getTicketById(ticket.id ?: "")
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

        // Después de mostrar el Toast en el Fragment:
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.revertirEstadoResult.collect { result ->
                    when (result) {
                        is ResultState.Success -> {
                            Toast.makeText(requireContext(), "Estado revertido con éxito", Toast.LENGTH_SHORT).show()
                            viewModel.getTicketById(args.ticket.id ?: "")
                            viewModel.checarSiPuedeRevertir(args.ticket.id ?: "")
                            viewModel.setRevertirEstadoIdle()
                        }
                        is ResultState.Error -> {
                            Log.e("TicketDetailFragment", "Error al revertir estado", Throwable(result.message))
                            Toast.makeText(requireContext(), "No se pudo revertir: ${result.message}", Toast.LENGTH_LONG).show()
                            viewModel.setRevertirEstadoIdle()
                        }
                        is ResultState.Loading -> {
                            // Loader opcional
                        }
                    }
                }
            }
        }

        viewModel.ticket.observe(viewLifecycleOwner) { ticketActualizado ->
            actualizarEstadoVisual(ticketActualizado.estadoActual ?: "ASIGNADO")
            // Mostrar el botón SOLO si el estado NO es ASIGNADO
            val estado = ticketActualizado.estadoActual ?: "ASIGNADO"
            binding.btnRevertirEstado.visibility = if (estado != "ASIGNADO") View.VISIBLE else View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        ocultarMBotoomNavigationView()
    }

    private fun eventListener(){
        binding.btnCambiarEstado.setOnClickListener {
            mostrarDialogCambioEstado(args.ticket)
        }

        binding.imgClose.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnRevertirEstado.setOnClickListener {
            viewModel.revertirEstadoTicket(args.ticket.id ?: "")
        }
    }

    private fun initView(ticket: Ticket){
        val fecha = ticket.createAt?.let {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
        } ?: "Fecha no disponible"

        val hora = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(ticket.createAt)

        binding.tvCodigoTicket.text = ticket.id.toString()
        actualizarEstadoVisual(ticket.estadoActual ?: "ASIGNADO")
        binding.tvDescripcionProblema.text = ticket.descripcion
        binding.tvFecha.text = fecha
        binding.tvHora.text = hora.toString()
        binding.tvDescripcionProblema.text = ticket.descripcion

        viewModel.dispositivo.observe(viewLifecycleOwner){dispositivo->
            binding.tvModelo.text = dispositivo.modelo
            binding.tvTipoDispositivo.text = dispositivo.tipo

            Glide.with(requireContext())
                .load(dispositivo.ruta)
                .placeholder(R.drawable.icon_device_text)
                .into(binding.imgDispositivo)
        }
    }

    private fun ocultarMBotoomNavigationView(){
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bnv_navegacion_principal)
        bottomNav?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bnv_navegacion_principal)
        bottomNav?.visibility = View.VISIBLE
    }

    private fun actualizarEstadoVisual(estado: String){
        val contexto = binding.root.context

        val (texto, fondo) = when (estado.uppercase(Locale.getDefault())){
            "ASIGNADO" -> Pair("ASIGNADO", R.drawable.bg_estado_asignado)
            "INICIADO" -> Pair("INICIADO", R.drawable.bg_estado_iniciado)
            "TERMINADO" -> Pair("TERMINADO", R.drawable.bg_estado_terminado)
            else -> Pair(estado, R.drawable.bg_estado_asignado)
        }

        binding.tvEstadoTicket.text = texto
        binding.tvEstadoTicket.setBackgroundResource(fondo)

        binding.btnCambiarEstado.text = when(estado){
            "ASIGNADO" -> "Iniciar Ticket"
            "INICIADO" -> "Finalizar Ticket"
            "TERMINADO" -> "Ticket Terminado"
            else -> "Actualizar Estado"
        }
        binding.btnCambiarEstado.isEnabled = estado != "TERMINADO"
    }

    private fun mostrarDialogCambioEstado(ticket: Ticket) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.fragmen_estado_popup, null)

        val tvEstadoActual = dialogView.findViewById<TextView>(R.id.tvEstadoActual)
        val tvEstadoCambio = dialogView.findViewById<TextView>(R.id.tvEstadoCambio)
        val btnCancelar = dialogView.findViewById<MaterialButton>(R.id.btnCancelar)
        val btnConfirmar = dialogView.findViewById<Button>(R.id.btnConfirmar)

        // Inicialmente muestra el estado actual
        tvEstadoActual.text = ticket.estadoActual
        tvEstadoCambio.text = getSiguienteEstado(ticket.estadoActual)

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnConfirmar.setOnClickListener {
            viewModel.cambiarEstadoTicket(ticket.id ?: "", comentario = "Se ha actualizado el estado del ticket")
        }

        // Observa el ticket actualizado en tiempo real
        viewModel.ticket.observe(viewLifecycleOwner) { ticketActualizado ->
            tvEstadoActual.text = ticketActualizado.estadoActual
            tvEstadoCambio.text = getSiguienteEstado(ticketActualizado.estadoActual)

            // Si el estado es TERMINADO, cierra el dialog automáticamente
            if (ticketActualizado.estadoActual == "TERMINADO") {
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    // Función auxiliar para obtener el siguiente estado
    private fun getSiguienteEstado(estadoActual: String?): String {
        return when (estadoActual) {
            "ASIGNADO" -> "INICIADO"
            "INICIADO" -> "TERMINADO"
            else -> "TERMINADO"
        }
    }

}