package com.cursokotlin.reparapp.views.adapter

import com.cursokotlin.reparapp.model.Ticket

interface DeviceListener {
    fun onTicketClicked(ticket: Ticket, position: Int)
}