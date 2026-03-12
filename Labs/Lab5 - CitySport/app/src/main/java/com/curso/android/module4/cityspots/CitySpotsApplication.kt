package com.curso.android.module4.cityspots

import android.app.Application
import com.curso.android.module4.cityspots.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class CitySpotsApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        instance = this

        startKoin {
            androidLogger(Level.DEBUG)

            androidContext(this@CitySpotsApplication)

            modules(appModule)
        }
    }

    companion object {
        lateinit var instance: CitySpotsApplication
            private set
    }
}
