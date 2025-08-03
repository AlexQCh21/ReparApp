package com.cursokotlin.reparapp.views.adapter

import com.cursokotlin.reparapp.model.Notificacion

interface NotificacionListener {
    fun onTicketClicked(notificacion: Notificacion, position: Int)
}