package com.cursokotlin.reparapp.views.utils

import android.util.Log
import com.cursokotlin.reparapp.views.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreManager {

    private val db = FirebaseFirestore.getInstance()

    companion object {
        const val TAG = "FirestoreManager"
        const val COLLECTION_USERS = "usuarios"
    }

    /**
     * Crea o actualiza un documento de usuario en Firestore.
     * Usamos el UID de Firebase Auth como ID del documento.
     * @param user El objeto Usuario con los datos a guardar.
     * @param onResult Callback que se invoca cuando la operación termina.
     */
    fun saveUser(user: Usuario, onResult: (Boolean, String?) -> Unit) {
        // El usuario debe tener un ID (debería ser el UID de Auth)
        if (user.id.isNullOrEmpty()) {
            onResult(false, "El ID de usuario no puede ser nulo o vacío.")
            return
        }

        db.collection(COLLECTION_USERS).document(user.id!!)
            .set(user, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Usuario guardado con éxito en Firestore con ID: ${user.id}")
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al guardar usuario en Firestore", e)
                onResult(false, e.message)
            }
    }
}