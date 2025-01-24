package com.example.supabasedemo.compose.views

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.supabasedemo.data.network.SensorManagerSingleton
import com.example.supabasedemo.data.network.fixForScreen
import com.example.supabasedemo.ui.theme.AppTheme
import java.util.Locale

/**
 * A composable view that displays real-time gyroscope sensor data.
 * Utilizes SensorManagerSingleton to access device gyroscope readings
 * and displays X, Y, Z rotation values in a bordered box.
 */

@Composable
fun GyroscopeView(
    context: Context // Required to initialize sensor manager
) {
    // Collect latest gyroscope readings as state
    val gyroscopes by SensorManagerSingleton.gyroscopeReadingsFlow.collectAsState()

    // Initialize sensor manager when view is first composed
    LaunchedEffect(Unit) {
        SensorManagerSingleton.initialize(context)
    }

    // Container with border outline
    Box(
        modifier = Modifier.border(1.dp, AppTheme.colorScheme.outline)
    ) {
        // Display gyroscope readings in vertical layout
        Column(
            modifier = Modifier.padding(4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "GYROSCOPE")
            // Display formatted X, Y, Z rotation values
            Text(text = "x: " + gyroscopes.last().x.fixForScreen())
            Text(text = "y: " + gyroscopes.last().y.fixForScreen())
            Text(text = "z: " + gyroscopes.last().z.fixForScreen())
        }
    }
}