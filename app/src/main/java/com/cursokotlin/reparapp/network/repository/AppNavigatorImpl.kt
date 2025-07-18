package com.cursokotlin.reparapp.network.repository

import androidx.navigation.NavHostController
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.domain.repository.AppNavigator

class AppNavigatorImpl(
    private val navController: NavHostController
) : AppNavigator {

    override fun navigateToHome(userRole: String) {
        navController.navigate(
            when (userRole) {
                "tecnico" -> R.id.homeTechnicalFragment
                else -> R.id.homeClientFragment
            }
        ) {
            popUpTo(R.id.auth_flow) { inclusive = true }
        }
    }

    override fun navigateToVerification() {
        navController.navigate(R.id.action_signUpParte2Fragment_to_verificacionEmailFragment)
    }

    override fun showError(message: String) {
        // Puedes usar un Toast aquí si es necesario
    }
}
