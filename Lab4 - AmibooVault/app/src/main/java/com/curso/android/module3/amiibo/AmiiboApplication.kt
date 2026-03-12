package com.curso.android.module3.amiibo

import android.app.Application
import com.curso.android.module3.amiibo.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class AmiiboApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inicializar Koin (Dependency Injection)
        initializeKoin()
    }

    private fun initializeKoin() {
        startKoin {

            androidLogger(Level.DEBUG)

            androidContext(this@AmiiboApplication)

            modules(appModule)
        }
    }
}