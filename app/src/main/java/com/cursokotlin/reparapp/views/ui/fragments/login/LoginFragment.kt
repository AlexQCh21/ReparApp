package com.cursokotlin.reparapp.views.ui.fragments.login

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentLoginBinding
import com.cursokotlin.reparapp.views.ui.activities.PrincipalActivity
import com.cursokotlin.reparapp.viewmodel.LoginViewModel
import com.cursokotlin.reparapp.viewmodel.ValidationStateLogin
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val viewModel:LoginViewModel by viewModels()
    private lateinit var binding: FragmentLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        setupEventListeners()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeFieldErrors()
        observeLoginState()
    }

    private fun observeFieldErrors() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fieldErrors.collect { errors ->
                    binding.etContenedorCorreo.error = errors?.emailError
                    binding.etContenedorClave.error = errors?.passwordError
                }
            }
        }
    }

    private fun observeLoginState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.validation.collect { state ->
                    when (state) {
                        is ValidationStateLogin.Loading -> showLoading(true)
                        is ValidationStateLogin.Verified -> {
                            showLoading(false)
                            Toast.makeText(requireContext(), "Bienvenido", Toast.LENGTH_SHORT).show()
                            val intent = Intent(requireContext(), PrincipalActivity::class.java).apply {
                                putExtra("userRole", state.userRole)
                            }
                            startActivity(intent)
                            requireActivity().finish()
                        }
                        is ValidationStateLogin.Error -> {
                            showLoading(false)
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }

                        is ValidationStateLogin.NotVerified -> {
                            showLoading(false)
                            val userRole = state.userRole
                            findNavController().navigate(R.id.action_loginFragment_to_verificacionEmailFragment)
                            Toast.makeText(requireContext(), "Verifica tu correo", Toast.LENGTH_LONG).show()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun setupEventListeners() {
        binding.btnIniciarSesion.setOnClickListener {
            // Ocultar el teclado
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(binding.root.windowToken, 0)

            login()
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

        binding.etCorreo.doOnTextChanged { text, _, _, _ ->
            viewModel.onEmailChanged(text.toString())
            binding.etContenedorCorreo.error = null
        }

        binding.etClave.doOnTextChanged { text, _, _, _ ->
            viewModel.onPasswordChanged(text.toString())
            binding.etContenedorClave.error = null
        }

    }

    private fun login() {
        viewModel.signIn()
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
