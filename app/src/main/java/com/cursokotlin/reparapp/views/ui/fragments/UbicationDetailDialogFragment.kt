package com.cursokotlin.reparapp.views.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentUbicationDetailDialogBinding
import com.cursokotlin.reparapp.model.Local
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UbicationDetailDialogFragment : Fragment() {
    private lateinit var binding: FragmentUbicationDetailDialogBinding
    private val args: UbicationDetailDialogFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUbicationDetailDialogBinding.inflate(inflater,container,false)
        eventListener()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val local = args.local
        initView(local)
    }

    override fun onResume() {
        super.onResume()
        ocultarMBotoomNavigationView()
    }

    private fun initView(local: Local) {
        // Cargar imagen
        Glide.with(requireContext())
            .load(local.foto_p)
            .placeholder(R.drawable.icon_map)
            .into(binding.imgFotoLocal)

        Glide.with(requireContext())
            .load(local.foto_r_1)
            .placeholder(R.drawable.icon_map)
            .into(binding.imgLocalFrontal)

        Glide.with(requireContext())
            .load(local.foto_r_2)
            .placeholder(R.drawable.icon_map)
            .into(binding.imgLocalFrontal2)

        binding.tvDireccion.text = local.direccion
        binding.tvHorarioLaboral.text = local.horario_atencion
        binding.tvTelefono.text = local.telefono

        binding.btnMapa.setOnClickListener {
            val action = UbicationDetailDialogFragmentDirections.actionUbicationDetailDialogFragmentToMapLocalFragment(local)
            findNavController().navigate(action)
        }
    }

    private fun eventListener(){
        binding.imgFlechaBack.setOnClickListener {
            findNavController().popBackStack()
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