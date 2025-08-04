package com.cursokotlin.reparapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cursokotlin.reparapp.domain.model.Result
import com.cursokotlin.reparapp.domain.repository.AuthRepository
import com.cursokotlin.reparapp.model.Notificacion
import com.cursokotlin.reparapp.network.services.NotificacionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val notificacionManager: NotificacionManager
) : ViewModel() {

    private val _notificaciones = MutableLiveData<List<Notificacion>>()
    val notificaciones: LiveData<List<Notificacion>> get() = _notificaciones

    private val _obtenerNotificacionResult = MutableStateFlow<ResultState<Boolean>>(ResultState.Success(false))
    val obtenerNotificacionResult: StateFlow<ResultState<Boolean>> get() = _obtenerNotificacionResult

    fun getNotificaciones() {
        viewModelScope.launch {
            when(val userResult = authRepository.getUserData()){
                is Result.Success -> {
                    val usuario = userResult.data
                    val userId = usuario.id
                    val rolUsuario = usuario.rol

                    when(val notificacionesResult = notificacionManager.obtenerNotificacionesPorUsuario(userId)){
                        is Result.Success -> {
                            val notificacionesFiltradas = notificacionesResult.data.filter {
                                it.tipoUsuario == rolUsuario
                            }
                            _notificaciones.value = notificacionesFiltradas
                            _obtenerNotificacionResult.value = ResultState.Success(true)
                        }
                        is Result.Failure -> {
                            _obtenerNotificacionResult.value = ResultState.Error("Error al cargar notificaciones: ${notificacionesResult.error.message}")
                        }
                        else -> {}
                    }

                }
                is Result.Failure ->{
                    _obtenerNotificacionResult.value = ResultState.Error("Error al cargar notificaciones: ${userResult.error.message}")
                }
                else -> {}
            }
        }
    }
}

