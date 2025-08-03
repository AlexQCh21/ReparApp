package com.cursokotlin.reparapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cursokotlin.reparapp.domain.model.Result
import com.cursokotlin.reparapp.model.Componente
import com.cursokotlin.reparapp.model.Local
import com.cursokotlin.reparapp.network.services.ComponenteManager
import com.cursokotlin.reparapp.network.services.LocalManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComponenteViewModel @Inject constructor(
    private val componenteManager: ComponenteManager
) : ViewModel(){
    private val _componente = MutableLiveData<List<Componente>>()
    val componente : LiveData<List<Componente>> get() = _componente

    private val _obtenerComponenteResult = MutableStateFlow<ResultState<Boolean>>(ResultState.Success(false))
    val obtenerComponenteResult: StateFlow<ResultState<Boolean>> = _obtenerComponenteResult

    fun getComponentes(){
        viewModelScope.launch {
            when(val result = componenteManager.getAll()){
                is Result.Success -> _componente.value = result.data
                is Result.Failure -> _obtenerComponenteResult.value = ResultState.Error("Error al cargar componentes: ${result.error.message}")
                else -> {}
            }
        }
    }

}