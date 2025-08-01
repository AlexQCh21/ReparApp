package com.cursokotlin.reparapp.views.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.FragmentMapLocalBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapLocalFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapLocalBinding? = null
    private val binding get() = _binding!!

    private val args: MapLocalFragmentArgs by navArgs()
    private lateinit var mMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapLocalBinding.inflate(inflater, container, false)
        eventListener()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ocultarMBotoomNavigationView()
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as? SupportMapFragment

        mapFragment?.getMapAsync(this)
    }

    override fun onResume() {
        super.onResume()
        ocultarMBotoomNavigationView()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("MapDebug", "Mapa listo")
        mMap = googleMap

        val local = args.local
        try {
            val lat = local.latitud
            val lng = local.longitud
            val ubicacion = LatLng(lat, lng)

            mMap.addMarker(MarkerOptions().position(ubicacion).title(local.nombre))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 16f))
        } catch (e: Exception) {
            Log.e("MapError", "Error al cargar coordenadas: ${e.message}")
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
        _binding =null
        // Muestra el BottomNavigationView de nuevo
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bnv_navegacion_principal)
        bottomNav?.visibility = View.VISIBLE
    }
}
