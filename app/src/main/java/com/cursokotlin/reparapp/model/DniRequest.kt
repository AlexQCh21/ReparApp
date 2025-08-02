package com.cursokotlin.reparapp.model

data class DniRequest(
    val token: String,
    val type_document: String = "dni",
    val document_number: String
)
