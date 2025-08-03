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
import java.util.Date
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

    // Flow para el resultado de cambio de estado
    private val _cambiarEstadoResult = MutableStateFlow<ResultState<Unit>>(ResultState.Success(Unit))
    val cambiarEstadoResult: StateFlow<ResultState<Unit>> get() = _cambiarEstadoResult

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

    fun cambiarEstadoTicket(ticketId: String, comentario: String = "") {
        viewModelScope.launch {
            _cambiarEstadoResult.value = ResultState.Loading
            when(val result = ticketManager.avanzarEstadoTicket(ticketId)) {
                is Result.Success -> _cambiarEstadoResult.value = ResultState.Success(Unit)
                is Result.Failure -> _cambiarEstadoResult.value =
                    ResultState.Error("Error al cambiar estado: ${result.error.message}")
                else -> {}
            }
        }
    }

    private val _ticket = MutableLiveData<Ticket>()
    val ticket: LiveData<Ticket> get() = _ticket

    fun getTicketById(ticketId: String) {
        viewModelScope.launch {
            when(val result = ticketManager.getTicketById(ticketId)) {
                is Result.Success -> _ticket.value = result.data
                is Result.Failure -> { /* Muestra error si quieres */ }
                else -> {}
            }
        }
    }

    // --- NUEVO: Estado de si se puede revertir ---
    private val _puedeRevertir = MutableLiveData<Boolean>()
    val puedeRevertir: LiveData<Boolean> get() = _puedeRevertir

    fun checarSiPuedeRevertir(ticketId: String) {
        viewModelScope.launch {
            val estados = ticketManager.obtenerEstados(ticketId)
            if (estados is Result.Success && estados.data.size >= 2) {
                val ultimoEstado = estados.data.last()
                val ahora = Date()
                val puedeDeshacer = ultimoEstado.fecha_creacion?.let {
                    ahora.time - it.time < 60 * 60 * 1000
                } ?: false
                _puedeRevertir.postValue(puedeDeshacer)
            } else {
                _puedeRevertir.postValue(false)
            }
        }
    }

    // --- FIN NUEVO ---

    //Revertir estado
    private val _revertirEstadoResult = MutableStateFlow<ResultState<Unit>>(ResultState.Loading)
    val revertirEstadoResult: StateFlow<ResultState<Unit>> get() = _revertirEstadoResult

    fun revertirEstadoTicket(ticketId: String) {
        viewModelScope.launch {
            _revertirEstadoResult.value = ResultState.Loading
            when(val result = ticketManager.revertirEstadoTicket(ticketId)) {
                is Result.Success -> _revertirEstadoResult.value = ResultState.Success(Unit)
                is Result.Failure -> _revertirEstadoResult.value = ResultState.Error(result.error.message.toString())
                else -> {}
            }
        }
    }

    fun setRevertirEstadoIdle() {
        _revertirEstadoResult.value = ResultState.Loading
    }



}