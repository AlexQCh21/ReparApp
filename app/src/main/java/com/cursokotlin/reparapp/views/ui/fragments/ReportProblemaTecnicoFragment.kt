package com.cursokotlin.reparapp.views.ui.fragments

import android.app.AlertDialog
import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.cursokotlin.reparapp.databinding.FragmentReportProblemaTecnicoBinding
import com.cursokotlin.reparapp.viewmodel.ReportarProblemaTecnicoViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReportProblemaTecnicoFragment : Fragment() {

    private var _binding: FragmentReportProblemaTecnicoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportarProblemaTecnicoViewModel by viewModels()

    // Variable para guardar la URI de la imagen seleccionada o tomada
    private var imageUri: Uri? = null

    // Para los ActivityResultLaunchers
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private var tempCameraUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa el launcher para galería
        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
                Glide.with(this)
                    .load(it)
                    .into(binding.imgPreviewFoto)
            }
        }

        // Inicializa el launcher para cámara
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success && tempCameraUri != null) {
                imageUri = tempCameraUri
                Glide.with(this)
                    .load(imageUri)
                    .into(binding.imgPreviewFoto)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportProblemaTecnicoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Botón buscar DNI
        binding.btnBuscarDni.setOnClickListener {
            val dni = binding.etDni.text.toString().trim()
            if (dni.length == 8) {
                viewModel.buscarPersonaPorDni(dni)
            } else {
                Toast.makeText(requireContext(), "DNI inválido", Toast.LENGTH_SHORT).show()
            }
        }

        // Observar datos de persona
        viewModel.persona.observe(viewLifecycleOwner) { persona ->
            if (persona != null) {
                binding.etNombreCompletos.setText(persona.full_name)
                binding.etCelular.setText("")
                binding.etDireccion.setText(persona.address)
            } else {
                binding.etNombreCompletos.setText("")
                binding.etCelular.setText("")
                binding.etDireccion.setText("")
                Toast.makeText(requireContext(), "No se encontró al cliente", Toast.LENGTH_SHORT).show()
            }
        }

        // ProgressBar overlay, bloqueo de botón y cambio de texto
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnGenerarTicket.isEnabled = !isLoading
            binding.btnGenerarTicket.text = if (isLoading) "Generando Ticket..." else "Generar Ticket"
        }

        // Error
        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        // Observa éxito y navega atrás si fue exitoso
        viewModel.ticketCreadoExitoso.observe(viewLifecycleOwner) { exito ->
            if (exito == true) {
                findNavController().popBackStack()
                viewModel.resetExito()
                Toast.makeText(requireContext(), "Ticket generado", Toast.LENGTH_SHORT).show()
            }
        }

        // Listener de "adjuntar foto"
        binding.frameAdjuntarFoto.setOnClickListener {
            mostrarDialogoSeleccionImagen()
        }

        // Listener de "Generar Ticket"
        binding.btnGenerarTicket.setOnClickListener {
            val nombreCompleto = binding.etNombreCompletos.text.toString().trim()
            val partes = nombreCompleto.split(",")
            val apellidos = partes.getOrNull(0)?.trim() ?: ""
            val nombres = partes.getOrNull(1)?.trim() ?: ""
            viewModel.reportarProblema(
                dni = binding.etDni.text.toString(),
                nombre = nombres,
                apellido = apellidos,
                telefono = binding.etCelular.text.toString(),
                direccion = binding.etDireccion.text.toString(),
                nombreDispositivo = binding.etNombreDispositivo.text.toString(),
                tipo = binding.etTipo.selectedItem.toString(),
                marca = binding.etMarca.text.toString(),
                modelo = binding.etModelo.text.toString(),
                imageUri = imageUri
            )
        }
    }

    // Dialogo para escoger entre galería o cámara
    private fun mostrarDialogoSeleccionImagen() {
        val opciones = arrayOf("Tomar foto", "Seleccionar de galería")
        AlertDialog.Builder(requireContext())
            .setTitle("Adjuntar Foto")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> tomarFoto()
                    1 -> abrirGaleria()
                }
            }
            .show()
    }

    private fun abrirGaleria() {
        galleryLauncher.launch("image/*")
    }

    private fun tomarFoto() {
        val uri = crearUriTemporal()
        tempCameraUri = uri
        cameraLauncher.launch(uri)
    }

    private fun crearUriTemporal(): Uri {
        val resolver = requireContext().contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}