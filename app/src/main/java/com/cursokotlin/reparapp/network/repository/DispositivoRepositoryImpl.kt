package com.cursokotlin.reparapp.network.repository

import com.cursokotlin.reparapp.domain.model.Result
import com.cursokotlin.reparapp.domain.repository.DispositivoRepository
import com.cursokotlin.reparapp.model.Dispositivo
import com.cursokotlin.reparapp.network.services.DispositivoManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DispositivoRepositoryImpl @Inject constructor(
    private val dispositivoManager: DispositivoManager
):DispositivoRepository {
    override suspend fun saveDevice(dispositivo: Dispositivo): Result<Unit> {
        return dispositivoManager.saveDispositivo(dispositivo)
    }

    override suspend fun getDispositivo(dispositivoId: String): Result<Dispositivo> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllDispositivo(): Result<List<Dispositivo>> {
        return dispositivoManager.getAll()
    }

    override suspend fun changeDispositivo(dispositivoId: String): Result<Dispositivo> {
        TODO("Not yet implemented")
    }
}