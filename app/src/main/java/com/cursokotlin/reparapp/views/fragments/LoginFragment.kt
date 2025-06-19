package com.cursokotlin.reparapp.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentLoginBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class LoginFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inicializa el binding
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        eventListenersButtom()
        return binding.root
    }

    private fun eventListenersButtom(){
        binding.botonLogin.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_loginParte2Fragment)
            Toast.makeText(requireContext(), "Ingresar", Toast.LENGTH_SHORT).show()

        }

        binding.btnRegistrate.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        binding.botonGoogle.setOnClickListener {  }

        binding.botonFacebook.setOnClickListener {  }
    }



}