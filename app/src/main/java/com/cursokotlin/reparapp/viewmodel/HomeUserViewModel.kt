package com.cursokotlin.reparapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cursokotlin.reparapp.domain.model.Result
import com.cursokotlin.reparapp.domain.repository.AuthRepository
import com.cursokotlin.reparapp.domain.repository.UserRepository
import com.cursokotlin.reparapp.model.Usuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeUserViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _usuario = MutableLiveData<Usuario?>(null)
    val usuario: LiveData<Usuario?> = _usuario

    init {
        obtenerUsuario()
    }

    private fun obtenerUsuario() {
        viewModelScope.launch {
            val firebaseUser = authRepository.getCurrentUser()
            val userId = firebaseUser?.uid

            if (!userId.isNullOrEmpty()) {
                when (val result = userRepository.getUser(userId)) {
                    is Result.Success -> _usuario.value = result.data
                    is Result.Failure -> {
                        // Podrías usar otro StateFlow para errores si quieres
                    }
                    else->{}
                }
            }
        }
    }
}
