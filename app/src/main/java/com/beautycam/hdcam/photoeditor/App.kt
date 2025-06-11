package com.beautycam.hdcam.photoeditor

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        photoApp = this
    }

    companion object {
        var photoApp: App? = null
            private set
        private val TAG = App::class.java.simpleName
    }
}