package com.cursokotlin.reparapp.viewmodel

data class RegisterFormState(
    val email: String = "",
    val emailError: String? = null,
    val username: String = "",
    val usernameError: String? = null,
    val telefono: String = "",
    val telefonoError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val isValid: Boolean = false
)
