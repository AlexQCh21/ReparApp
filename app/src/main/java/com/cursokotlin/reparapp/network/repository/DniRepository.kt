package com.cursokotlin.reparapp.network.repository

import com.cursokotlin.reparapp.model.DniRequest
import com.cursokotlin.reparapp.model.Persona
import com.cursokotlin.reparapp.network.services.DniApiService
import javax.inject.Inject

class DniRepository @Inject constructor(
    private val dniApiService: DniApiService
) {
    suspend fun obtenerPersonaPorDni(dni: String, token: String): Persona? {
        val request = DniRequest(
            token = token,
            document_number = dni
        )

        return try {
            val response = dniApiService.getPersonaSuspend(request)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
