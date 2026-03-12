package com.curso.android.module4.cityspots

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.curso.android.module4.cityspots.ui.components.RequirePermissions
import com.curso.android.module4.cityspots.ui.screens.CameraScreen
import com.curso.android.module4.cityspots.ui.screens.MapScreen
import com.curso.android.module4.cityspots.ui.theme.CitySpotsTheme
import com.curso.android.module4.cityspots.ui.viewmodel.MapViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            CitySpotsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RequirePermissions {
                        CitySpotsNavigation()
                    }
                }
            }
        }
    }
}

object NavRoutes {
    const val MAP = "map"
    const val CAMERA = "camera"
}

@Composable
fun CitySpotsNavigation() {
    val navController = rememberNavController()

    val sharedViewModel: MapViewModel = koinViewModel()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.MAP
    ) {
        composable(NavRoutes.MAP) {
            MapScreen(
                onNavigateToCamera = {
                    navController.navigate(NavRoutes.CAMERA)
                },
                viewModel = sharedViewModel
            )
        }

        composable(NavRoutes.CAMERA) {
            CameraScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = sharedViewModel
            )
        }
    }
}
