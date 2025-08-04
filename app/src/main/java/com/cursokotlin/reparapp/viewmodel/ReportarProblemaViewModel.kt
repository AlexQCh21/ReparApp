package com.cursokotlin.reparapp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cursokotlin.reparapp.model.Dispositivo
import com.cursokotlin.reparapp.model.EstadoTicket
import com.cursokotlin.reparapp.network.repository.AuthRepositoryImpl
import com.cursokotlin.reparapp.network.repository.DispositivoRepositoryImpl
import com.cursokotlin.reparapp.network.repository.TicketRepositoryImpl
import com.cursokotlin.reparapp.model.Result
import com.cursokotlin.reparapp.model.Estado
import com.cursokotlin.reparapp.model.Ticket
import com.cursokotlin.reparapp.network.storage.TicketStorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ReportarProblemaViewModel @Inject constructor(
    private val authRepository: AuthRepositoryImpl,
    private val ticketRepositoryImpl: TicketRepositoryImpl,
    private val dispositivoRepositoryImpl: DispositivoRepositoryImpl,
    private val ticketStorageManager: TicketStorageManager
) : ViewModel() {

    private val _dispositivos = MutableStateFlow<List<Dispositivo>>(emptyList())
    val dispositivos: StateFlow<List<Dispositivo>> = _dispositivos

    private val _crearTicketResult = MutableStateFlow<ResultState<Boolean>>(ResultState.Success(false))
    val crearTicketResult: StateFlow<ResultState<Boolean>> = _crearTicketResult

    private val _nuevoTicketCreado = MutableStateFlow<Ticket?>(null)
    val nuevoTicketCreado: StateFlow<Ticket?> = _nuevoTicketCreado


    private var imagenSeleccionada: Uri? = null

    init {
        obtenerDispositivos()
    }

    fun setImagenSeleccionada(uri: Uri) {
        imagenSeleccionada = uri
    }

    private fun obtenerDispositivos() {
        viewModelScope.launch {
            when (val result = dispositivoRepositoryImpl.getAllDispositivosLibres()) {
                is Result.Success -> _dispositivos.value = result.data
                is Result.Failure -> _crearTicketResult.value = ResultState.Error("Error al cargar dispositivos: ${result.error.message}")
                else -> {}
            }
        }
    }

    fun crearTicket(descripcion: String, dispositivo: Dispositivo?) {
        if (descripcion.isBlank() || dispositivo == null) {
            _crearTicketResult.value = ResultState.Error("Completa todos los campos")
            return
        }

        viewModelScope.launch {
            _crearTicketResult.value = ResultState.Loading

            try {
                val usuario = authRepository.getCurrentUser()
                if (usuario == null) {
                    _crearTicketResult.value = ResultState.Error("Usuario no autenticado")
                    return@launch
                }

                // No generes el ID aquí, deja que el manager lo asigne
                var nuevoTicket = Ticket(
                    id = null, // ID lo asigna Firestore
                    cliente = usuario.uid,
                    dispositivoId = dispositivo.id,
                    descripcion = descripcion,
                    ruta = ""
                )

                // Subes la imagen usando un ID temporal
                imagenSeleccionada?.let { uri ->
                    val imageId = UUID.randomUUID().toString()
                    val url = ticketStorageManager.uploadDispositivoImage(uri, imageId)
                    if (url != null) {
                        nuevoTicket = nuevoTicket.copy(ruta = url)
                    }
                }

                // Guarda el ticket y obtén el ticket con ID asignado por Firestore
                val resultado = ticketRepositoryImpl.saveTicket(nuevoTicket)

                if (resultado is Result.Success) {
                    // Aquí obtienes el ticket guardado con su ID
                    val ticketGuardado = resultado.data // <-- asegúrate que saveTicket retorna Result<Ticket>

                    val estadoInicial = EstadoTicket(
                        id = null, // Firestore lo asigna
                        estado = Estado.ASIGNADO.toString(),
                        automatico = true,
                        comentario = "Ticket creado por el usuario"
                    )

                    // Usa el id real del ticket
                    ticketRepositoryImpl.agregarEstado(ticketGuardado.id!!, estadoInicial)

                    _crearTicketResult.value = ResultState.Success(true)

                    _nuevoTicketCreado.value = ticketGuardado
                } else if (resultado is Result.Failure) {
                    _crearTicketResult.value = ResultState.Error("Error al guardar ticket: ${resultado.error.message}")
                }
            } catch (e: Exception) {
                _crearTicketResult.value = ResultState.Error("Error inesperado: ${e.message}")
            }
        }
    }
}
