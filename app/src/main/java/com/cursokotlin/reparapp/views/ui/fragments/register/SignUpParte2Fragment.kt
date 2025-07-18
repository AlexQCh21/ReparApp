package com.cursokotlin.reparapp.views.ui.fragments.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentRegisterParte2Binding
import com.cursokotlin.reparapp.model.Usuario
import com.cursokotlin.reparapp.viewmodel.RegisterFormState
import com.cursokotlin.reparapp.viewmodel.RegisterUiState
import com.cursokotlin.reparapp.viewmodel.RegisterEvent
import com.cursokotlin.reparapp.viewmodel.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpParte2Fragment : Fragment() {

    private var _binding: FragmentRegisterParte2Binding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels()
    private val args: SignUpParte2FragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterParte2Binding.inflate(inflater, container, false)
        setupTextWatchers()
        setupObservers()
        setupListeners()
        return binding.root
    }

    private fun setupTextWatchers() {
        // Validación en tiempo real
        binding.etNombre.addTextChangedListener { viewModel.onUsernameChanged(it.toString()) }
        binding.etApellidos.addTextChangedListener { viewModel.onUsernameChanged(it.toString()) }
        binding.etTelefono.addTextChangedListener { viewModel.onTelefonoChanged(it.toString()) }
        binding.etClave.addTextChangedListener { viewModel.onPasswordChanged(it.toString()) }
        binding.etConfirmarClave.addTextChangedListener { viewModel.onConfirmPasswordChanged(it.toString()) }
    }

    private fun setupObservers() {
        // Observar cambios en el formulario
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.formState.collect { formState ->
                    updateFormErrors(formState)
                    binding.btnContinuarRegistro.isEnabled = formState.isValid
                    print(formState.isValid)
                }
            }
        }

        // Observar el estado de la UI
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        RegisterUiState.Idle -> resetUI()
                        RegisterUiState.Loading -> showLoading(true)
                        is RegisterUiState.Success -> showLoading(false)
                        is RegisterUiState.Error -> {
                            showLoading(false)
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        // Observar eventos únicos como navegación
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventFlow.collect { event ->
                    when (event) {
                        is RegisterEvent.NavigateToVerification -> {
                            findNavController().navigate(R.id.action_signUpParte2Fragment_to_verificacionEmailFragment)
                        }
                        is RegisterEvent.ShowError -> {
                            Toast.makeText(requireContext(), event.message + " - Manda error por alguna razón", Toast.LENGTH_LONG).show()
                        }
                        null -> {} // Sin evento
                    }
                }
            }
        }
    }

    private fun updateFormErrors(formState: RegisterFormState) {
        binding.etCampoNombre.error = formState.usernameError
        binding.etCampoTelefono.error = formState.telefonoError
        binding.etCampoClave.error = formState.passwordError
        binding.etCampoConfirmarClave.error = formState.confirmPasswordError
    }

    private fun setupListeners() {
        binding.btnContinuarRegistro.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        if (!viewModel.formState.value.isValid) {
            Toast.makeText(context, "Por favor, completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
            return
        }

        val email = args.email
        val password = binding.etClave.text.toString()

        val userData = Usuario(
            id = "",
            nombreApellido = "${binding.etNombre.text} ${binding.etApellidos.text}",
            email = email,
            telefono = binding.etTelefono.text.toString(),
            rol = "cliente",
            isActive = true
        )

        viewModel.signUp(email, password, userData)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.progressBar.isIndeterminate = isLoading
        binding.btnContinuarRegistro.isEnabled = !isLoading
    }

    private fun resetUI() {
        binding.loadingOverlay.visibility = View.GONE
        binding.btnContinuarRegistro.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
