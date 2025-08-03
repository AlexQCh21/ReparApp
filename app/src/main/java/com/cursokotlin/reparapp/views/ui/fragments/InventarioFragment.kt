package com.cursokotlin.reparapp.views.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentInventoryBinding
import com.cursokotlin.reparapp.model.Componente
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

        var listaCompleta: List<Componente> = emptyList()

        viewModel.componente.observe(viewLifecycleOwner) { componentes ->
            listaCompleta = componentes
            componenteAdapter.updateList(componentes)
        }

        // Agrega el filtro de búsqueda aquí
        binding.etBuscarRefacciones.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val textoFiltrado = s.toString().trim()
                val listaFiltrada = if (textoFiltrado.isEmpty()) {
                    listaCompleta
                } else {
                    listaCompleta.filter {
                        it.nombre.contains(textoFiltrado, ignoreCase = true)
                    }
                }
                componenteAdapter.updateList(listaFiltrada)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        viewModel.getComponentes()
    }

}