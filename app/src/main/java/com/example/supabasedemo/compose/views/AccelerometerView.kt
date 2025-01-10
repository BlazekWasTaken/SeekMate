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
import com.example.supabasedemo.data.network.avg
import com.example.supabasedemo.data.network.fixForScreen
import com.example.supabasedemo.data.network.getForwardAcceleration
import com.example.supabasedemo.ui.theme.AppTheme
import java.util.Locale

@Composable
fun AccelerometerView(
    context: Context
) {
    val accelerations by SensorManagerSingleton.linearAccelerometerReadingsFlow.collectAsState()
    val gravity by SensorManagerSingleton.gravityReadingsFlow.collectAsState()

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
            Text(text = "ACCELEROMETER")
            Text(text = "x: " + (accelerations.takeLast(20).avg().x * 100).fixForScreen())
            Text(text = "y: " + (accelerations.takeLast(20).avg().y * 100).fixForScreen())
            Text(text = "z: " + (accelerations.takeLast(20).avg().z * 100).fixForScreen())
            Text(text = "GRAVITY")
            Text(text = "x: " + gravity.last().x.fixForScreen())
            Text(text = "y: " + gravity.last().y.fixForScreen())
            Text(text = "z: " + gravity.last().z.fixForScreen())
            Text(text = "FORWARD")
            Text(text = (getForwardAcceleration() * 100).fixForScreen())
        }
    }
}