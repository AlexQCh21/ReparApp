package com.cursokotlin.reparapp.model

import java.util.Date

data class Persona(
    val number: String,
    val full_name: String,
    val name: String,
    val surname: String,
    val date_of_birth: String,
    val department: String,
    val province: String,
    val district: String,
    val address: String,
    val ubigeo: String
)

