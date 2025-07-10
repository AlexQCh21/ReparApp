package com.cursokotlin.reparapp.views.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Usamos @JvmField para que Firestore pueda acceder directamente a las propiedades
data class Usuario(
    var id: String? = null, // Corresponderá al UID de Firebase Auth

    @JvmField @PropertyName("nombre_apellido")
    var nombreApellido: String = "",

    @JvmField @PropertyName("email")
    var email: String = "",

    @JvmField @PropertyName("telefono")
    var telefono: String = "",

    @JvmField @PropertyName("rol")
    var rol: String = "cliente",

    @JvmField @PropertyName("is_active")
    var isActive: Boolean = true,

    @ServerTimestamp
    @JvmField @PropertyName("create_at")
    var createAt: Date? = null,

    // Campos que pueden ser nulos o no existir al principio
    @JvmField @PropertyName("dni")
    var dni: String? = null,

    @JvmField @PropertyName("direccion")
    var direccion: String? = null,

    // Campos específicos para el rol "tecnico"
    @JvmField @PropertyName("especialidad")
    var especialidad: String? = null,

    @JvmField @PropertyName("dias_laborables")
    var diasLaborables: String? = null,

    @JvmField @PropertyName("break")
    var breakTime: String? = null
) {
    // Constructor sin argumentos requerido por Firestore para la deserialización
    constructor() : this(null, "", "", "", "cliente", true, null, null, null, null, null, null)
}