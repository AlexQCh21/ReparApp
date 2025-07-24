package com.cursokotlin.reparapp.views.ui.fragments.register

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentLoginBinding
import com.cursokotlin.reparapp.databinding.FragmentVerificationEmailBinding
import com.cursokotlin.reparapp.viewmodel.ValidatedUserViewModel
import com.cursokotlin.reparapp.viewmodel.ValidationState
import com.cursokotlin.reparapp.views.ui.activities.PrincipalActivity
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import dalvik.system.ZipPathValidator
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VerificationEmailFragment : Fragment() {

    private val viewModel:ValidatedUserViewModel by viewModels()
    private lateinit var binding: FragmentVerificationEmailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inicia la verificación del correo
        viewModel.checkUserVerification()
        observerValidateUser()
        binding = FragmentVerificationEmailBinding.inflate(inflater, container, false)
        binding.btnReenviarCorreo.setOnClickListener {
            viewModel.resendVerificationEmail()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.validation.collect { state ->
                    when (state) {
                        is ValidationState.Message -> {
                            //Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                        is ValidationState.Error -> {
                            //Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                        else -> { /* No hacer nada si está en estado Idle u otro */ }
                    }
                }
            }
        }



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Detectar botón back
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // Navegar al LoginFragment
//            findNavController().navigate(R.id.loginFragment) {
//                popUpTo(R.id.loginFragment) {
//                    inclusive = true
//                }
//            }
        }
    }


    private fun observerValidateUser(){
        lifecycleScope.launch {
          viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
              viewModel.validation.collect{ state ->
                  when(state){
                      is ValidationState.Verified -> {
                          val intent = Intent(requireContext(), PrincipalActivity::class.java).apply {
                              putExtra("userRole", state.userRole)
                          }

                          startActivity(intent)
                          requireActivity().finish()
                      }
                      else->{}
                  }
              }
          }
        }
    }
}