package com.cursokotlin.reparapp.views.fragments

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentRegisterBinding




class SignUpFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inicializa el binding
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        eventListenersButton()
        return binding.root
    }

    private fun eventListenersButton(){
        binding.botonLoginContinuar.setOnClickListener {
            // 1. Obtenemos el texto del email y lo convertimos a String
            val email = binding.teEmail.text.toString().trim()

            // 2. Validamos el email
            if (validateEmail(email)) {
                // 3. Si es válido, creamos la acción con el email como argumento
                val action = SignUpFragmentDirections.actionSignUpFragmentToSignUpParte2Fragment(email)

                // 4. Navegamos usando la acción
                findNavController().navigate(action)
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        // Limpiar errores previos
        binding.tilEmail.error = null

        // Validar email
        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_empty_email)
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            return false
        }

        // Si pasa todas las validaciones, es válido
        return true
    }


}