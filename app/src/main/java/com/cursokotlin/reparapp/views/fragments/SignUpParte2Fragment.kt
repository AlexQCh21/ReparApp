package com.cursokotlin.reparapp.views.fragments

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
import com.cursokotlin.reparapp.views.activities.PrincipalActivity
import com.cursokotlin.reparapp.views.model.Usuario
import com.cursokotlin.reparapp.views.utils.AuthManager
import com.cursokotlin.reparapp.views.utils.FirestoreManager


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
        binding.botonRegistroContinuar.setOnClickListener {
            // Si la validación es exitosa, procedemos con el registro
            registerUser()
        }
    }

    private fun validateFields(): Boolean {
        // Limpiar errores previos
        binding.tilUsuario.error = null
        binding.tilNombre.error = null
        binding.tilApellidos.error = null
        binding.tilTelefono.error = null
        binding.tilPassword.error = null
        binding.tilConfirmarPassword.error = null

        // Obtener valores de los campos
        val usuario = binding.teUsuario.text.toString().trim()
        val nombre = binding.teNombre.text.toString().trim()
        val apellidos = binding.teApellidos.text.toString().trim()
        val telefono = binding.teTelefono.text.toString().trim()
        val password = binding.tePassword.text.toString()
        val confirmarPassword = binding.teConfirmarPassword.text.toString()

        var isValid = true

        // Validar usuario
        if (usuario.isEmpty()) {
            binding.tilUsuario.error = "El usuario es obligatorio"
            isValid = false
        }

        // Validar nombre
        if (nombre.isEmpty()) {
            binding.tilNombre.error = "El nombre es obligatorio"
            isValid = false
        }

        // Validar apellidos
        if (apellidos.isEmpty()) {
            binding.tilApellidos.error = "Los apellidos son obligatorios"
            isValid = false
        }

        // Validar teléfono
        if (telefono.isEmpty()) {
            binding.tilTelefono.error = "El teléfono es obligatorio"
            isValid = false
        } else if (!telefono.matches(Regex("^[0-9]{7,15}\$"))) {
            binding.tilTelefono.error = "Teléfono inválido (solo números, mínimo 7 dígitos)"
            isValid = false
        }

        // Validar contraseña
        if (password.isEmpty()) {
            binding.tilPassword.error = "La contraseña es obligatoria"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        }

        // Validar confirmación de contraseña
        if (confirmarPassword.isEmpty()) {
            binding.tilConfirmarPassword.error = "Debe confirmar la contraseña"
            isValid = false
        } else if (password != confirmarPassword) {
            binding.tilConfirmarPassword.error = "Las contraseñas no coinciden"
            isValid = false
        }

        return isValid
    }

    private fun registerUser() {
        showLoading(true)

        val email = args.email
        val password = binding.tePassword.text.toString()

        authManager.signUpUser(email, password) { isSuccess, firebaseUser, authError ->
            if (isSuccess && firebaseUser != null) {
                // Paso 1: Éxito en Auth. Ahora intentamos guardar en Firestore.
                val nuevoUsuario = Usuario(
                    id = firebaseUser.uid,
                    nombreApellido = "${binding.teNombre.text.toString().trim()} ${binding.teApellidos.text.toString().trim()}",
                    email = email,
                    telefono = binding.teTelefono.text.toString().trim(),
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
        // Aquí puedes controlar la visibilidad de una ProgressBar
        // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.botonRegistroContinuar.isEnabled = !isLoading
    }


}