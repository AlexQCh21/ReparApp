package com.cursokotlin.reparapp.views.ui.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.ActivityPrincipalBinding

class PrincipalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrincipalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Ajusta los insets para pantalla completa
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val userRole = intent.getStringExtra("userRole") ?: "cliente"

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Inflar el grafo completo
        val navGraph = navController.navInflater.inflate(R.navigation.navigation_graph)

         // Aquí cambiamos el startDestination del GRAFO RAÍZ según el rol
        navGraph.setStartDestination(R.id.main_flow)

        // Ahora dentro de main_flow ponemos el destino inicial según el rol
        val mainFlow = navGraph.findNode(R.id.main_flow) as NavGraph
        mainFlow.setStartDestination(
            if (userRole == "cliente") R.id.homeClientFragment else R.id.homeTechnicalFragment
        )

        // Asigna el grafo modificado al NavController
        navController.graph = navGraph


        // Configura el BottomNavigationView
        binding = ActivityPrincipalBinding.inflate(layoutInflater)
        binding.bnvNavegacionPrincipal.setupWithNavController(navController)
    }



}
