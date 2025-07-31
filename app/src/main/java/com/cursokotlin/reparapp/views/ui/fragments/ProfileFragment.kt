package com.cursokotlin.reparapp.views.ui.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentProfileBinding
import com.cursokotlin.reparapp.model.Usuario
import com.cursokotlin.reparapp.views.ui.activities.AuthActivity
import com.cursokotlin.reparapp.network.AuthManager
import com.cursokotlin.reparapp.viewmodel.HomeUserViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: HomeUserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        eventsListeners()
        initComponents()
        ocultarMBotoomNavigationView()
        return binding.root
    }


    private fun eventsListeners(){
        //Botón para regresar al fragment anterior
        binding.imgFlechaBack.setOnClickListener {
            findNavController().navigateUp()
        }

        //Boton para cerrar sesión
        binding.btnCerrarSesion.setOnClickListener {
            viewModel.cerrarSesion()

            val intent = Intent(requireContext(), AuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            requireActivity().finish()
        }

        //Actualizar mis nombres
        binding.imgUpdateNombres.setOnClickListener {
            val campo ="nombre"
            val currentValue = viewModel.usuario.value?.nombre ?: ""

            showInputDialog("Actualizar mi Nombre", "", currentValue) { nuevoValor ->
                viewModel.actualizarCampoUsuario(campo, nuevoValor) { exito, error ->
                    if (exito) {
                        Toast.makeText(requireContext(), "Nombre actualizado", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), error ?: "Error al actualizar", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        //Actualizar mis apellidos
        binding.imgUpdateApellidos.setOnClickListener {
            val campo ="apellido"
            val currentValue = viewModel.usuario.value?.apellido ?: ""

            showInputDialog("Actualizar mis Apellidos", "", currentValue) { nuevoValor ->
                viewModel.actualizarCampoUsuario(campo, nuevoValor) { exito, error ->
                    if (exito) {
                        Toast.makeText(requireContext(), "Nombre actualizado", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), error ?: "Error al actualizar", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        //Actualizar mi celular
        binding.imgUpdateCelular.setOnClickListener {
            val campo ="telefono"
            val currentValue = viewModel.usuario.value?.telefono ?: ""

            showInputDialog("Actualizar mi telefono", "", currentValue) { nuevoValor ->
                viewModel.actualizarCampoUsuario(campo, nuevoValor) { exito, error ->
                    if (exito) {
                        Toast.makeText(requireContext(), "Telefono actualizado", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), error ?: "Error al actualizar", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        //Actualizar mi dirección
        binding.imgUpdateDireccion.setOnClickListener {
            val campo = "direccion"
            val currentValue = viewModel.usuario.value?.direccion ?: ""

            showInputDialog("Actualizar mi Dirección", "", currentValue) { nuevoValor ->
                viewModel.actualizarCampoUsuario(campo, nuevoValor) { exito, error ->
                    if (exito) {
                        Toast.makeText(requireContext(), "Dirección actualizada", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), error ?: "Error al actualizar", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }



    }

    fun initComponents(){
        viewModel.usuario.observe(viewLifecycleOwner){ usuario->
            if(usuario != null){
                binding.tvNombreCompletos.text = usuario.nombre
                binding.tvApellidosCompletos.text = usuario.apellido
                binding.tvNumeroTelefono.text = if(usuario.telefono!=null) usuario.telefono else ""
                binding.tvCorreo.text = usuario.email
                binding.tvDireccion.text = if(usuario.direccion != null) usuario.direccion else ""
            }

        }

    }

    private fun showInputDialog(
        title: String,
        hint: String,
        currentValue: String,
        onConfirm: (String) -> Unit
    ) {
        val input = EditText(requireContext()).apply {
            setText(currentValue)
            setHint(hint)
            inputType = InputType.TYPE_CLASS_TEXT
            setSelection(currentValue.length)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(input)
            .setPositiveButton("Guardar") { dialog, _ ->
                val newValue = input.text.toString()
                if (newValue.isNotBlank()) {
                    onConfirm(newValue)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.cancel()
            }
            .show()
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
