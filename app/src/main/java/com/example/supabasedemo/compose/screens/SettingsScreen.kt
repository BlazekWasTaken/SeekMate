package com.example.supabasedemo.compose.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.ui.theme.MyOutlinedButton

/**
 * Settings screen that provides access to:
 * - Account information
 * - Theme customization
 * - Demo/testing features
 *
 * The screen uses a simple vertical layout with buttons for navigation
 * and handles system back press events.
 */

@Composable
fun SettingsScreen(
    onNavigateToMainMenu: () -> Unit,      // Callback to return to main menu
    onNavigateToAccountInfo: () -> Unit,    // Navigate to account settings
    onNavigateToThemeChoice: () -> Unit,    // Navigate to theme selection
    onNavigateToDemo: () -> Unit,           // Navigate to demo features
    setState: (state: UserState) -> Unit    // Update app-wide user state
){
    // Set initial screen state
    LaunchedEffect(Unit) {
        setState(UserState.InSettings)
    }

    // Main content column with centered items
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        // Account settings button
        MyOutlinedButton(
            onClick = {
                setState(UserState.InAccountInfo)
                onNavigateToAccountInfo()
            }) {
            Text(text = "Account Info")
        }

        Spacer(modifier = Modifier.padding(8.dp))

        // Theme selection button
        MyOutlinedButton(
            onClick = {
                setState(UserState.InThemeChoice)
                onNavigateToThemeChoice()
            }) {
            Text(text = "Theme choice")
        }

        Spacer(modifier = Modifier.padding(8.dp))

        // Demo/testing features button
        MyOutlinedButton(
            onClick = {
                setState(UserState.InDemo)
                onNavigateToDemo()
            }) {
            Text(text = "Demo")
        }

        // Handle system back button press
        BackHandler {
            setState(UserState.InMainMenu)
            onNavigateToMainMenu()
        }
    }
}