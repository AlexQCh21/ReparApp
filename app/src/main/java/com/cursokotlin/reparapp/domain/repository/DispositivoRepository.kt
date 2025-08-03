package com.cursokotlin.reparapp.domain.repository

import com.cursokotlin.reparapp.domain.model.Result
import com.cursokotlin.reparapp.model.Dispositivo
import com.cursokotlin.reparapp.model.Ticket
import com.cursokotlin.reparapp.model.Usuario

interface DispositivoRepository {
    suspend fun saveDevice(dispositivo: Dispositivo): Result<Unit>
    suspend fun getDispositivo(dispositivoId: String): Result<Dispositivo>
    suspend fun changeDispositivo(dispositivoId: String): Result<Dispositivo>
    suspend fun getAllDispositivo(): Result<List<Dispositivo>>
    suspend fun getAllDispositivosLibres(): Result<List<Dispositivo>>
}