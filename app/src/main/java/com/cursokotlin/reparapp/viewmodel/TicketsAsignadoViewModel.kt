package com.cursokotlin.reparapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cursokotlin.reparapp.domain.model.Result
import com.cursokotlin.reparapp.model.Dispositivo
import com.cursokotlin.reparapp.model.Ticket
import com.cursokotlin.reparapp.network.services.DispositivoManager
import com.cursokotlin.reparapp.network.services.TicketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TicketsAsignadoViewModel @Inject constructor(
    private val ticketManager: TicketManager,
    private val dispositivoManager: DispositivoManager
) : ViewModel()  {

    private val _tickets = MutableLiveData<List<Ticket>>()
    val tickets : LiveData<List<Ticket>> get() = _tickets

    private val _dispositivo = MutableLiveData<Dispositivo>()
    val dispositivo : LiveData<Dispositivo> get() = _dispositivo

    private val _obtenerTicketsAsignadosResult = MutableStateFlow<ResultState<Boolean>>(ResultState.Success(false))
    val obtenerTicketsAsignadosResult: StateFlow<ResultState<Boolean>> = _obtenerTicketsAsignadosResult

    fun getTicketsAsignados(){
        viewModelScope.launch {
            when(val result = ticketManager.getTicketsAsignados()){
                is Result.Success -> _tickets.value = result.data
                is Result.Failure -> _obtenerTicketsAsignadosResult.value = ResultState.Error("Error al cargar tickets asignados: ${result.error.message}")
                else -> {}
            }
        }
    }

    fun getDispositivoTicket(dispositivoId: String) {
        viewModelScope.launch {
            when(val result = dispositivoManager.getDispositivoById(dispositivoId)) {
                is Result.Success -> _dispositivo.value = result.data
                is Result.Failure -> _obtenerTicketsAsignadosResult.value =
                    ResultState.Error("Error al cargar dispositivo: ${result.error.message}")
                else -> {}
            }
        }
    }



}