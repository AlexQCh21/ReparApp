package com.cursokotlin.reparapp.model

import com.cursokotlin.reparapp.model.Dispositivo
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import java.util.Date

data class BaseConocimiento(

    @get:Exclude
    var id: String = "",

    @JvmField @PropertyName("descripcion")
    var descripcion: String = "",

    @JvmField @PropertyName("dispositivo_tipo")
    var dispositivo_tipo: String = "",

    @JvmField @PropertyName("fecha_publicacion")
    var fecha_publicacion: Date? = null,

    @JvmField @PropertyName("tecnico_id")
    var tecnico_id: String = "",

    @JvmField @PropertyName("titulo")
    var titulo: String = "",

    @JvmField @PropertyName("imagen")
    var imagen: String = "",

){
    constructor() : this("","", "",null,"", "", "")
}
