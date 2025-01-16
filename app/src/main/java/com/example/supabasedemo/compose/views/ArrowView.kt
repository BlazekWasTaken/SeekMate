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

@Composable
fun ArrowView() {
    val uwbAngle by UwbManagerSingleton.azimuth.collectAsState()

    val angleHistory = remember { mutableStateListOf<Float>() }
    LaunchedEffect(uwbAngle) {
        angleHistory.add(uwbAngle)
        if (angleHistory.size > 10) angleHistory.removeAt(0)
        delay(500)
    }
    val angleStdDev = computeStandardDeviation(angleHistory)
    val stdThreshold = 30.0f;

    Box(
        modifier = Modifier
            .border(1.dp, AppTheme.colorScheme.outline)
            .size(150.dp, 150.dp)
    ) {
        if (angleStdDev > stdThreshold) {
            Log.i("Arrow", "Std: ${angleStdDev} greater than threshold: ${stdThreshold}. Showing behind you message")
            Text(text = "Behind you")
        } else {
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

fun computeStandardDeviation(values: List<Float>): Float {
    if (values.isEmpty()) return 0f
    val mean = values.average().toFloat()
    val squaredDiffs = values.map { (it - mean) * (it - mean) }
    val variance = squaredDiffs.sum() / values.size
    return sqrt(variance)
}
