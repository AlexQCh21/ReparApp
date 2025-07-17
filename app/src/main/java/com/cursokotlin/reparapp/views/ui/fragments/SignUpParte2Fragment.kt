package com.cursokotlin.reparapp.views.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.cursokotlin.reparapp.databinding.FragmentRegisterParte2Binding
import com.cursokotlin.reparapp.views.ui.activities.PrincipalActivity
import com.cursokotlin.reparapp.model.Usuario
import com.cursokotlin.reparapp.network.AuthManager
import com.cursokotlin.reparapp.network.FirestoreManager


class SignUpParte2Fragment : Fragment() {
    private lateinit var binding: FragmentRegisterParte2Binding
    private lateinit var authManager: AuthManager
    private lateinit var firestoreManager: FirestoreManager
    private val args: SignUpParte2FragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = SignUpParte2FragmentArgs.fromBundle(requireArguments())
        val email = args.email
        // Inicializar AuthManager
        authManager = AuthManager()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Corrección: Inicializa el binding aquí y retorna binding.root
        binding = FragmentRegisterParte2Binding.inflate(inflater, container, false)

        // Inicializar managers
        authManager = AuthManager()
        firestoreManager = FirestoreManager()

        // Configurar listeners
        eventListenersButton()

        return binding.root
    }

    private fun eventListenersButton(){
        binding.btnContinuarRegistro.setOnClickListener {
            // Si la validación es exitosa, procedemos con el registro
            registerUser()
        }
    }

    private fun validateFields(): Boolean {
        // Limpiar errores previos
        binding.etUsuario.error = null
        binding.etNombre.error = null
        binding.etApellidos.error = null
        binding.etTelefono.error = null
        binding.etClave.error = null
        binding.etConfirmarClave.error = null

        // Obtener valores de los campos
        val usuario = binding.etUsuario.text.toString().trim()
        val nombre = binding.etNombre.text.toString().trim()
        val apellidos = binding.etApellidos.text.toString().trim()
        val telefono = binding.etTelefono.text.toString().trim()
        val password = binding.etClave.text.toString()
        val confirmarPassword = binding.etConfirmarClave.text.toString()

        var isValid = true

        // Validar usuario
        if (usuario.isEmpty()) {
            binding.etUsuario.error = "El usuario es obligatorio"
            isValid = false
        }

        // Validar nombre
        if (nombre.isEmpty()) {
            binding.etNombre.error = "El nombre es obligatorio"
            isValid = false
        }

        // Validar apellidos
        if (apellidos.isEmpty()) {
            binding.etApellidos.error = "Los apellidos son obligatorios"
            isValid = false
        }

        // Validar teléfono
        if (telefono.isEmpty()) {
            binding.etTelefono.error = "El teléfono es obligatorio"
            isValid = false
        } else if (!telefono.matches(Regex("^[0-9]{7,15}\$"))) {
            binding.etTelefono.error = "Teléfono inválido (solo números, mínimo 7 dígitos)"
            isValid = false
        }

        // Validar contraseña
        if (password.isEmpty()) {
            binding.etClave.error = "La contraseña es obligatoria"
            isValid = false
        } else if (password.length < 6) {
            binding.etClave.error = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        }

        // Validar confirmación de contraseña
        if (confirmarPassword.isEmpty()) {
            binding.etConfirmarClave.error = "Debe confirmar la contraseña"
            isValid = false
        } else if (password != confirmarPassword) {
            binding.etConfirmarClave.error = "Las contraseñas no coinciden"
            isValid = false
        }

        return isValid
    }

    private fun registerUser() {
        showLoading(true)

        val email = args.email
        val password = binding.etClave.text.toString()

        authManager.signUpUser(email, password) { isSuccess, firebaseUser, authError ->
            if (isSuccess && firebaseUser != null) {
                // Paso 1: Éxito en Auth. Ahora intentamos guardar en Firestore.
                val nuevoUsuario = Usuario(
                    id = firebaseUser.uid,
                    nombreApellido = "${binding.etNombre.text.toString().trim()} ${binding.etApellidos.text.toString().trim()}",
                    email = email,
                    telefono = binding.etTelefono.text.toString().trim(),
                    rol = "cliente", // O "tecnico" según corresponda
                    isActive = true
                )

                firestoreManager.saveUser(nuevoUsuario) { isSaved, firestoreError ->
                    showLoading(false)

                    if (isSaved) {
                        // ÉXITO: Usuario creado y datos guardados
                        Toast.makeText(context, "Registro completado con éxito", Toast.LENGTH_LONG).show()

                        // Lanzamos PrincipalActivity con el rol
                        val intent = Intent(requireContext(), PrincipalActivity::class.java).apply {
                            putExtra("userRole", nuevoUsuario.rol)
                        }
                        startActivity(intent)

                        // 🗑Cerramos AuthActivity para que no quede en el backstack
                        requireActivity().finish()

                    } else {
                        // Falló guardar en Firestore
                        Log.e("RegisterUser", "Falló el guardado en Firestore. Iniciando rollback de Auth.")
                        Toast.makeText(context, "Error al guardar datos. Deshaciendo registro...", Toast.LENGTH_LONG).show()

                        authManager.deleteCurrentUser { isDeleted, deleteError ->
                            if (isDeleted) {
                                Toast.makeText(context, "No se pudo completar el registro. Por favor, inténtelo de nuevo.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Error crítico: no se pudo deshacer el registro. Contacte a soporte.", Toast.LENGTH_LONG).show()
                                Log.e("RegisterUser", "CRÍTICO: Falló el rollback de Auth. Error: $deleteError")
                            }
                        }
                    }
                }
            } else {
                // Falló crear usuario en Auth
                showLoading(false)
                Toast.makeText(context, "Error de registro: $authError", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingOverlay.apply {
                alpha = 0f
                visibility = View.VISIBLE
                animate().alpha(1f).setDuration(300).start()
            }
            binding.progressBar.isIndeterminate = true

        } else {
            binding.loadingOverlay.animate()
                .alpha(0f).setDuration(300)
                .withEndAction { binding.loadingOverlay.visibility = View.GONE }
                .start()
        }
        binding.btnContinuarRegistro.isEnabled = !isLoading
    }


}