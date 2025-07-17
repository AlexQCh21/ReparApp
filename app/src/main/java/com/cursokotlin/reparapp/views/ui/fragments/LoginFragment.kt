package com.cursokotlin.reparapp.views.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentLoginBinding
import com.cursokotlin.reparapp.views.ui.activities.PrincipalActivity
import com.cursokotlin.reparapp.model.Usuario
import com.cursokotlin.reparapp.network.AuthManager
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var authManager: AuthManager
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authManager = AuthManager()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        setupEventListeners()
        return binding.root
    }

    private fun setupEventListeners() {
        binding.btnIniciarSesion.setOnClickListener {
            // Ocultar el teclado
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(binding.root.windowToken, 0)

            val email = binding.etCorreo.text.toString().trim()
            val password = binding.etClave.text.toString().trim()
            login(email, password)
        }

        binding.tvRegistrate.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        binding.cardBtnGoogle.setOnClickListener {
            Toast.makeText(requireContext(), "Login con Google (en desarrollo)", Toast.LENGTH_SHORT).show()
        }

        binding.cardBtnFacebook.setOnClickListener {
            Toast.makeText(requireContext(), "Login con Facebook (en desarrollo)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun login(email: String, password: String) {

        // Validaciones
        if (email.isEmpty()) {
            binding.etCorreo.error = "El correo es obligatorio"
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etCorreo.error = "Correo inválido"
            return
        }
        if (password.isEmpty()) {
            binding.etClave.error = "La contraseña es obligatoria"
            return
        }

        //Mostrar loader al iniciar sesión
        showLoading(true)

        // Llama a AuthManager para iniciar sesión
        authManager.signInUser(email, password) { success, user, error ->
            if (success && user != null) {
                // Si el login fue exitoso, buscar el usuario en Firestore
                obtenerDatosUsuario(user.uid)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Error al iniciar sesión: $error",
                    Toast.LENGTH_LONG
                ).show()

                // Ocultar loader si falla
                showLoading(false)
            }
        }
    }

    private fun obtenerDatosUsuario(uid: String) {
        firestore.collection("usuarios").document(uid).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val usuario = documentSnapshot.toObject(Usuario::class.java)
                    if (usuario != null) {
                        if (usuario.isActive) {
                            // Redirige según rol
                            val intent = Intent(requireContext(), PrincipalActivity::class.java)
                            intent.putExtra("userRole", usuario.rol)
                            startActivity(intent)
                            requireActivity().finish()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Tu cuenta está desactivada. Contacta al administrador.",
                                Toast.LENGTH_LONG
                            ).show()
                            authManager.logout()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "No se pudo obtener la información del usuario",
                            Toast.LENGTH_SHORT
                        ).show()
                        authManager.logout()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Usuario no encontrado en la base de datos",
                        Toast.LENGTH_SHORT
                    ).show()
                    authManager.logout()
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Error al obtener datos: ${it.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
                authManager.logout()
            }
    }


    /**
     * Mostrar u ocultar el loader con fondo semitransparente
     */
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingOverlay.apply {
                alpha = 0f
                visibility = View.VISIBLE
                animate().alpha(1f).setDuration(300).start()
            }
        } else {
            binding.loadingOverlay.animate()
                .alpha(0f).setDuration(300)
                .withEndAction { binding.loadingOverlay.visibility = View.GONE }
                .start()
        }
        // Opcional: deshabilitar botón mientras carga
        binding.btnIniciarSesion.isEnabled = !isLoading
    }
}
