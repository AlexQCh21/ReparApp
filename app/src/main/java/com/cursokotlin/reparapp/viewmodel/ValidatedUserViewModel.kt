package com.cursokotlin.reparapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cursokotlin.reparapp.model.AuthError
import com.cursokotlin.reparapp.network.repository.AuthRepository
import com.cursokotlin.reparapp.network.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.cursokotlin.reparapp.model.Result
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class ValidatedUserViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // Estado de la verificación del usuario
    private val _validation = MutableStateFlow<ValidationState>(ValidationState.Idle)
    val validation: StateFlow<ValidationState> = _validation.asStateFlow()

    private var job: Job? = null

    fun checkUserVerification() {
        job?.cancel()
        job = viewModelScope.launch {
            _validation.value = ValidationState.Loading
            authRepository.observeCurrentUser().collectLatest { user ->
                if (user != null) {
                    user.reload() // Actualiza el estado desde Firebase
                    if (user.isEmailVerified) {
                        val userRoleResult = userRepository.getUserRole(user.uid)
                        if (userRoleResult is Result.Success) {
                            val userRole = userRoleResult.data
                            _validation.value = ValidationState.Verified(userRole)
                            job?.cancel()
                        } else {
                            _validation.value = ValidationState.Error("No se pudo obtener el rol")
                        }
                    } else {
                        _validation.value = ValidationState.NotVerified
                    }
                    delay(2000) // Espera 2 segundos antes de volver a comprobar
                }
            }

        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            when (val result = authRepository.sendEmailVerification(authRepository.getCurrentUser())) {
                is Result.Success -> {
                    _validation.value = ValidationState.Message("Correo de verificación enviado correctamente")
                }
                is Result.Failure -> {
                    _validation.value = ValidationState.Error(
                        when (result.error) {
                            is AuthError.EmailAlreadyVerified -> "Tu correo ya está verificado"
                            else -> "No se pudo enviar el correo de verificación"
                        }
                    )
                }

                else->{}
            }
        }
    }

}

sealed class ValidationState {
    object Idle : ValidationState()
    object Loading : ValidationState()
    data class Verified(val userRole: String) : ValidationState()
    object NotVerified : ValidationState()
    data class Error(val message: String) : ValidationState()
    data class Message(val message: String) : ValidationState()
}
