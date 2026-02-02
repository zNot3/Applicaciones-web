package com.curso.android.module1.dice

import android.os.Bundle
import android.util.Log

// --- AndroidX Activity ---
// ComponentActivity: Activity base moderna que soporta Compose y otras APIs de Jetpack
// enableEdgeToEdge: Función para habilitar UI de borde a borde (sin barras opacas)
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

// --- Jetpack Compose Core ---
// Estas son las importaciones fundamentales para construir UIs con Compose
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

// --- Material 3 Components ---
// Componentes de UI siguiendo Material Design 3
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar

// --- Compose Runtime (Estado y Efectos) ---
// Estas son las APIs para manejar estado reactivo en Compose
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

// --- Compose UI ---
// Utilidades para modificar la apariencia y comportamiento de composables
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Kotlin Coroutines ---
// Corrutinas para operaciones asíncronas (como nuestra animación del dado)
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "CharacterSheet"
private const val MIN_STAT = 3
private const val MAX_STAT = 18
private const val THRESHOLD_BAD = 30
private const val THRESHOLD_GODLY = 50

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Activity creada. Inicializando UI...")
        enableEdgeToEdge()
        Log.d(TAG, "onCreate: Edge-to-Edge habilitado")
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CharacterCreationScreen()
                }
            }
        }
        Log.d(TAG, "onCreate: UI de Compose establecida correctamente")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCreationScreen() {
    var vit by rememberSaveable { mutableIntStateOf(10) }
    var dex by rememberSaveable { mutableIntStateOf(10) }
    var wis by rememberSaveable { mutableIntStateOf(10) }

    val totalScore = vit + dex + wis

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "RPG Character Sheet",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Roll dices for your attributes",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            StatRow(label = "VITALIDAD", value = vit) {
                vit = (MIN_STAT..MAX_STAT).random()
                Log.d(TAG, "Nuevo valor VIT: $vit")
            }

            StatRow(label = "DESTREZA", value = dex) {
                dex = (MIN_STAT..MAX_STAT).random()
                Log.d(TAG, "Nuevo valor DEX: $dex")
            }

            StatRow(label = "SABIDURÍA", value = wis) {
                wis = (MIN_STAT..MAX_STAT).random()
                Log.d(TAG, "Nuevo valor WIS: $wis")
            }

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Button(
                onClick = { rollDice() },
                enabled = !isRolling,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.outline
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Lanzar dado",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = if (isRolling) "LANZANDO..." else "LANZAR D20",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Dado de 20 caras (d20)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatRow (label: String, value: Int, onRoll: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.labelMedium)
                Text(
                    text = value.toString(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onRoll,
                modifier = Modifier.height(50.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("ROLL")
            }
        }
    }
}

@Preview (
    showBackground = true,
    showSystemUi = true
)

@Composable
fun CharacterPreview() {
    MaterialTheme {
        CharacterCreationScreen()
    }
}