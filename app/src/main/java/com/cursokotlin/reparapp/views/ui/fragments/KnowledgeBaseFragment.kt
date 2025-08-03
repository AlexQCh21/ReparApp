package com.cursokotlin.reparapp.views.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.cursokotlin.reparapp.databinding.FragmentKnowledgeBaseBinding
import com.cursokotlin.reparapp.model.BaseConocimiento
import com.cursokotlin.reparapp.viewmodel.BaseConocimientoViewModel
import com.cursokotlin.reparapp.views.adapter.BaseConocimientoAdapter
import com.cursokotlin.reparapp.views.adapter.BaseConocimientoListener
import kotlin.getValue
import androidx.core.net.toUri
import androidx.navigation.fragment.findNavController
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentMapLocalBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class KnowledgeBaseFragment : Fragment(), BaseConocimientoListener {

    private var _binding: FragmentKnowledgeBaseBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BaseConocimientoViewModel by viewModels()
    private lateinit var baseConocimientoAdapter: BaseConocimientoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentKnowledgeBaseBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        baseConocimientoAdapter = BaseConocimientoAdapter(emptyList(), this)
        binding.rvListaBaseConocimientos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvListaBaseConocimientos.adapter = baseConocimientoAdapter

        viewModel.baseConocimiento.observe(viewLifecycleOwner) { base ->
            baseConocimientoAdapter.actualizarLista(base)
        }

        viewModel.getBaseConocimientoClient()

        binding.etBuscarBaseConocimientos.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                baseConocimientoAdapter.filtrar(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.imgFlechaBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        ocultarMBotoomNavigationView()
    }

    override fun onBaseConocimientoClicked(
        baseConocimiento: BaseConocimiento,
        position: Int
    ) {
        val tecnicoId = baseConocimiento.tecnico_id
        viewModel.getNameTech(tecnicoId) { username ->
            val dialog = KnowledgeDetailDialogFragment(baseConocimiento, username)
            dialog.show(parentFragmentManager, "detalle_conocimiento")
        }

    }

    private fun ocultarMBotoomNavigationView(){
        // Oculta el BottomNavigationView
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bnv_navegacion_principal)
        bottomNav?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding =null
        // Muestra el BottomNavigationView de nuevo
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bnv_navegacion_principal)
        bottomNav?.visibility = View.VISIBLE
    }

}