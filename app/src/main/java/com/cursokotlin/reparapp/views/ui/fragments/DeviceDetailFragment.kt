package com.cursokotlin.reparapp.views.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.cursokotlin.reparapp.databinding.FragmentDeviceDetailBinding

class DeviceDetailFragment : Fragment() {

    private lateinit var binding: FragmentDeviceDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDeviceDetailBinding.inflate(inflater, container, false)
        eventsListeners()
        return binding.root
    }

    private fun eventsListeners() {
        //Boton para regresar a la vista anterior
        binding.imgFlechaBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}