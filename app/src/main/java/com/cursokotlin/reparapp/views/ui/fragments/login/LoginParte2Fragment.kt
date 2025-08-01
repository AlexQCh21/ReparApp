package com.cursokotlin.reparapp.views.ui.fragments.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentLoginBinding
import com.cursokotlin.reparapp.databinding.FragmentLoginParte2Binding
import com.cursokotlin.reparapp.viewmodel.LoginViewModel
import com.cursokotlin.reparapp.viewmodel.ValidationStateLogin
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginParte2Fragment : Fragment() {

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var binding: FragmentLoginParte2Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginParte2Binding.inflate(inflater, container, false)
        eventListener()
        observeViewModel()
        return binding.root
    }

    private fun eventListener() {
        binding.btnContinuarLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            if (email.isBlank()) {
                Toast.makeText(requireContext(), "Ingrese un correo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.sendResetPassword(email)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.validation.collect { state ->
                    when (state) {
                        is ValidationStateLogin.Error -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            findNavController().navigateUp()
                        }
                        is ValidationStateLogin.Loading -> {
                            // Puedes mostrar un ProgressBar si deseas
                        }
                        is ValidationStateLogin.Idle -> {
                            // Nada o limpiar estados
                        }
                        else -> {
                            // Otros estados como Verified, NotVerified, etc.
                        }
                    }
                }
            }
        }
    }


}