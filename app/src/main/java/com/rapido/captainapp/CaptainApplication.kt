package com.rapido.captainapp

import android.app.Application
import com.rapido.captainapp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CaptainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Koin
        startKoin {
            androidContext(this@CaptainApplication)
            modules(appModule)
        }
    }
}