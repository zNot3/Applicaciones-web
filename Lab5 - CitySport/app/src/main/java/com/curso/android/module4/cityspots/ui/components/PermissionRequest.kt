package com.curso.android.module4.cityspots.ui.components

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

val requiredPermissions = listOf(
    Manifest.permission.CAMERA,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequirePermissions(
    content: @Composable () -> Unit
) {
    val permissionsState = rememberMultiplePermissionsState(
        permissions = requiredPermissions
    )

    if (permissionsState.allPermissionsGranted) {
        content()
    } else {
        PermissionRequestScreen(permissionsState = permissionsState)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequestScreen(
    permissionsState: MultiplePermissionsState
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (permissionsState.shouldShowRationale) {
                Icons.Default.Warning
            } else {
                Icons.Default.CameraAlt
            },
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Permisos Requeridos",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        val description = if (permissionsState.shouldShowRationale) {
            "Has denegado los permisos anteriormente. " +
                    "City Spots necesita acceso a la cámara para capturar fotos " +
                    "y a la ubicación para guardar las coordenadas de tus spots."
        } else {
            "Para usar City Spots necesitamos acceso a tu cámara " +
                    "y ubicación. Esto nos permite:\n\n" +
                    "• Capturar fotos de los lugares que visites\n" +
                    "• Guardar las coordenadas GPS de cada spot\n" +
                    "• Mostrar tus spots en el mapa"
        }

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        PermissionsList(permissionsState)

        Spacer(modifier = Modifier.height(32.dp))

        if (permissionsState.shouldShowRationale) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = {
                        permissionsState.launchMultiplePermissionRequest()
                    }
                ) {
                    Text("Otorgar Permisos")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Abrir Configuración")
                }
            }
        } else {
            Button(
                onClick = {
                    permissionsState.launchMultiplePermissionRequest()
                }
            ) {
                Text("Otorgar Permisos")
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionsList(permissionsState: MultiplePermissionsState) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        permissionsState.revokedPermissions.forEach { permission ->
            val (icon, label) = when (permission.permission) {
                Manifest.permission.CAMERA -> Icons.Default.CameraAlt to "Cámara"
                Manifest.permission.ACCESS_FINE_LOCATION -> Icons.Default.LocationOn to "Ubicación Precisa"
                Manifest.permission.ACCESS_COARSE_LOCATION -> Icons.Default.LocationOn to "Ubicación Aproximada"
                else -> Icons.Default.Warning to permission.permission
            }

            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
