package com.cursokotlin.reparapp

import android.app.Application
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ReparApp:Application() {
    override fun onCreate() {
        super.onCreate()
    }
}