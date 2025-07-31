package com.cursokotlin.reparapp.views.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.cursokotlin.reparapp.R
import com.google.android.material.button.MaterialButton

class CustomDialogFragment : DialogFragment() {

    interface DialogListener {
        fun onConfirm()
        fun onCancel()
    }

    private var titulo: String? = null
    private var contenido: String? = null
    private var listener: DialogListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        titulo = arguments?.getString(ARG_TITULO)
        contenido = arguments?.getString(ARG_CONTENIDO)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_dialog_popup, null)

        val tvTitulo = view.findViewById<TextView>(R.id.titulo)
        val tvContenido = view.findViewById<TextView>(R.id.contenido)
        val btnCancelar = view.findViewById<MaterialButton>(R.id.btn_cancelar)
        val btnContinuar = view.findViewById<MaterialButton>(R.id.btn_continuar)

        tvTitulo.text = titulo
        tvContenido.text = contenido

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .setCancelable(false)
            .create()

        btnCancelar.setOnClickListener {
            listener?.onCancel()
            dialog.dismiss()
        }

        btnContinuar.setOnClickListener {
            listener?.onConfirm()
            dialog.dismiss()
        }

        return dialog
    }


    fun setDialogListener(listener: DialogListener) {
        this.listener = listener
    }

    companion object {
        private const val ARG_TITULO = "arg_titulo"
        private const val ARG_CONTENIDO = "arg_contenido"

        fun newInstance(titulo: String, contenido: String): CustomDialogFragment {
            val fragment = CustomDialogFragment()
            val args = Bundle().apply {
                putString(ARG_TITULO, titulo)
                putString(ARG_CONTENIDO, contenido)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
