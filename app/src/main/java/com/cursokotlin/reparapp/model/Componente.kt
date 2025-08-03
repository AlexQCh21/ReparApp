package com.cursokotlin.reparapp.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Componente(
    @get:Exclude
    var id: String = "",

    @JvmField @PropertyName("nombre")
    var nombre: String = "",

    @JvmField @PropertyName("cantidad")
    var cantidad: Int = 0,

    @JvmField @PropertyName("ruta_foto")
    var url_foto: String = "",

    @ServerTimestamp
    @JvmField @PropertyName("create_at")
    var createAt: Date? = null
) {
    constructor() : this("","",0,"",null)
}
