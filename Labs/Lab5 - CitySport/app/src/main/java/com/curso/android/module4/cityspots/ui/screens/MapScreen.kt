package com.curso.android.module4.cityspots.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.imageLoader
import coil.request.ImageRequest
import com.curso.android.module4.cityspots.data.entity.SpotEntity
import com.curso.android.module4.cityspots.ui.viewmodel.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import org.koin.androidx.compose.koinViewModel

@Composable
fun MapScreen(
    onNavigateToCamera: () -> Unit,
    viewModel: MapViewModel = koinViewModel()
) {
    val spots by viewModel.spots.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val spotPendingDeletion by viewModel.spotPendingDeletion.collectAsState() // NUEVO

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(spots) {
        spots.forEach { spot ->
            val request = ImageRequest.Builder(context)
                .data(spot.imageUri.toUri())
                .build()
            context.imageLoader.enqueue(request)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadUserLocation()
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(14.6349, -90.5069),
            12f
        )
    }

    LaunchedEffect(userLocation) {
        userLocation?.let { location ->
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(location, 15f)
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCamera,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar Spot"
                )
            }
        }
    ) { paddingValues ->
        var selectedSpot by remember { mutableStateOf<SpotEntity?>(null) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SpotMap(
                spots = spots,
                userLocation = userLocation,
                cameraPositionState = cameraPositionState,
                onSpotClick = { spot -> selectedSpot = spot },
                onSpotLongClick = { spot -> viewModel.requestDeleteSpot(spot) }, // NUEVO
                onMapClick = { selectedSpot = null }
            )

            selectedSpot?.let { spot ->
                SpotInfoCard(
                    spot = spot,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        // NUEVO: Diálogo de confirmación de borrado
        spotPendingDeletion?.let { spot ->
            AlertDialog(
                onDismissRequest = { viewModel.cancelDeleteSpot() },
                title = { Text("Eliminar Spot") },
                text = {
                    Text("¿Estás seguro que deseas eliminar \"${spot.title}\"? Esta acción no se puede deshacer.")
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.confirmDeleteSpot() }) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelDeleteSpot() }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
private fun SpotMap(
    spots: List<SpotEntity>,
    userLocation: LatLng?,
    cameraPositionState: CameraPositionState,
    onSpotClick: (SpotEntity) -> Unit,
    onSpotLongClick: (SpotEntity) -> Unit, // NUEVO
    onMapClick: () -> Unit
) {
    val mapProperties = remember {
        MapProperties(
            isMyLocationEnabled = true,
            isBuildingEnabled = true
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = true,
            compassEnabled = true
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings,
        onMapClick = { onMapClick() }
    ) {
        spots.forEach { spot ->
            val markerState = rememberMarkerState(
                key = spot.id.toString(),
                position = LatLng(spot.latitude, spot.longitude)
            )

            Marker(
                state = markerState,
                title = spot.title,
                onClick = {
                    onSpotClick(spot)
                    true
                },
                onInfoWindowLongClick = { // NUEVO
                    onSpotLongClick(spot)
                }
            )
        }
    }
}

@Composable
private fun SpotInfoCard(
    spot: SpotEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SubcomposeAsyncImage(
                model = spot.imageUri.toUri(),
                contentDescription = spot.title,
                modifier = Modifier
                    .size(280.dp, 160.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 2.dp
                        )
                    }
                },
                success = {
                    SubcomposeAsyncImageContent()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = spot.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "📍 ${String.format("%.4f", spot.latitude)}, ${String.format("%.4f", spot.longitude)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
