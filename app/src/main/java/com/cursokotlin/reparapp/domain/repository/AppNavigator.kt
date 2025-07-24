package com.cursokotlin.reparapp.domain.repository

interface AppNavigator {
    fun navigateToHome(userRole: String)
    fun navigateToVerification()
    fun showError(message: String)
}