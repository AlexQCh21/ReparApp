package com.cursokotlin.reparapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cursokotlin.reparapp.domain.model.AuthError
import com.cursokotlin.reparapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.cursokotlin.reparapp.domain.model.Result
import com.cursokotlin.reparapp.domain.repository.AuthRepository
import kotlinx.coroutines.delay

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authManager: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkUserAuthentication()
    }

    private fun checkUserAuthentication() {
        viewModelScope.launch {
            delay(500)
            val currentUser = authManager.getCurrentUser()
            Log.i("SPLASH", "Usuario actual: ${currentUser?.email}")
            if (currentUser != null) {
                when (val userResult = userRepository.getUser(currentUser.uid)) {
                    is Result.Success -> {
                        val usuario = userResult.data
                        val isVerified = authManager.getCurrentUser()?.isEmailVerified == true
                        if (usuario.isActive && isVerified) {
                            // Usuario activo y verificado -> navega a PrincipalActivity con su rol
                            _uiState.value = SplashUiState.NavigateToMain(usuario.rol)
                        } else {
                            _uiState.value = SplashUiState.ShowError("Tu cuenta está desactivada. Contacta al administrador.")
                        }
                    }
                    is Result.Failure -> {
                        val errorMsg = when (userResult.error) {
                            AuthError.UserNotFound -> "No se encontraron datos del usuario."
                            is AuthError.DatabaseError -> "Error de base de datos."
                            else -> "Error desconocido."
                        }
                        _uiState.value = SplashUiState.ShowError(errorMsg)
                    }

                    else -> { /* no debería pasar, pero necesario para ser exhaustivo */ }
                }

            } else {
                // Usuario no autenticado -> navega al login
                _uiState.value = SplashUiState.NavigateToLogin
            }
        }
    }



}

sealed class SplashUiState {
    object Loading : SplashUiState()
    data class NavigateToMain(val userRole: String) : SplashUiState()
    object NavigateToLogin : SplashUiState()
    data class ShowError(val message: String) : SplashUiState()
}
