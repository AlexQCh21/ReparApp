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
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.databinding.ActivitySplashScreenBinding
import com.cursokotlin.reparapp.model.Usuario
import com.cursokotlin.reparapp.network.AuthManager
import com.cursokotlin.reparapp.viewmodel.SplashUiState
import com.cursokotlin.reparapp.viewmodel.SplashViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.black)
        binding.pbCarga.isIndeterminate = true
        observeUiState()
    }


    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is SplashUiState.Loading -> {
                            binding.pbCarga.visibility = View.VISIBLE
                        }
                        is SplashUiState.NavigateToMain -> {
                            startActivity(Intent(this@SplashScreenActivity, PrincipalActivity::class.java).apply {
                                putExtra("userRole", state.userRole)
                            })
                            finish()
                        }
                        is SplashUiState.NavigateToLogin -> {
                            startActivity(Intent(this@SplashScreenActivity, AuthActivity::class.java))
                            finish()
                        }
                        is SplashUiState.ShowError -> {
                            Toast.makeText(this@SplashScreenActivity, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this@SplashScreenActivity, AuthActivity::class.java))
                            finish()
                        }
                    }
                }
            }
        }
    }

}
