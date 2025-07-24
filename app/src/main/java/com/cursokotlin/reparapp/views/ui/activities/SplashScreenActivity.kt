package com.cursokotlin.reparapp.views.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.ActivitySplashScreenBinding
import com.cursokotlin.reparapp.model.Usuario
import com.cursokotlin.reparapp.network.AuthManager
import com.cursokotlin.reparapp.viewmodel.SplashUiState
import com.cursokotlin.reparapp.viewmodel.SplashViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pbCarga.isIndeterminate = true
        observeUiState()
    }



    private fun observeUiState() {
        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { state ->
                when (state) {
                    is SplashUiState.Loading -> {
                        binding.pbCarga.visibility = View.VISIBLE
                    }
                    is SplashUiState.NavigateToMain -> {
                        //Cuando esta autenticado te redirige a la vista home
                        val intent = Intent(this@SplashScreenActivity, PrincipalActivity::class.java).apply {
                            putExtra("userRole", state.userRole)
                        }
                        Toast.makeText(this@SplashScreenActivity, "autenticado", Toast.LENGTH_LONG).show()
                        startActivity(intent)
                        finish()
                    }
                    is SplashUiState.NavigateToLogin -> {
                        //Te redirige al login
                        Toast.makeText(this@SplashScreenActivity, "No autenticado", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@SplashScreenActivity, AuthActivity::class.java))
                        finish()
                    }
                    is SplashUiState.ShowError -> {
                        //Te redirige al login
                        Toast.makeText(this@SplashScreenActivity, "Error", Toast.LENGTH_LONG).show()
                        Toast.makeText(this@SplashScreenActivity, state.message, Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@SplashScreenActivity, AuthActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }
}
