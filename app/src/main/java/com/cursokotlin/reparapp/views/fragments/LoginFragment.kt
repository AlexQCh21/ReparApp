package com.cursokotlin.reparapp.views.fragments

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
import com.cursokotlin.reparapp.views.activities.PrincipalActivity
import com.cursokotlin.reparapp.views.model.Usuario
import com.cursokotlin.reparapp.views.utils.AuthManager
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
        binding.botonLogin.setOnClickListener {
            val email = binding.textField.editText?.text.toString().trim()
            val password = binding.textFieldPassword.editText?.text.toString().trim()
            login(email, password)
        }

        binding.btnRegistrate.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        binding.botonGoogle.setOnClickListener {
            Toast.makeText(requireContext(), "Login con Google (en desarrollo)", Toast.LENGTH_SHORT).show()
        }

        binding.botonFacebook.setOnClickListener {
            Toast.makeText(requireContext(), "Login con Facebook (en desarrollo)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun login(email: String, password: String) {
        // Validaciones
        if (email.isEmpty()) {
            binding.textField.error = "El correo es obligatorio"
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textField.error = "Correo inválido"
            return
        }
        if (password.isEmpty()) {
            binding.textFieldPassword.error = "La contraseña es obligatoria"
            return
        }

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
}
