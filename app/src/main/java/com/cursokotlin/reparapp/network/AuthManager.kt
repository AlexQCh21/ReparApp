package com.cursokotlin.reparapp.network

import android.util.Log
import com.cursokotlin.reparapp.domain.model.AuthError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.cursokotlin.reparapp.domain.model.Result
import com.facebook.AccessToken
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider


@Singleton
class AuthManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestoreManager: FirestoreManager
) {
    //private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        const val TAG = "AuthManager"
    }



    // Verifica si hay un usuario logueado (Flow para observación reactiva)
    fun observeCurrentUser(): Flow<FirebaseUser?> = flow {
        while (true) {
            emit(firebaseAuth.currentUser)
            kotlinx.coroutines.delay(1000) // Emite cada segundo (ajustable)
        }
    }

    // Devuelve el usuario actual (sincrónico)
    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    // Registra un usuario con email y password (usando suspend)
    suspend fun signUpUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw AuthError.UnknownError(Exception("Usuario nulo").message)
            Result.Success(user)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            val error = when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> AuthError.InvalidEmail
                "ERROR_WEAK_PASSWORD" -> AuthError.WeakPassword
                else -> AuthError.UnknownError(e.message)
            }
            Result.Failure(error)
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.Failure(AuthError.UserCollision)
        } catch (e: Exception) {
            Result.Failure(AuthError.UnknownError(e.message))
        }
    }

    // Inicia sesión (suspend + Result)
    suspend fun signInUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                Result.Success(user)
            } else {
                // Este caso es poco probable si signInWithEmailAndPassword tiene éxito, pero es bueno por seguridad
                Result.Failure(AuthError.UnknownError("Autenticación exitosa pero el usuario es nulo."))
            }
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            val error = when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> AuthError.InvalidEmail
                "ERROR_WRONG_PASSWORD" -> AuthError.WrongPassword // Mapeo corregido
                "ERROR_USER_NOT_FOUND" -> AuthError.UserNotFound // Añadido
                else -> AuthError.UnknownError(e.message)
            }
            Result.Failure(error)
        } catch (e: FirebaseAuthException) {
            // Captura otros tipos de FirebaseAuthException si es necesario (ej. problemas de red)
            Result.Failure(AuthError.UnknownError(e.message))
        } catch (e: Exception) {
            // Captura cualquier otra excepción inesperada
            Result.Failure(AuthError.UnknownError(e.message))
        }
    }

    fun addAuthStateListener(onAuthStateChanged: (FirebaseUser?) -> Unit) {
        firebaseAuth.addAuthStateListener { auth ->
            onAuthStateChanged(auth.currentUser)
        }
    }


    // Restablecer contraseña (suspend)
    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(AuthError.DatabaseError(e))
        }
    }

    fun userValidated(user: FirebaseUser?=firebaseAuth.currentUser):Boolean{
        return user?.isEmailVerified?:false
    }

    // Verificación del email (suspend)
    suspend fun sendEmailVerification(user: FirebaseUser? = firebaseAuth.currentUser): Result<Unit> {
        return try {
            val targetUser = user ?: throw AuthError.UserNotLoggedIn
            targetUser.sendEmailVerification().await()
            Log.d(TAG, "Email de verificación enviado a ${targetUser.email}")
            Result.Success(Unit)
        } catch (e: AuthError) {
            Log.e(TAG, "Error de autenticación al enviar verificación", e)
            Result.Failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar email de verificación", e)
            Result.Failure(AuthError.DatabaseError(e))
        }
    }

    // Iniciar como anónimo (suspend)
    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.signInAnonymously().await()
            val user = authResult.user ?: throw Exception("Usuario anónimo nulo")
            Result.Success(user)
        } catch (e: Exception) {
            Result.Failure(AuthError.DatabaseError(e))
        }
    }

    // Cerrar sesión (sincrónico)
    fun logout() {
        firebaseAuth.signOut()
    }

    // Eliminar usuario actual (suspend)
    suspend fun deleteCurrentUser(): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: throw Exception("No hay usuario para borrar")
            user.delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(AuthError.DatabaseError(e))
        }
    }

    //Iniciar sesión con google
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user ?: return Result.Failure(AuthError.UnknownError("Usuario nulo"))

            val email = user.email ?: return Result.Failure(AuthError.UnknownError("No se pudo obtener el email del usuario"))

            // Verificamos los métodos de inicio de sesión asociados a ese email
            val signInMethods = firebaseAuth.fetchSignInMethodsForEmail(email).await().signInMethods

            if (signInMethods != null && signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)) {
                // Si el correo ya está registrado con email/password, NO permitir iniciar con Google
                return Result.Failure(AuthError.ExistingUserWithDifferentAuthMethod)
            }

            return Result.Success(user)

        } catch (e: Exception) {
            Log.e(TAG, "Error al iniciar sesión con Google", e)
            Result.Failure(AuthError.UnknownError(e.message))
        }
    }



//    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
//        return try {
//            val credential = GoogleAuthProvider.getCredential(idToken, null)
//            val authResult = firebaseAuth.signInWithCredential(credential).await()
//            val user = authResult.user
//            if (user != null) {
//                Result.Success(user)
//            } else {
//                Result.Failure(AuthError.UnknownError("Usuario nulo después de autenticarse con Google."))
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error al iniciar sesión con Google", e)
//            Result.Failure(AuthError.UnknownError(e.message))
//        }
//    }



    /*
     *Login con facebook
     */
//    suspend fun signInWithFacebook(token: AccessToken): Result<FirebaseUser> {
//        return try {
//            val credential = FacebookAuthProvider.getCredential(token.token)
//            val authResult = firebaseAuth.signInWithCredential(credential).await()
//            val user = authResult.user
//            if (user != null) {
//                Result.Success(user)
//            } else {
//                Result.Failure(AuthError.UnknownError("Usuario nulo después de autenticarse con Facebook."))
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error al iniciar sesión con Facebook", e)
//            Result.Failure(AuthError.UnknownError(e.message))
//        }
//    }

    suspend fun signInWithFacebook(token: AccessToken): Result<FirebaseUser> {
        return try {
            val credential = FacebookAuthProvider.getCredential(token.token)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user

            if (user != null) {
                // Comprobamos si el proveedor es Facebook
                val isFacebookUser = user.providerData.any {
                    it.providerId == FacebookAuthProvider.PROVIDER_ID
                }

                if (isFacebookUser) {
                    // Si inició con Facebook, tratamos como verificado
                    Log.d(TAG, "Usuario con Facebook, se considera verificado automáticamente: ${user.email}")
                    Result.Success(user)
                } else if (user.isEmailVerified) {
                    // Si no fue por Facebook pero el email está verificado
                    Log.d(TAG, "Usuario con email verificado: ${user.email}")
                    Result.Success(user)
                } else {
                    Log.w(TAG, "Usuario no verificado: ${user.email}")
                    Result.Failure(AuthError.EmailNotVerified)
                }

            } else {
                Result.Failure(AuthError.UnknownError("Usuario nulo después de autenticarse con Facebook."))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error al iniciar sesión con Facebook", e)
            Result.Failure(AuthError.UnknownError(e.message))
        }
    }



}