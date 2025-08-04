package com.cursokotlin.reparapp.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cursokotlin.reparapp.model.Dispositivo
import com.cursokotlin.reparapp.network.repository.AuthRepositoryImpl
import com.cursokotlin.reparapp.network.repository.DispositivoRepositoryImpl
import com.cursokotlin.reparapp.network.storage.StorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import com.cursokotlin.reparapp.model.Result
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DeviceDetailViewModel @Inject constructor(
    private val authRepository: AuthRepositoryImpl,
    private val dispositivoRepository: DispositivoRepositoryImpl,
    private val storageManager: StorageManager
) : ViewModel() {

    private val _addDispositivoResult = MutableLiveData<ResultState<Boolean>>()
    val addDispositivoResult: LiveData<ResultState<Boolean>> = _addDispositivoResult

    /**
     * Guarda un dispositivo en Firestore.
     * Primero sube la imagen a Firebase Storage,
     * luego guarda la URL en el objeto `Dispositivo` y finalmente lo guarda.
     */
    fun saveDispositivo(dispositivo: Dispositivo, imageUri: Uri) {
        viewModelScope.launch {
            _addDispositivoResult.postValue(ResultState.Loading)

            try {
                val dispositivoId = UUID.randomUUID().toString()
                val imageUrl = storageManager.uploadDispositivoImage(imageUri, dispositivoId)

                if (imageUrl != null) {
                    val dispositivoConImagen = dispositivo.copy(
                        ruta = imageUrl,
                        usuario = authRepository.getCurrentUser()?.uid ?: ""
                    )

                    val result = dispositivoRepository.saveDevice(dispositivoConImagen)

                    when (result) {
                        is Result.Success -> {
                            _addDispositivoResult.postValue(ResultState.Success(true))
                            Log.d("DeviceDetailViewModel", "Dispositivo guardado exitosamente")
                        }
                        is Result.Failure -> {
                            _addDispositivoResult.postValue(ResultState.Error("Error al guardar: ${result.error}"))
                            Log.e("DeviceDetailViewModel", "Error al guardar dispositivo: ${result.error}")
                        }

                        else -> {}
                    }
                } else {
                    _addDispositivoResult.postValue(ResultState.Error("No se pudo subir la imagen"))
                    Log.e("DeviceDetailViewModel", "No se pudo subir la imagen")
                }
            } catch (e: Exception) {
                _addDispositivoResult.postValue(ResultState.Error("Error inesperado: ${e.message}"))
                Log.e("DeviceDetailViewModel", "Error inesperado al guardar dispositivo", e)
            }
        }
    }
}

sealed class ResultState<out T> {
    object Loading : ResultState<Nothing>()
    data class Success<T>(val data: T) : ResultState<T>()
    data class Error(val message: String) : ResultState<Nothing>()
}
