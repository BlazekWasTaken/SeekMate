package com.example.supabasedemo.compose.views

import com.example.supabasedemo.data.network.UwbManagerSingleton
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.supabasedemo.ui.theme.AppTheme
import java.util.Locale
/**
 * UWB (Ultra-Wideband) data visualization component that displays:
 * - Distance between UWB devices in meters
 * - Azimuth angle between devices in degrees
 *
 * Data is collected from UwbManagerSingleton which handles the UWB device communication.
 * Values of -1 indicate the data is still loading or unavailable.
 */

/**
 * Composable that displays real-time UWB ranging data in a bordered box.
 * Collects distance and azimuth data from UwbManagerSingleton state flows.
 */
@Composable
fun UwbDataView() {
    // Collect latest UWB measurements, defaulting to -1 when unavailable
    val distance by UwbManagerSingleton.distance.collectAsState(initial = -1.0)
    val azimuth by UwbManagerSingleton.azimuth.collectAsState(initial = -1.0)

    Box(
        modifier = Modifier.border(1.dp, AppTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .padding(4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "UWB")
            Text(text = "distance: ${distance.toFloat().fixDistanceForScreen()}")
            Text(text = "angle: ${azimuth.toFloat().fixAngleForScreen()}")
        }
    }
}

/**
 * Formats distance measurements for display.
 * @return Formatted string with 2 decimal places and 'm' suffix, or "Loading..."
 */
private fun Float.fixDistanceForScreen(): String {
    return if (this != -1F) {
        String.format(Locale.getDefault(), "%.2f", this) + "m"
    } else {
        "Loading..."
    }
}

/**
 * Formats azimuth angle measurements for display.
 * @return Formatted string with 0 decimal places and degree symbol, or "Loading..."
 */
private fun Float.fixAngleForScreen(): String {
    return if (this != -1F) {
        String.format(Locale.getDefault(), "%.0f", this) + "\u00B0"
    } else {
        "Loading..."
    }
}