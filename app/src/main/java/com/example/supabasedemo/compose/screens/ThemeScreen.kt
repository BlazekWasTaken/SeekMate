package com.example.supabasedemo.compose.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.ui.theme.ThemeChoice

/**
 * A screen that allows users to customize the app's theme settings.
 * Provides options to:
 * - Match the system/device theme
 * - Manually toggle between light and dark themes
 */

/**
 * Theme selection screen composable
 * @param onNavigateToSettings Callback to return to settings screen
 * @param setTheme Callback to update the app's theme choice
 * @param setState Callback to update the user's current state
 */
@Composable
fun ThemeScreen(
    onNavigateToSettings: () -> Unit,
    setTheme: (theme: ThemeChoice) -> Unit,
    setState: (state: UserState) -> Unit
){
    // Set initial state when screen is launched
    LaunchedEffect(Unit) {
        setState(UserState.InThemeChoice)
    }

    // Theme state management
    var matchDeviceTheme by remember { mutableStateOf(true) }
    var dark by remember { mutableStateOf(true) }

    // Main layout container
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        // System theme matching toggle
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            Text(text = "Match device theme?")
            Spacer(modifier = Modifier.padding(6.dp))
            Switch(
                checked = matchDeviceTheme,
                onCheckedChange = {
                    matchDeviceTheme = it
                    if (matchDeviceTheme) {
                        setTheme(ThemeChoice.System)
                    }
                }
            )
        }

        // Manual dark/light theme toggle (only shown if not matching device theme)
        if (!matchDeviceTheme) {
            Spacer(modifier = Modifier.padding(8.dp))
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                Text(text = "Dark?")
                Spacer(modifier = Modifier.padding(6.dp))
                Switch(
                    checked = dark,
                    onCheckedChange = {
                        dark = it
                        setTheme(if (dark) ThemeChoice.Dark else ThemeChoice.Light)
                    }
                )
            }
        }

        // Handle back navigation
        BackHandler {
            setState(UserState.InSettings)
            onNavigateToSettings()
        }
    }
}