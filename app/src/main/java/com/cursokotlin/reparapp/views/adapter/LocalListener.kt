package com.cursokotlin.reparapp.views.adapter

import com.cursokotlin.reparapp.model.Local


interface LocalListener {
    fun onLocalClicked(local: Local, position: Int)
}