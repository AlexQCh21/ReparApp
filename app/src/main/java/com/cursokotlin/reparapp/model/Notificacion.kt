package com.cursokotlin.reparapp.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Notificacion(
    var id: String? = null,

    @JvmField @PropertyName("leido")
    var visto:Boolean = false,

    @JvmField @PropertyName("mensaje")
    var mensaje:String = "",

    @JvmField @PropertyName("receptorId")
    var receptorId:String = "",

    @JvmField @PropertyName("tipo_usuario")
    var tipoUsuario:String = "",

    @JvmField @PropertyName("asuntoId")
    var asuntoId:String = "",

    @ServerTimestamp
    @JvmField @PropertyName("fecha_creacion")
    var createAt: Date? = null
) {
    constructor() : this("", false,"","","","",null)
}
