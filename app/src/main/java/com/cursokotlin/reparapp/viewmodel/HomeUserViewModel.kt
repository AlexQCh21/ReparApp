package com.cursokotlin.reparapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cursokotlin.reparapp.domain.model.Result
import com.cursokotlin.reparapp.domain.repository.AuthRepository
import com.cursokotlin.reparapp.domain.repository.UserRepository
import com.cursokotlin.reparapp.model.Ticket
import com.cursokotlin.reparapp.model.Usuario
import com.cursokotlin.reparapp.network.services.TicketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeUserViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val ticketManager: TicketManager,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _usuario = MutableLiveData<Usuario?>(null)
    val usuario: LiveData<Usuario?> = _usuario

    private val _tickets = MutableLiveData<List<Ticket>>()
    val tickets : LiveData<List<Ticket>> get() = _tickets

    private val _obtenerTicketsClienteResult = MutableStateFlow<ResultState<Boolean>>(ResultState.Success(false))
    val obtenerTicketsClienteResult: StateFlow<ResultState<Boolean>> = _obtenerTicketsClienteResult

    init {
        obtenerUsuario()
        //getTicketsClient()
    }

    private fun obtenerUsuario() {
        viewModelScope.launch {
            val firebaseUser = authRepository.getCurrentUser()
            val userId = firebaseUser?.uid

            if (!userId.isNullOrEmpty()) {
                when (val result = userRepository.getUser(userId)) {
                    is Result.Success -> _usuario.value = result.data
                    is Result.Failure -> {
                        // Error
                    }
                    else->{}
                }
            }
        }
    }

    fun getTicketsClient(){
        viewModelScope.launch {
            when(val result = ticketManager.getMyTickets()){
                is Result.Success -> _tickets.value = result.data
                is Result.Failure -> _obtenerTicketsClienteResult.value = ResultState.Error("Error al cargar tickets: ${result.error.message}")
                else -> {}
            }
        }
    }
}
