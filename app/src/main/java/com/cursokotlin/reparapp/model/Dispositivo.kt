package com.cursokotlin.reparapp.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Dispositivo(
    @JvmField @PropertyName("nombre_modelo")
    var nombre: String = "",

    @JvmField @PropertyName("tipo_dispositivo")
    var tipo: String = "",

    @JvmField @PropertyName("usuarioId")
    var usuario: String = "",

    @JvmField @PropertyName("rutaImg")
    var ruta: String = "",

    @JvmField @PropertyName("numero_serie")
    var serie: String = "",

    @JvmField @PropertyName("modelo")
    var modelo: String = "",

    @JvmField @PropertyName("marca")
    var marca: String = "",

    @ServerTimestamp
    @JvmField @PropertyName("create_at")
    var createAt: Date? = null
) {
    constructor() : this("", "", "","","", "", "",null)
}

