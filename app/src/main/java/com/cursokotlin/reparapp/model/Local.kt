package com.cursokotlin.reparapp.model

import android.os.Parcelable
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Parcelize
data class Local(
    @JvmField
    @PropertyName("id")
    var id: String? = null,

    @JvmField @PropertyName("direccion")
    var direccion:String = "",

    @JvmField @PropertyName("nombre")
    var nombre:String = "",

    @JvmField @PropertyName("foto_principal")
    var foto_p:String = "",

    @JvmField @PropertyName("foto_referencia_1")
    var foto_r_1:String = "",

    @JvmField @PropertyName("foto_referencia_2")
    var foto_r_2:String = "",

    @JvmField @PropertyName("horario_atencion")
    var horario_atencion:String = "",

    @JvmField @PropertyName("telefono")
    var telefono:String = "",

    @JvmField
    @PropertyName("latitud")
    var latitud: Double = 0.0,

    @JvmField
    @PropertyName("longitud")
    var longitud: Double = 0.0,


    @ServerTimestamp
    @JvmField @PropertyName("fecha_creacion")
    var createAt: Date? = null
): Parcelable {
    constructor() : this(null, "","","", "","","","",0.0,0.0,null)
}
