package com.cursokotlin.reparapp.views.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentInventoryBinding
import com.cursokotlin.reparapp.viewmodel.ComponenteViewModel
import com.cursokotlin.reparapp.viewmodel.LocalViewModel
import com.cursokotlin.reparapp.views.adapter.ComponenteAdapter
import com.cursokotlin.reparapp.views.adapter.LocalAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InventarioFragment : Fragment() {
    private var _binding : FragmentInventoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ComponenteViewModel by viewModels()
    private lateinit var componenteAdapter:ComponenteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentInventoryBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        componenteAdapter = ComponenteAdapter(emptyList())
        binding.rvListaPiezasDisponibles.layoutManager = LinearLayoutManager(requireContext())
        binding.rvListaPiezasDisponibles.adapter = componenteAdapter

        viewModel.componente.observe(viewLifecycleOwner) { componentes ->
            componenteAdapter.updateList(componentes)
        }

        viewModel.getComponentes()
    }
}