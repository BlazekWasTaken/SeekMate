package com.example.supabasedemo.compose.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.unit.dp
import com.example.supabasedemo.compose.viewModels.MainViewModel
import com.example.supabasedemo.data.model.Game
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.ui.theme.AppTheme
import com.example.supabasedemo.ui.theme.MyOutlinedButton

/**
 * EndGameScreen displays the game results after a match has concluded.
 * It shows whether the player won or lost and provides navigation options
 * to view statistics or return to the main menu.
 */

/**
 * @param setState Function to update the global user state
 * @param onNavigateToMainMenu Callback to navigate back to main menu
 * @param getGame Function to retrieve the completed game data
 * @param viewModel Main view model containing auth and game logic
 * @param onNavigateToStats Callback to navigate to statistics screen
 */
@Composable
fun EndGameScreen(
    setState: (UserState) -> Unit,
    onNavigateToMainMenu: () -> Unit,
    getGame: () -> Game,
    viewModel: MainViewModel,
    onNavigateToStats: () -> Unit
) {
    // Track whether current user won the game
    var didUserWin: Boolean by remember { mutableStateOf(false) }

    // Determine game winner when screen loads
    LaunchedEffect(Unit) {
        val game = getGame()
        // Get current user ID and clean it
        val userId = viewModel.supabaseAuth.getCurrentUser()?.get("sub").toString().trim().replace("\"", "")

        // Compare user ID with game data to determine if they won
        // User1 is the controller - they win if game.won is true
        // User2 is the non-controller - they win if game.won is false
        if (game.user1 == userId) {
            didUserWin = game.won!!
        } else if (game.user2 == userId) {
            didUserWin = !game.won!!
        }
    }

    // UI Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Game ended")
        Spacer(modifier = Modifier.padding(8.dp))

        // Display win/lose message
        if (didUserWin) Text(text = "You won! Congrats", style = AppTheme.typography.labelLarge)
        else Text(text = "You lost:( Bummer", style = AppTheme.typography.labelLarge)

        Spacer(modifier = Modifier.padding(8.dp))

        // Navigation buttons
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
                setState(UserState.InMainMenu)
                onNavigateToMainMenu()
            }) {
            Text(text = "Back to Main Menu")
        }

        // Handle back button press
        BackHandler {
            setState(UserState.InMainMenu)
            onNavigateToMainMenu()
        }
    }
}