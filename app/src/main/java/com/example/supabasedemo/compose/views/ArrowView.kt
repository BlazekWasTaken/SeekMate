package com.example.supabasedemo.compose.views

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.supabasedemo.R
import com.example.supabasedemo.data.network.UwbManagerSingleton
import com.example.supabasedemo.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlin.math.sqrt

/**
 * Provides a visual direction indicator using UWB (Ultra-Wideband) angle data.
 * This component displays either:
 * - An arrow pointing in the direction of another UWB device
 * - A "Behind you" message when angle variations indicate target is behind user
 */

/**
 * Displays a directional arrow that rotates based on UWB angle measurements.
 * Uses a rolling window of angle measurements to determine stability and position.
 */
@Composable
fun ArrowView() {
    // Get current UWB angle from singleton manager
    val uwbAngle by UwbManagerSingleton.azimuth.collectAsState()

    // Maintain history of last 10 angle measurements to detect stability
    val angleHistory = remember { mutableStateListOf<Float>() }
    LaunchedEffect(uwbAngle) {
        angleHistory.add(uwbAngle)
        if (angleHistory.size > 10) angleHistory.removeAt(0)
        delay(500) // Sample every 500ms
    }

    // Calculate angle variation to detect if target is behind user
    val angleStdDev = computeStandardDeviation(angleHistory)
    val stdThreshold = 30.0f // Threshold for considering angle unstable

    Box(
        modifier = Modifier
            .border(1.dp, AppTheme.colorScheme.outline)
            .size(150.dp, 150.dp)
    ) {
        if (angleStdDev > stdThreshold) {
            // High angle variation indicates target is behind user
            Log.i("Arrow", "Std: ${angleStdDev} greater than threshold: ${stdThreshold}. Showing behind you message")
            Text(text = "Behind you")
        } else {
            // Stable angle measurement - show rotating arrow
            Log.i("Arrow", "Std: ${angleStdDev} less than threshold: ${stdThreshold}. Showing arrow")
            Image(
                painter = painterResource(R.drawable.arrow),
                contentDescription = "Arrow image",
                modifier = Modifier
                    .rotate(-uwbAngle)
                    .fillMaxSize()
                    .padding(25.dp)
            )
        }
    }
}

/**
 * Calculates the standard deviation of a list of angle measurements.
 * Used to determine stability of UWB angle readings.
 *
 * @param values List of angle measurements in degrees
 * @return Standard deviation of the angles, or 0 if list is empty
 */
fun computeStandardDeviation(values: List<Float>): Float {
    if (values.isEmpty()) return 0f
    val mean = values.average().toFloat()
    val squaredDiffs = values.map { (it - mean) * (it - mean) }
    val variance = squaredDiffs.sum() / values.size
    return sqrt(variance)
}
