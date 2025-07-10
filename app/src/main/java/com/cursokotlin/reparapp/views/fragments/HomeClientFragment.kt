package com.cursokotlin.reparapp.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentHomeUserBinding
import com.cursokotlin.reparapp.databinding.FragmentProfileBinding
import com.cursokotlin.reparapp.views.utils.AuthManager


class HomeClientFragment : Fragment() {

    private lateinit var binding: FragmentHomeUserBinding
    private lateinit var authManager: AuthManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authManager = AuthManager()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeUserBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        showPerfilUser()
        return binding.root
    }

    private fun showPerfilUser(){
        binding.btnPerfilUser.setOnClickListener {
            findNavController().navigate(R.id.action_homeClientFragment_to_profileFragment)
        }
    }
}