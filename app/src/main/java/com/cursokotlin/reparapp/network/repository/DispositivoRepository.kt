package com.cursokotlin.reparapp.network.repository

import com.cursokotlin.reparapp.model.Result
import com.cursokotlin.reparapp.model.Dispositivo

interface DispositivoRepository {
    suspend fun saveDevice(dispositivo: Dispositivo): Result<Unit>
    suspend fun getDispositivo(dispositivoId: String): Result<Dispositivo>
    suspend fun changeDispositivo(dispositivoId: String): Result<Dispositivo>
    suspend fun getAllDispositivo(): Result<List<Dispositivo>>
    suspend fun getAllDispositivosLibres(): Result<List<Dispositivo>>
}