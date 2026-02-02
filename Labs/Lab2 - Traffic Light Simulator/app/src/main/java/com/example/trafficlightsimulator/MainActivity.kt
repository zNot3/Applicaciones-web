package com.example.trafficlightsimulator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

enum class Light { Red, Yellow, Green }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface (
                    modifier = Modifier.fillMaxSize(),
                    color = Color.DarkGray
                ) {
                    TrafficLightSimulator()
                }
            }
        }
    }
}

@Composable
fun TrafficLightSimulator() {
    var currentLight by remember { mutableStateOf(Light.Red) }

    LaunchedEffect(Unit) {
        while (true) {
            currentLight = Light.Red
            delay(2000)
            currentLight = Light.Green
            delay(2000)
            currentLight = Light.Yellow
            delay(1000)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .background(Color.Black, shape = MaterialTheme.shapes.medium)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TrafficLightCircle(
                color = if (currentLight == Light.Red) Color.Red else Color.Gray
            )
            TrafficLightCircle(
                color = if (currentLight == Light.Yellow) Color.Yellow else Color.Gray
            )
            TrafficLightCircle(
                color = if (currentLight == Light.Green) Color.Green else Color.Gray
            )
        }
    }
}

@Composable
fun TrafficLightCircle(color: Color) {
    
}