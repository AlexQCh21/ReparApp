package com.cursokotlin.reparapp.network.services

import com.cursokotlin.reparapp.model.ApiResponse
import com.cursokotlin.reparapp.model.DniRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface DniApiService {
    @POST("query")
    suspend fun getPersonaSuspend(@Body body: DniRequest): Response<ApiResponse>
}
