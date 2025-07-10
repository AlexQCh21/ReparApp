package com.cursokotlin.reparapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cursokotlin.reparapp.databinding.ActivitySplashScreenBinding
import com.cursokotlin.reparapp.views.activities.AuthActivity
import com.cursokotlin.reparapp.views.activities.PrincipalActivity
import com.cursokotlin.reparapp.views.model.Usuario
import com.cursokotlin.reparapp.views.utils.AuthManager
import com.google.firebase.firestore.FirebaseFirestore

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var authManager: AuthManager
    private lateinit var binding:ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        authManager = AuthManager()

        binding.progress.isIndeterminate = true
        binding.progress.visibility = View.VISIBLE

        // Espera 2 segundos (2000 ms) antes de lanzar la app principal
        Handler(Looper.getMainLooper()).postDelayed({
//            startActivity(Intent(this, AuthActivity::class.java))
//            finish()
            userAuthenticate()
        }, 2000)
    }

    private fun setupNavigation(){
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }

    private fun userAuthenticate() {
        val currentUser = authManager.getCurrentUser()
        if (currentUser != null) {
            //Hay un usuario logueado, obtener datos de Firestore
            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val usuario = document.toObject(Usuario::class.java)
                        if (usuario?.isActive == true) {
                            // Abrir PrincipalActivity con el rol
                            val intent = Intent(this, PrincipalActivity::class.java)
                            intent.putExtra("userRole", usuario.rol)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Tu cuenta está desactivada. Contacta al administrador.",
                                Toast.LENGTH_LONG
                            ).show()
                            authManager.logout()
                            setupNavigation()
                        }
                    } else {
                        Toast.makeText(this, "No se encontraron datos del usuario.", Toast.LENGTH_SHORT).show()
                        authManager.logout()
                        setupNavigation()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                    authManager.logout()
                    setupNavigation()
                }
        } else {
            // No hay usuario logueado, carga flujo de autenticación
            setupNavigation()
        }
    }


}