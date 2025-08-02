package com.cursokotlin.reparapp.model

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val data: Persona
)
