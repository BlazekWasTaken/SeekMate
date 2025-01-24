package com.example.supabasedemo.compose.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.supabasedemo.compose.viewModels.MainViewModel
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.ui.theme.MyOutlinedButton

/**
 * Main menu screen that serves as the central hub for navigation.
 * Provides access to:
 * - Game start
 * - Tutorial
 * - Settings
 * - Statistics
 * - Mini-game practice
 * - Logout
 *
 * Handles back press by minimizing app rather than navigating back.
 */
@Composable
fun MainMenuScreen(
    // Navigation callbacks
    onNavigateToLoginChoice: () -> Unit,    // Called after logout
    onNavigateToGame: () -> Unit,           // Start new game
    onNavigateToTutorial: () -> Unit,       // Show tutorial
    onNavigateToSettings: () -> Unit,       // Open settings
    onNavigateToStats: () -> Unit,          // Show statistics
    onNavigateToMiniGame: () -> Unit,       // Practice mode
    onNavigateToEndGame: () -> Unit,        // Game completion
    // State management
    getState: () -> MutableState<UserState>, // Current app state
    setState: (state: UserState) -> Unit     // Update app state
) {
    // Minimize app on back press instead of navigation
    val activity = LocalActivity.current
    BackHandler {
        activity?.moveTaskToBack(true)
    }

    val viewModel = MainViewModel(LocalContext.current, setState = { setState(it) })

    // Initialize screen state
    LaunchedEffect(Unit) {
        setState(UserState.InMainMenu)
    }

    // Main menu button layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MyOutlinedButton(
            onClick = {
                setState(UserState.Logout)
            }) {
            Text(text = "Log out")
        }
        Spacer(modifier = Modifier.padding(8.dp))
        MyOutlinedButton(
            onClick = {
                setState(UserState.InSettings)
                onNavigateToSettings()
            }) {
            Text(text = "Settings")
        }
        Spacer(modifier = Modifier.padding(8.dp))
        MyOutlinedButton(
            onClick = {
                setState(UserState.InGameCreation)
                onNavigateToGame()
            }) {
            Text(text = "Play now")
        }
        Spacer(modifier = Modifier.padding(8.dp))
        MyOutlinedButton(
            onClick = {
                setState(UserState.InStats)
                onNavigateToStats()
            }) {
            Text(text = "See stats")
        }
        Spacer(modifier = Modifier.padding(8.dp))
        MyOutlinedButton(
            onClick = {
                setState(UserState.InTutorial)
                onNavigateToTutorial()
            }) {
            Text(text = "How to play?")
        }
        Spacer(modifier = Modifier.padding(8.dp))
        MyOutlinedButton(
            onClick = {
                setState(UserState.InMiniGame)
                onNavigateToMiniGame()
            }) {
            Text(text = "Mini-game")
        }
    }

    // Handle state transitions
    val userState = getState().value
    when (userState) {
        is UserState.Logout -> {
            LaunchedEffect(Unit) {
                viewModel.supabaseAuth.logout()
            }
        }

        is UserState.LogoutSucceeded -> {
            LaunchedEffect(Unit) {
                onNavigateToLoginChoice()
            }
        }

        is UserState.LogoutFailed -> {
            LaunchedEffect(Unit) {
                setState(UserState.InMainMenu)
            }
        }

        is UserState.InEndGame -> {
            LaunchedEffect(Unit) {
                onNavigateToEndGame()
            }
        }

        else -> {

        }
    }
}