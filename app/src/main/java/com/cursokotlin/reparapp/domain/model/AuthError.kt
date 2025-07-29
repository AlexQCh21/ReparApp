package com.cursokotlin.reparapp.domain.model

sealed class AuthError(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    object UserNotLoggedIn : AuthError("Usuario no autenticado")
    object InvalidEmail : AuthError("Email no válido")
    object WeakPassword : AuthError("Contraseña débil")
    object UserCollision : AuthError("Usuario ya existe")
    object UserNotFound : AuthError("Usuario no encontrado")
    object InvalidUserData : AuthError("Datos de usuario no válidos")
    object EmailAlreadyVerified : AuthError("El correo ya está verificado")
    object WrongPassword : AuthError()
    data class DatabaseError(override val cause: Exception) : AuthError("Error de base de datos", cause)
    data class UnknownError(override val message: String?) : AuthError(message)
    object Unauthorized : AuthError()
}

// Actualiza tu Result.Failure para usarlo:
data class Failure(val error: AuthError) : Result<Nothing>()  // En Result.kt