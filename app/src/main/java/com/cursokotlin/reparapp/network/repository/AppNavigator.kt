package com.cursokotlin.reparapp.network.repository

interface AppNavigator {
    fun navigateToHome(userRole: String)
    fun navigateToVerification()
    fun showError(message: String)
}