package com.cursokotlin.reparapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cursokotlin.reparapp.model.Result
import com.cursokotlin.reparapp.model.flatMap
import com.cursokotlin.reparapp.model.map
import com.cursokotlin.reparapp.network.repository.AuthRepository
import com.cursokotlin.reparapp.network.repository.UserRepository
import com.cursokotlin.reparapp.model.Usuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    // Estado principal del registro
    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // Eventos de una sola vez (para navegación y mensajes)
    private val _eventFlow = MutableStateFlow<RegisterEvent?>(null)
    val eventFlow: StateFlow<RegisterEvent?> = _eventFlow.asStateFlow()

    // Estado del formulario para validación en tiempo real
    private val _formState = MutableStateFlow(RegisterFormState())
    val formState: StateFlow<RegisterFormState> = _formState.asStateFlow()

    // ==== REGISTRO ====
    fun signUp(email: String, password: String, userData: Usuario) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading

            val result = authRepository.signUp(email, password)
                .flatMap { authResult ->
                    val completeUser = userData.copy(id = authResult.uid)
                    userRepository.saveUser(completeUser)
                        .flatMap {
                            authRepository.sendEmailVerification(authResult)
                                .map { completeUser }
                        }
                }

            when (result) {
                is Result.Success -> {
                    _uiState.value = RegisterUiState.Success(result.data)
                    _eventFlow.value = RegisterEvent.NavigateToVerification
                }
                is Result.Failure -> {
                    val errorMessage = result.error.message ?: "Error en el registro"
                    _uiState.value = RegisterUiState.Error(errorMessage)
                    _eventFlow.value = RegisterEvent.ShowError(errorMessage)
                }

                else ->{}
            }
        }
    }

    // ==== VALIDACIONES EN TIEMPO REAL ====
    fun onUsernameChanged(newUsername: String) {
        val error = if (newUsername.isBlank()) "Nombre obligatorio" else null

        _formState.value = _formState.value.copy(
            username = newUsername,
            usernameError = error,
            isValid = validateForm()
        )
    }

    fun onNombreChanged(nombre: String) {
        val error = if (nombre.isBlank()) "Nombre obligatorio" else null

        _formState.value = _formState.value.copy(
            nombre = nombre,
            nombreError = error,
            isValid = validateForm()
        )
    }

    fun onApellidosChanged(apellidos: String) {
        val error = if (apellidos.isBlank()) "Apellidos obligatorios" else null

        _formState.value = _formState.value.copy(
            apellidos = apellidos,
            apellidosError = error,
            isValid = validateForm()
        )
    }


    fun onTelefonoChanged(newTelefono: String) {
        val error = validateTelefono(newTelefono)

        _formState.value = _formState.value.copy(
            telefono = newTelefono,
            telefonoError = error,
            isValid = validateForm()
        )
    }

    fun onPasswordChanged(newPassword: String) {
        val error = validatePassword(newPassword)

        _formState.value = _formState.value.copy(
            password = newPassword,
            passwordError = error,
            isValid = validateForm()
        )
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        val error = if (_formState.value.password.isBlank()) {
            "Primero es la contraseña"
        } else if(confirmPassword != _formState.value.password) "Las contraseñas no coinciden" else null

        _formState.value = _formState.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = error,
            isValid = validateForm()
        )
    }



    private fun validateTelefono(telefono: String): String? {
        //if (telefono.isBlank()) return "Teléfono obligatorio"
        if (!telefono.matches(Regex("^[0-9]{7,15}\$"))) return "Teléfono inválido"
        if(telefono.length != 9) return "Debe de tener 9 caracteres"
        return null
    }

    private fun validatePassword(password: String): String? {
        if (password.length < 6) return "Mínimo 6 caracteres"
        if (!password.any { it.isDigit() }) return "Debe contener un número"
        if (!password.any { it.isUpperCase() }) return "Debe contener mayúscula"
        if (!password.any { !it.isLetterOrDigit() }) return "Debe contener al menos un caracter especial"
        return null
    }

    private fun validateForm(): Boolean {
        val state = _formState.value
        return listOf(
            state.usernameError,
            state.nombreError,
            state.apellidosError,
            state.telefonoError,
            state.passwordError,
            state.confirmPasswordError
        ).all { it == null } && listOf(
            state.username,
            state.nombre,
            state.apellidos,
            state.telefono,
            state.password,
            state.confirmPassword
        ).all { it.isNotBlank() }
    }
}

// Estados de la UI para el registro
sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    data class Success(val user: Usuario) : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

// Eventos de una sola vez
sealed class RegisterEvent {
    object NavigateToVerification : RegisterEvent()
    data class ShowError(val message: String) : RegisterEvent()
}
