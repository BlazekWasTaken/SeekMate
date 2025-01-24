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
import com.example.supabasedemo.ui.theme.AppTheme
import java.util.Locale

/**
 * Provides a compass-style rotation display that shows device orientation.
 * Uses device sensors to track azimuth (rotation around vertical axis).
 */

/**
 * A composable that displays the current device rotation angle.
 * Shows azimuth value in degrees within a bordered box.
 *
 * @param context Android context needed for sensor access
 */
@Composable
fun RotationView(
    context: Context,
) {
    // Collect rotation sensor readings as state
    val rotationReading by SensorManagerSingleton.compassReadingsFlow.collectAsState()

    // Initialize sensors when view is first composed
    LaunchedEffect(Unit) {
        SensorManagerSingleton.initialize(context)
    }

    Box(
        modifier = Modifier.border(1.dp, AppTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .padding(4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "ROTATION")
            Text(text = "azimuth: " + rotationReading.last().fixForScreen())
        }
    }
}

/**
 * Formats rotation angle for display.
 * Adds degree symbol and leading space for positive values.
 *
 * @return Formatted string with degree symbol
 */
private fun Float.fixForScreen(): String {
    return if (this < 0) {
        String.format(Locale.getDefault(), "%.0f", this) + "\u00B0"
    }
    else {
        " " + String.format(Locale.getDefault(), "%.0f", this) + "\u00B0"
    }
}
