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

//jak jest controller to jest user1

@Composable
fun EndGameScreen(
    getState: () -> MutableState<UserState>,
    setState: (UserState) -> Unit,
    onNavigateToMainMenu: () -> Unit,
    getGame: () -> Game,
    viewModel: MainViewModel,
    onNavigateToStats: () -> Unit
) {
    var didUserWin: Boolean by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        var game = getGame()
        var userId = viewModel.supabaseAuth.getCurrentUser()?.get("sub").toString().trim().replace("\"", "")
        didUserWin = if (game.user1 == userId) game.won!! else !game.won!!
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Game ended")
        Spacer(modifier = Modifier.padding(8.dp))

        if (didUserWin) Text(text = "You won! Congrats", style = AppTheme.typography.labelLarge)
        else Text(text = "You lost:( Bummer", style = AppTheme.typography.labelLarge)

        Spacer(modifier = Modifier.padding(8.dp))
        MyOutlinedButton(
            onClick = {
                setState(UserState.InStats)
            }) {
            Text(text = "See stats")
        }
        Spacer(modifier = Modifier.padding(8.dp))
        MyOutlinedButton(
            onClick = {
                setState(UserState.InMainMenu)
            }) {
            Text(text = "Back to Main Menu")
        }

        BackHandler {
            setState(UserState.InMainMenu)
        }

        val userState = getState().value
        when (userState) {
            is UserState.InMainMenu -> {
                LaunchedEffect(Unit) {
                    onNavigateToMainMenu()
                }
            }
            is UserState.InAccountInfo -> {
                LaunchedEffect(Unit) {
                    onNavigateToStats()
                }
            }
            else -> {}
        }
    }
}