package com.cursokotlin.reparapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cursokotlin.reparapp.model.Ticket
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedTicketViewModel @Inject constructor() : ViewModel() {
    private val _ticket = MutableLiveData<Ticket>()
    val ticket: LiveData<Ticket> = _ticket

    fun setTicket(ticket: Ticket) {
        _ticket.value = ticket
    }
}
