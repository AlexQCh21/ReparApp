package com.cursokotlin.reparapp.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Ticket(
    @JvmField
    @PropertyName("id")
    var id: String? = null,

    @JvmField @PropertyName("dispositivoId")
    var dispositivoId: String = "",

    @JvmField @PropertyName("rutaImg")
    var ruta: String = "",

    @JvmField @PropertyName("descripcion")
    var descripcion: String = "",

    @JvmField @PropertyName("usuario_id")
    var cliente: String = "",

    @JvmField @PropertyName("tecnico_id")
    var tecnico: String = "",

    @JvmField @PropertyName("feedback")
    var retroalimentacion: String = "",

    @ServerTimestamp
    @JvmField @PropertyName("fecha_creacion")
    var createAt: Date? = null
) {
    constructor() : this("", "","", "","","", "",null)
}


