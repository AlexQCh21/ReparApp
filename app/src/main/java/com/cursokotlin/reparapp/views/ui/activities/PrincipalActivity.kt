package com.cursokotlin.reparapp.views.ui.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.ActivityPrincipalBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrincipalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrincipalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        window.statusBarColor = ContextCompat.getColor(this, android.R.color.black)

        val userRole = intent.getStringExtra("userRole") ?: "cliente"

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        val navGraph = navController.navInflater.inflate(R.navigation.navigation_graph)
        navGraph.setStartDestination(R.id.main_flow)

        val mainFlow = navGraph.findNode(R.id.main_flow) as NavGraph
        mainFlow.setStartDestination(
            if (userRole == "cliente") R.id.homeClientFragment else R.id.homeTechnicalFragment
        )

        if(userRole!="cliente"){
            binding.bnvNavegacionPrincipal.menu.clear()
            binding.bnvNavegacionPrincipal.inflateMenu(R.menu.menu_tecnico)
        }else{
            binding.bnvNavegacionPrincipal.menu.clear()
            binding.bnvNavegacionPrincipal.inflateMenu(R.menu.menu)
        }

        navController.graph = navGraph

        // ESTA ES LA FORMA CORRECTA
        binding.bnvNavegacionPrincipal.setupWithNavController(navController)
    }




}
