package com.example.listasensores

import android.content.Context
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.listasensores.model.SensorData
import com.example.listasensores.ui.theme.SensorDetectorTheme
import com.example.listasensores.viewmodel.SensorViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SensorDetectorTheme {
                SensorScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorScreen(viewModel: SensorViewModel = viewModel()) {
    val context = LocalContext.current
    var sensors by remember { mutableStateOf(viewModel.sensors) }

    // Cargar sensores al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadSensors(context)
    }

    // Actualizar periódicamente el nivel de batería
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.updateBatteryLevel(context)
            delay(5000) // Actualizar cada 5 segundos
        }
    }

    // Escuchar cambios en el ViewModel
    LaunchedEffect(viewModel.sensors) {
        sensors = viewModel.sensors
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sensores del Dispositivo") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (sensors.isEmpty()) {
                Text(
                    text = "Cargando sensores...",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    items(sensors) { sensor ->
                        SensorItem(sensor = sensor) {
                            if (sensor.name == "Linterna (Flash)") {
                                viewModel.toggleFlashlight()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SensorItem(
    sensor: SensorData,
    onFlashlightToggle: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = sensor.name,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (sensor.isDynamic) {
            Text(
                text = "Estado: ${sensor.dynamicValue}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (sensor.name == "Linterna (Flash)") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onFlashlightToggle,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(if (sensor.dynamicValue == true) "Apagar" else "Encender")
                    }
                }
            }
        } else {
            Text(
                text = "Tipo: ${sensor.type} | Proveedor: ${sensor.vendor}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Rango Máximo: ${sensor.maxRange} | Resolución: ${sensor.resolution}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Versión: ${sensor.version} | Consumo: ${sensor.power}mA",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}