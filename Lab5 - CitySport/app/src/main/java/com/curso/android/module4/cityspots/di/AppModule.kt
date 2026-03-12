package com.curso.android.module4.cityspots.di

import com.curso.android.module4.cityspots.data.db.SpotDatabase
import com.curso.android.module4.cityspots.repository.SpotRepository
import com.curso.android.module4.cityspots.ui.viewmodel.MapViewModel
import com.curso.android.module4.cityspots.utils.CameraUtils
import com.curso.android.module4.cityspots.utils.CoordinateValidator
import com.curso.android.module4.cityspots.utils.LocationUtils
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {

    // =========================================================================
    // CAPA DE DATOS - Database
    // =========================================================================

    single {
        SpotDatabase.getInstance(androidContext())
    }

    single {
        get<SpotDatabase>().spotDao()
    }

    // =========================================================================
    // CAPA DE DATOS - Utils
    // =========================================================================

    single {
        CameraUtils(androidContext())
    }

    single {
        LocationUtils(androidContext())
    }

    singleOf(::CoordinateValidator)

    // =========================================================================
    // CAPA DE REPOSITORIO
    // =========================================================================

    single {
        SpotRepository(
            spotDao = get(),
            cameraUtils = get(),
            locationUtils = get(),
            coordinateValidator = get()
        )
    }

    // =========================================================================
    // CAPA DE PRESENTACIÓN - ViewModels
    // =========================================================================

    viewModelOf(::MapViewModel)
}
