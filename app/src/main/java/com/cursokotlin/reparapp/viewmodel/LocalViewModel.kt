package com.cursokotlin.reparapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cursokotlin.reparapp.domain.model.Result
import com.cursokotlin.reparapp.model.Local
import com.cursokotlin.reparapp.network.services.LocalManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocalViewModel @Inject constructor(
    private val localManager: LocalManager
) : ViewModel(){
    private val _locales = MutableLiveData<List<Local>>()
    val locales : LiveData<List<Local>> get() = _locales

    private val _obtenerLocalesResult = MutableStateFlow<ResultState<Boolean>>(ResultState.Success(false))
    val obtenerLocalesResult: StateFlow<ResultState<Boolean>> = _obtenerLocalesResult

    fun getTicketsClient(){
        viewModelScope.launch {
            when(val result = localManager.getAllLocales()){
                is Result.Success -> _locales.value = result.data
                is Result.Failure -> _obtenerLocalesResult.value = ResultState.Error("Error al cargar locales: ${result.error.message}")
                else -> {}
            }
        }
    }

}