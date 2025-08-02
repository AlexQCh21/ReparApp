package com.cursokotlin.reparapp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cursokotlin.reparapp.R
import com.cursokotlin.reparapp.model.Persona
import com.cursokotlin.reparapp.network.repository.AuthRepositoryImpl
import com.cursokotlin.reparapp.network.repository.DispositivoRepositoryImpl
import com.cursokotlin.reparapp.network.repository.DniRepository
import com.cursokotlin.reparapp.network.repository.TicketRepositoryImpl
import com.cursokotlin.reparapp.network.storage.TicketStorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import com.cursokotlin.reparapp.domain.model.Result
import javax.inject.Inject
import android.net.Uri
import com.cursokotlin.reparapp.domain.model.AuthError
import com.cursokotlin.reparapp.model.Dispositivo
import com.cursokotlin.reparapp.model.Estado
import com.cursokotlin.reparapp.model.Ticket
import com.cursokotlin.reparapp.model.Usuario
import com.cursokotlin.reparapp.network.FirestoreManager
import java.util.UUID

@HiltViewModel
class ReportarProblemaTecnicoViewModel @Inject constructor(
    private val authRepository: AuthRepositoryImpl,
    private val firestoreManager: FirestoreManager,
    private val ticketRepositoryImpl: TicketRepositoryImpl,
    private val dispositivoRepositoryImpl: DispositivoRepositoryImpl,
    private val ticketStorageManager: TicketStorageManager,
    private val dniRepository: DniRepository,
    @ApplicationContext private val context: Context // para acceder a strings
) : ViewModel() {

    private val _persona = MutableLiveData<Persona?>()
    val persona: LiveData<Persona?> = _persona

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _ticketCreadoExitoso = MutableLiveData<Boolean>()
    val ticketCreadoExitoso: LiveData<Boolean> get() = _ticketCreadoExitoso



    fun buscarPersonaPorDni(dni: String) {
        viewModelScope.launch {
            _loading.value = true
            val token = context.getString(R.string.token_dni)
            val resultado = dniRepository.obtenerPersonaPorDni(dni, token)
            _persona.value = resultado
            _loading.value = false
            if (resultado == null) {
                _error.value = "No se encontró persona para el DNI ingresado"
            } else {
                _error.value = null
            }
        }
    }


    fun resetExito() {
        _ticketCreadoExitoso.value = false
    }

    fun reportarProblema(
        dni: String,
        nombre: String,
        apellido: String,
        telefono: String,
        direccion: String,
        nombreDispositivo: String,
        tipo: String,
        marca: String,
        modelo: String,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            _ticketCreadoExitoso.value = false

            // 1. Buscar usuario por DNI
            val resultadoUser = firestoreManager.getUserByDni(dni)
            val userId: String
            val usuario: Usuario

            if (resultadoUser is Result.Success) {
                userId = resultadoUser.data.id
                usuario = resultadoUser.data
            } else if (resultadoUser is Result.Failure && resultadoUser.error == AuthError.UserNotFound) {
                userId = UUID.randomUUID().toString()
                usuario = Usuario(
                    id = userId,
                    nombreUsuario = dni,
                    nombre = nombre,
                    apellido = apellido,
                    email = "",
                    telefono = telefono,
                    rol = "cliente",
                    isActive = true,
                    createAt = null,
                    dni = dni,
                    direccion = direccion
                )
                val saveUserResult = firestoreManager.saveUser(usuario)
                if (saveUserResult is Result.Failure) {
                    _error.value = "No se pudo crear el usuario"
                    _loading.value = false
                    return@launch
                }
            } else {
                _error.value = "Error desconocido al buscar usuario"
                _loading.value = false
                return@launch
            }

            // 2. Crear dispositivo
            val dispositivo = Dispositivo(
                nombre = nombreDispositivo,
                tipo = tipo,
                usuario = userId,
                ruta = "",
                modelo = modelo,
                marca = marca
            )
            val dispositivoRes = dispositivoRepositoryImpl.saveDevice(dispositivo)
            val dispositivoId = if (dispositivoRes is Result.Success) dispositivo.id else ""
            if (dispositivoRes is Result.Failure) {
                _error.value = "No se pudo crear el dispositivo"
                _loading.value = false
                return@launch
            }

            // 3. Subir imagen si existe
            var rutaImg = ""
            imageUri?.let { uri ->
                val imageId = UUID.randomUUID().toString()
                val url = ticketStorageManager.uploadDispositivoImage(uri, imageId)
                if (url != null) {
                    rutaImg = url
                }
            }

            // 4. Crear ticket
            val ticket = Ticket(
                id = null,
                dispositivoId = dispositivoId,
                ruta = rutaImg,
                descripcion = "Problema reportado por usuario",
                cliente = userId,
                tecnico = "",
                estadoActual = Estado.ASIGNADO.toString(),
                retroalimentacion = ""
            )

            val ticketRes = ticketRepositoryImpl.saveTicket(ticket)
            if (ticketRes is Result.Success) {
                _ticketCreadoExitoso.value = true
            } else {
                _error.value = "Error al generar el ticket"
            }
            _loading.value = false
        }
    }

//    fun reportarProblema(
//        dni: String,
//        nombre: String,
//        apellido: String,
//        telefono: String,
//        direccion: String,
//        nombreDispositivo: String,
//        tipo: String,
//        marca: String,
//        modelo: String,
//        imageUri: Uri?
//    ) {
//        viewModelScope.launch {
//            _loading.value = true
//
//            // 1. Crea el usuario
//            val userId = UUID.randomUUID().toString()
//            val usuario = Usuario(
//                id = userId,
//                nombreUsuario = dni,
//                nombre = nombre,
//                apellido = apellido,
//                email = "",
//                telefono = telefono,
//                rol = "cliente",
//                isActive = true,
//                createAt = null,
//                dni = dni,
//                direccion = direccion
//            )
//            firestoreManager.saveUser(usuario)
//
//            // 2. Crear dispositivo (sin imagen)
//            val dispositivo = Dispositivo(
//                nombre = nombreDispositivo,
//                tipo = tipo,
//                usuario = userId,
//                ruta = "",
//                modelo = modelo,
//                marca = marca
//            )
//            val dispositivoRes = dispositivoRepositoryImpl.saveDevice(dispositivo)
//            val dispositivoId = if (dispositivoRes is Result.Success) dispositivo.id else ""
//
//            // 3. Subes la imagen usando un ID temporal (UUID) y usas la URL para el ticket
//            var rutaImg = ""
//            imageUri?.let { uri ->
//                val imageId = UUID.randomUUID().toString()
//                val url = ticketStorageManager.uploadDispositivoImage(uri, imageId)
//                if (url != null) {
//                    rutaImg = url
//                }
//            }
//
//            // 4. Crear ticket con la URL generada
//            val ticket = Ticket(
//                id = null, // Firestore lo asigna
//                dispositivoId = dispositivoId,
//                ruta = rutaImg,
//                descripcion = "Problema reportado por usuario",
//                cliente = userId,
//                tecnico = "",
//                estadoActual = Estado.ASIGNADO.toString(),
//                retroalimentacion = ""
//            )
//
//            val ticketRes = ticketRepositoryImpl.saveTicket(ticket)
//
//            _loading.value = false
//
//            _error.value = when {
//                ticketRes is Result.Success -> null
//                else -> "Error al generar el ticket"
//            }
//        }
//    }

}
