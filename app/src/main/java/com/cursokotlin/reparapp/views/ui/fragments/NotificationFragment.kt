package com.cursokotlin.reparapp.views.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentNotificationBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationFragment : Fragment() {

    private lateinit var binding: FragmentNotificationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationBinding.inflate(inflater, container, false)
        eventListeners()
        initValueComponents()
        ocultarMBotoomNavigationView()
        return binding.root
    }

    private fun initValueComponents() {
        // Aquí puedes inicializar tus componentes más adelante
    }

    private fun eventListeners(){
        binding.imgFlechaBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun ocultarMBotoomNavigationView(){
        // Oculta el BottomNavigationView
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bnv_navegacion_principal)
        bottomNav?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Muestra el BottomNavigationView de nuevo
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bnv_navegacion_principal)
        bottomNav?.visibility = View.VISIBLE
    }

}