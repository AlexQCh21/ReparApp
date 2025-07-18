package com.cursokotlin.reparapp.views.ui.fragments.register

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentRegisterBinding

class SignUpFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        setupListeners()
        return binding.root
    }

    private fun setupListeners() {
        // Valida en tiempo real mientras el usuario escribe
        binding.etCorreo.doOnTextChanged { text, _, _, _ ->
            if (binding.etCorreo.error != null) {
                binding.etCampoCorreo.error = null // Limpia el error si el usuario corrige
            }
        }

        // Botón "Continuar"
        binding.btnContinuarRegistro.setOnClickListener {
            val email = binding.etCorreo.text.toString().trim()

            if (validateEmail(email)) {
                // Email válido: navega con argumento
                val action = SignUpFragmentDirections
                    .actionSignUpFragmentToSignUpParte2Fragment(email)
                findNavController().navigate(action)
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        // Limpia errores previos
        binding.etCampoCorreo.error = null

        return when {
            email.isEmpty() -> {
                binding.etCampoCorreo.error = getString(R.string.error_empty_email)
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.etCampoCorreo.error = getString(R.string.error_invalid_email)
                false
            }
            else -> true // Email válido
        }
    }
}
