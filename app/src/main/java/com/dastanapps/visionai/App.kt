package com.dastanapps.visionai

import android.app.Application

/**
 *
 * Created by Iqbal Ahmed on 18/06/2024
 *
 */

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }

    companion object {
        lateinit var INSTANCE: App
    }
}