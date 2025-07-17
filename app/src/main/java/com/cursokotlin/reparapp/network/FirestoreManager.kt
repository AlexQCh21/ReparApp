package com.cursokotlin.reparapp.network

import android.util.Log
import com.cursokotlin.reparapp.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreManager {

    private val db = FirebaseFirestore.getInstance()

    companion object {
        const val TAG = "FirestoreManager"
        const val COLLECTION_USERS = "usuarios"
    }


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