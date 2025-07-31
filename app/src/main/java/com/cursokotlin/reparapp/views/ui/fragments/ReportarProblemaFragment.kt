package com.cursokotlin.reparapp.views.ui.fragments

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentReportarProblemaBinding
import com.cursokotlin.reparapp.viewmodel.ReportarProblemaViewModel
import com.cursokotlin.reparapp.viewmodel.ResultState
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class ReportarProblemaFragment : Fragment() {

    private var _binding: FragmentReportarProblemaBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ReportarProblemaViewModel

    private val CAMERA_REQUEST = 1001
    private val GALLERY_REQUEST = 1002
    private var imageUri: Uri? = null

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            imageUri?.let { uri ->
                binding.imgPreviewFoto.setImageURI(uri)
                viewModel.setImagenSeleccionada(uri)
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            binding.imgPreviewFoto.setImageURI(it)
            viewModel.setImagenSeleccionada(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportarProblemaBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ReportarProblemaViewModel::class.java]
        ocultarMBotoomNavigationView()
        setupUI()
        return binding.root
    }

    private fun setupUI() {
        binding.imgFlechaBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.frameAdjuntarFoto.setOnClickListener {
            showImagePickerDialog()
        }

        binding.btnAddTicket.setOnClickListener {
            val descripcion = binding.etDescripcionProblema.text.toString().trim()
            val posicion = binding.spinnerTipoDispositivo.selectedItemPosition
            val dispositivo = viewModel.dispositivos.value.getOrNull(posicion)

            viewModel.crearTicket(descripcion, dispositivo)
        }

        // Spinner
        lifecycleScope.launchWhenStarted {
            viewModel.dispositivos.collect { lista ->
                val nombres = lista.map { it.nombre }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombres)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerTipoDispositivo.adapter = adapter
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.crearTicketResult.collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        binding.btnAddTicket.isEnabled = false
                        binding.btnAddTicket.text = "Guardando..."
                    }

                    is ResultState.Success -> {
                        if (result.data) {
                            Toast.makeText(requireContext(), "Ticket creado exitosamente", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        } else {
                            binding.btnAddTicket.isEnabled = true
                            binding.btnAddTicket.text = "Agregar Ticket"
                        }
                    }

                    is ResultState.Error -> {
                        binding.btnAddTicket.isEnabled = true
                        binding.btnAddTicket.text = "Agregar Ticket"
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }


    private fun showImagePickerDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar imagen")
            .setItems(arrayOf("Tomar foto", "Seleccionar de galería")) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        val photoFile = File.createTempFile("ticket_", ".jpg", requireContext().cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        imageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )

        cameraLauncher.launch(imageUri)
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

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

