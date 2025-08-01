package com.cursokotlin.reparapp.views.ui.fragments

import android.app.AlertDialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentDeviceDetailBinding
import com.cursokotlin.reparapp.model.Dispositivo
import com.cursokotlin.reparapp.viewmodel.DeviceDetailViewModel
import com.cursokotlin.reparapp.viewmodel.ResultState
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class DeviceDetailFragment : Fragment() {

    private lateinit var binding: FragmentDeviceDetailBinding
    private val viewModel:DeviceDetailViewModel by viewModels()

    private var selectedImageUri: Uri? = null // Para guardar la URI de la imagen
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.imgPreviewFoto.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDeviceDetailBinding.inflate(inflater, container, false)
        eventsListeners()
        ocultarMBotoomNavigationView()
        initComponents()
        return binding.root
    }

    private fun eventsListeners() {
        //Boton para regresar a la vista anterior
        binding.imgFlechaBack.setOnClickListener {
            findNavController().navigateUp()
        }

        //Botón para añadir una imagen
        binding.frameAdjuntarFoto.setOnClickListener {
            showImageOptions()
        }

        //Botón para Agregar dispositivo
        binding.btnGuardarDispositivo.setOnClickListener {
            saveDeviceClient()
            observarResultadoGuardar()
        }
    }

    private fun initComponents(){
        val spinner: Spinner = binding.etTipo
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.dispositivos_tipos,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun saveDeviceClient() {
        val nombre = binding.etNombreDispositivo.text.toString().trim()
        val tipo = binding.etTipo.selectedItem.toString().trim()
        val marca = binding.etMarca.text.toString().trim()
        val modelo = binding.etModelo.text.toString().trim()
        //val serie = binding.etSerie.text.toString().trim()

        val campos = listOf(nombre, tipo, marca, modelo)

        if (campos.all { it.isNotBlank() }) {
            // Aquí puedes construir el objeto Dispositivo (si ya tienes la clase definida)
            var dispositivo = Dispositivo(
                nombre = nombre,
                tipo = tipo,
                //serie = serie,
                modelo =modelo,
                marca = marca
            )

            // Llamar al método del ViewModel para guardar el dispositivo
            if (selectedImageUri != null) {
                viewModel.saveDispositivo(dispositivo, selectedImageUri!!)
            } else {
                Toast.makeText(requireContext(), "Debes seleccionar o tomar una foto", Toast.LENGTH_SHORT).show()
            }

        } else {
            Toast.makeText(requireContext(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observarResultadoGuardar(){
        viewModel.addDispositivoResult.observe(viewLifecycleOwner){result ->
            when (result){
                is ResultState.Loading ->{
                    binding.btnGuardarDispositivo.isEnabled = false
                    binding.btnGuardarDispositivo.text = "AGREGANDO ..."
                }

                is ResultState.Success -> {
                    findNavController().navigateUp()
                }

                is ResultState.Error ->{
                    Log.e("Error_add_device","error" + result.message)
                }
            }
        }
    }


    private fun showImageOptions() {
        val options = arrayOf("Elegir de la galería", "Tomar foto")
        AlertDialog.Builder(requireContext())
            .setTitle("Selecciona una opción")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> pickImageLauncher.launch("image/*")
                    1 -> takePhotoLauncher.launch(null)
                }
            }
            .show()
    }

    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            // Convierte el Bitmap a URI temporal
            val uri = saveBitmapToCacheAndGetUri(it)
            selectedImageUri = uri
            binding.imgPreviewFoto.setImageBitmap(bitmap) // Muestra la foto tomada
        }
    }

    // Convierte Bitmap a URI temporal
    private fun saveBitmapToCacheAndGetUri(bitmap: Bitmap): Uri {
        val file = File(requireContext().cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
        }
        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
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