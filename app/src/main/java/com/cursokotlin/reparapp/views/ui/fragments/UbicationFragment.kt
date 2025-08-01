package com.cursokotlin.reparapp.views.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.cursokotlin.reparapp.databinding.FragmentUbicationBinding
import com.cursokotlin.reparapp.model.Local
import com.cursokotlin.reparapp.viewmodel.LocalViewModel
import com.cursokotlin.reparapp.views.adapter.LocalAdapter
import com.cursokotlin.reparapp.views.adapter.LocalListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UbicationFragment : Fragment(),LocalListener {
    private lateinit var binding: FragmentUbicationBinding
    private val viewModel: LocalViewModel by viewModels()
    private lateinit var localAdapter: LocalAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUbicationBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        localAdapter = LocalAdapter(emptyList(), this)
        binding.rvLocalesDisponibles.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLocalesDisponibles.adapter = localAdapter

        viewModel.locales.observe(viewLifecycleOwner) { locales ->
            localAdapter.actualizarLista(locales)
        }

        viewModel.getTicketsClient()
    }

    override fun onLocalClicked(local: Local, position: Int) {
        val action = UbicationFragmentDirections.actionUbicationFragmentToUbicationDetailDialogFragment(local)
        findNavController().navigate(action)
    }

}