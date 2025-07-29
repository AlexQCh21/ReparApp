package com.cursokotlin.reparapp.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class EstadoTicket(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String? = null,

    @get:PropertyName("estado") @set:PropertyName("estado")
    var estado: String = "",

    @get:PropertyName("automatico") @set:PropertyName("automatico")
    var automatico: Boolean = true,

    @get:PropertyName("comentario") @set:PropertyName("comentario")
    var comentario: String = "",

    @ServerTimestamp
    @get:PropertyName("fecha_creacion") @set:PropertyName("fecha_creacion")
    var createAt: Date? = null
) {
    constructor() : this(null, "", true, "", null)
}
