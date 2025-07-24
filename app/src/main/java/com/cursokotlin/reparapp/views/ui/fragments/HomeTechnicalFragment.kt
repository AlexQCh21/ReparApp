package com.cursokotlin.reparapp.views.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentHomeTechnicalBinding


class HomeTechnicalFragment : Fragment() {
    private lateinit var binding: FragmentHomeTechnicalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeTechnicalBinding.inflate(inflater, container, false)
        showPerfilTech()
        return binding.root
    }

    private fun showPerfilTech(){
        binding.cardBtnPerfilTecnico.setOnClickListener {
            findNavController().navigate(R.id.action_homeTechnicalFragment_to_profileFragment)
        }
    }

}