package com.example.supabasedemo.compose.screens

import com.example.supabasedemo.data.network.UwbManagerSingleton
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.supabasedemo.compose.viewModels.MainViewModel
import com.example.supabasedemo.compose.views.AccelerometerView
import com.example.supabasedemo.compose.views.ArrowView
import com.example.supabasedemo.compose.views.CollectingDataView
import com.example.supabasedemo.compose.views.GyroscopeView
import com.example.supabasedemo.compose.views.RotationView
import com.example.supabasedemo.compose.views.UwbDataView
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.ui.theme.AppTheme
import com.example.supabasedemo.ui.theme.MyOutlinedButton
import com.example.supabasedemo.ui.theme.MyOutlinedTextField
import kotlin.random.Random

/**
 * Demo screen for testing UWB (Ultra-wideband) functionality and sensor data.
 * Features:
 * - UWB connection management (controller/responder roles)
 * - Device pairing via address/preamble
 * - Sensor data visualization (accelerometer, gyroscope, rotation)
 * - Direction indicators
 * - Data collection controls
 */

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun UwbScreen(
    // Navigate back to settings screen
    onNavigateToSettings: () -> Unit,
    // Update global app state
    setState: (state: UserState) -> Unit
) {
    // Initialize screen state
    LaunchedEffect(Unit) {
        setState(UserState.InDemo)
    }

    // Core state management
    val context = LocalContext.current
    val viewModel = MainViewModel(context, setState = { setState(it) })

    // UWB connection state
    var isController by remember { mutableStateOf(true) }  // Toggle between controller/responder
    val isStarted by UwbManagerSingleton.isStartedFlow.collectAsState(initial = false)
    var address by remember { mutableStateOf("") }         // Partner device address
    var preamble by remember { mutableStateOf("") }        // Connection preamble
    val deviceAddress by UwbManagerSingleton.address.collectAsState(initial = "-1")
    val devicePreamble by UwbManagerSingleton.preamble.collectAsState(initial = "-1")

    // Direction tracking
    var directionId: Int by remember { mutableIntStateOf(0) }

    // Permission management
    var permissionGranted by remember { mutableStateOf(false) }

    // Initialize UWB and check permissions
    LaunchedEffect(Unit) {
        if(!isStarted){
            UwbManagerSingleton.initialize(context, isController)
        }
        directionId = Random.nextInt(0, 1000)

        // Request UWB permissions if needed
        permissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.UWB_RANGING
        ) == PackageManager.PERMISSION_GRANTED
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(
                context as Activity, arrayOf(Manifest.permission.UWB_RANGING), 101
            )
        }

        UwbManagerSingleton.fetchDeviceDetails()
    }

    // Main UI container
    Box(
        modifier = Modifier
            .border(1.dp, AppTheme.colorScheme.outline, RectangleShape)
            .fillMaxWidth()
            .height(900.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Role selection UI
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "Controller:")
                Spacer(modifier = Modifier.padding(8.dp))
                Switch(checked = isController, onCheckedChange = {
                    isController = it
                    UwbManagerSingleton.setRoleAsController(it, context)
                    UwbManagerSingleton.stopSession()
                })
            }
            // Connection setup UI
            MyOutlinedTextField(
                value = address,
                onValueChange = { address = it },
                placeholder = { Text(text = "Enter Partner Address") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            if (!isController) {
                Spacer(modifier = Modifier.padding(8.dp))
                MyOutlinedTextField(
                    value = preamble,
                    onValueChange = { preamble = it },
                    placeholder = { Text(text = "Enter Preamble Value") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.padding(8.dp))
                MyOutlinedTextField(
                    value = directionId.toString(),
                    onValueChange = {
                        directionId = if (it != "") it.toInt() else 0
                    },
                    placeholder = { Text(text = "Enter directionId Value") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            // Device info display
            Spacer(modifier = Modifier.padding(8.dp))
            Text(text = "Your Device Address: $deviceAddress")
            if (isController) {
                Text(text = "Your Preamble: $devicePreamble")
                Text(text = "Your directionId: $directionId")
            }
            // Session controls
            Spacer(modifier = Modifier.padding(8.dp))
            if (!isStarted) {
                MyOutlinedButton(onClick = {
                    if (address.isNotBlank()) {
                        if (isController) {
                            viewModel.supabaseDb.createDirection(directionId)
                            UwbManagerSingleton.startSession(address, "0")
                        } else {
                            UwbManagerSingleton.startSession(address, preamble)
                        }
                    }
                }) {
                    Text(text = "Start")
                }
            } else {
                MyOutlinedButton(onClick = {
                    UwbManagerSingleton.stopSession()
                }) {
                    Text(text = "Stop")
                }
            }
            Spacer(modifier = Modifier.padding(8.dp))
            CollectingDataView(
                setState = setState
            )
            // Sensor data displays
            Spacer(modifier = Modifier.padding(24.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // UWB and Gyroscope data
                UwbDataView()
                Spacer(modifier = Modifier.padding(8.dp))
                GyroscopeView(context)
            }
            // Additional sensor displays
            Spacer(modifier = Modifier.padding(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                AccelerometerView(context)
                Spacer(modifier = Modifier.padding(8.dp))
                RotationView(context)
            }
            Spacer(modifier = Modifier.padding(8.dp))
            // Direction indicator
            ArrowView()
            Spacer(modifier = Modifier.padding(8.dp))

            // Navigation handling
            BackHandler {
                setState(UserState.InSettings)
                onNavigateToSettings()
            }
        }
    }
}