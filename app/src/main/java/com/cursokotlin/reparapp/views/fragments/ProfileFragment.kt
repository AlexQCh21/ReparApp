package com.cursokotlin.reparapp.views.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentProfileBinding
import com.cursokotlin.reparapp.views.activities.AuthActivity
import com.cursokotlin.reparapp.views.utils.AuthManager

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authManager = AuthManager()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        eventListeners()
        return binding.root
    }

    private fun eventListeners() {
        binding.cardCerrarSesion.setOnClickListener {
            // Cierra sesión
            authManager.logout()

            // Redirige a AuthActivity
            val intent = Intent(requireContext(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            // Finaliza la actividad actual
            requireActivity().finish()
        }
    }
}
