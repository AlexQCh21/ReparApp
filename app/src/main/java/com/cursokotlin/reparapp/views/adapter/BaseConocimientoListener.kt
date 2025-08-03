package com.cursokotlin.reparapp.views.adapter

import com.cursokotlin.reparapp.model.BaseConocimiento
import com.cursokotlin.reparapp.model.Ticket

interface BaseConocimientoListener {

    fun onBaseConocimientoClicked(baseConocimiento: BaseConocimiento, position: Int)

}