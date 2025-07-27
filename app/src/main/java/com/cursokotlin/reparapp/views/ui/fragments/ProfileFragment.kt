package com.cursokotlin.reparapp.views.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentProfileBinding
import com.cursokotlin.reparapp.views.ui.activities.AuthActivity
import com.cursokotlin.reparapp.network.AuthManager
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        eventsListeners()
        return binding.root
    }


    private fun eventsListeners(){
        //Botón para regresar al fragment anterior
        binding.imgFlechaBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

}
