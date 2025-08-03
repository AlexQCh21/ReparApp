package com.cursokotlin.reparapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cursokotlin.reparapp.domain.model.Result
import com.cursokotlin.reparapp.model.BaseConocimiento
import com.cursokotlin.reparapp.network.services.BaseConocimientoManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class BaseConocimientoViewModel @Inject constructor(
    private val baseConocimientoManager: BaseConocimientoManager
) : ViewModel(){
    private val _baseConocimiento = MutableLiveData<List<BaseConocimiento>>()
    val baseConocimiento : LiveData<List<BaseConocimiento>> get() = _baseConocimiento

    private val _obtenerBaseConocimientoResult = MutableStateFlow<ResultState<Boolean>>(ResultState.Success(false))
    val obtenerBaseConocmientoResult: StateFlow<ResultState<Boolean>> = _obtenerBaseConocimientoResult

    fun getBaseConocimientoClient(){
        viewModelScope.launch {
            when(val result = baseConocimientoManager.getAllBaseConocimiento()){
                is Result.Success -> _baseConocimiento.value = result.data
                is Result.Failure -> _obtenerBaseConocimientoResult.value = ResultState.Error("Error al cargar locales: ${result.error.message}")
                else -> {}
            }
        }
    }

}