package com.cursokotlin.reparapp.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cursokotlin.reparapp.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.cursokotlin.reparapp.domain.model.Result
import com.cursokotlin.reparapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _validation = MutableStateFlow<ValidationStateLogin>(ValidationStateLogin.Idle)
    val validation: StateFlow<ValidationStateLogin> = _validation.asStateFlow()

    private val _fieldErrors = MutableStateFlow<FieldErrors?>(null)
    val fieldErrors: StateFlow<FieldErrors?> = _fieldErrors.asStateFlow()

    private var currentEmail: String = ""
    private var currentPassword: String = ""

    fun onEmailChanged(email: String) {
        validateFields(email, null)
    }

    fun onPasswordChanged(password: String) {
        validateFields(null, password)
    }

    private fun validateFields(email: String?, password: String?) {
        email?.let { currentEmail = it }
        password?.let { currentPassword = it }

        val emailError = when {
            currentEmail.isEmpty() -> "El correo es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(currentEmail).matches() -> "Correo inválido"
            else -> null
        }

        val passwordError = when {
            currentPassword.isEmpty() -> "La contraseña es obligatoria"
            else -> null
        }

        _fieldErrors.value = FieldErrors(emailError, passwordError)
    }

    fun signIn() {
        if (_fieldErrors.value?.hasErrors() == true) {
            // Si hay errores de validación, no intentes iniciar sesión
            return
        }

        viewModelScope.launch {
            _validation.value = ValidationStateLogin.Loading
            val result = authRepository.signIn(currentEmail, currentPassword)

            when (result) {
                is Result.Success -> {
                    val user = authRepository.getCurrentUser()
                    val userRoleResult = userRepository.getUserRole(user?.uid ?: "")

                    if (userRoleResult is Result.Success) {
                        if (user?.isEmailVerified == true) {
                            // Correo verificado: estado Verified
                            _validation.value = ValidationStateLogin.Verified(userRoleResult.data)
                        } else {
                            // Correo NO verificado: estado NotVerified con rol
                            _validation.value = ValidationStateLogin.NotVerified(userRoleResult.data)
                        }
                    } else {
                        _validation.value = ValidationStateLogin.Error("No se pudo obtener el rol")
                    }
                }

                is Result.Failure -> {
                    _validation.value = ValidationStateLogin.Error(
                        result.error.message ?: "Error desconocido al iniciar sesión"
                    )
                }

                else -> {
                    _validation.value = ValidationStateLogin.Error("Error desconocido al iniciar sesión")
                }
            }
        }
    }
}

data class FieldErrors(
    val emailError: String? = null,
    val passwordError: String? = null
) {
    fun hasErrors(): Boolean = emailError != null || passwordError != null
}

sealed class ValidationStateLogin {
    object Idle : ValidationStateLogin()
    object Loading : ValidationStateLogin()
    data class Verified(val userRole: String) : ValidationStateLogin()
    data class NotVerified(val userRole: String) : ValidationStateLogin()
    data class Error(val message: String) : ValidationStateLogin()
}
