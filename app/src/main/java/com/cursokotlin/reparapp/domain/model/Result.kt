package com.cursokotlin.reparapp.domain.model

// Result.kt
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val error: Throwable) : Result<Nothing>()

    //data class Failure(val error: AuthError) : Result<Nothing>()
}

// Función map para transformar el valor de éxito
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Failure -> this
        is Failure -> TODO()
    }
}

// Función flatMap para encadenar operaciones
inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> {
    return when (this) {
        is Result.Success -> transform(data)
        is Result.Failure -> this
        is Failure -> TODO()
    }
}

// Función adicional útil
fun <T> Result<T>.getOrElse(defaultValue: T): T {
    return when (this) {
        is Result.Success -> data
        is Result.Failure -> defaultValue
        is Failure -> TODO()
    }
}