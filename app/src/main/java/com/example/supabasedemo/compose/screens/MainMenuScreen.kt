package com.example.supabasedemo.compose.screens

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

@Composable
fun MainMenuScreen(
    onNavigateToLoginChoice: () -> Unit,
    onNavigateToGame: () -> Unit,
    onNavigateToTutorial: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToMiniGame: () -> Unit,
    onNavigateToEndGame: () -> Unit,
    getState: () -> MutableState<UserState>,
    setState: (state: UserState) -> Unit
) {
    val viewModel = MainViewModel(LocalContext.current, setState = { setState(it) })

    LaunchedEffect(Unit) {
        setState(UserState.InMainMenu)
    }

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
            }) {
            Text(text = "Settings")
        }
        Spacer(modifier = Modifier.padding(8.dp))
        MyOutlinedButton(
            onClick = {
                setState(UserState.InGameCreation)
            }) {
            Text(text = "Play now")
        }
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
                setState(UserState.InTutorial)
            }) {
            Text(text = "How to play?")
        }
        Spacer(modifier = Modifier.padding(8.dp))
        MyOutlinedButton(
            onClick = {
                setState(UserState.InMiniGame)
            }) {
            Text(text = "Mini-game")
        }
    }

    val userState = getState().value
    when (userState) {
        is UserState.Logout -> {
            LaunchedEffect(Unit) {
                viewModel.supabaseAuth.logout()
            }
        }

        is UserState.InSettings -> {
            LaunchedEffect(Unit) {
                onNavigateToSettings()
            }
        }

        is UserState.InGameCreation -> {
            LaunchedEffect(Unit) {
                onNavigateToGame()
            }
        }

        is UserState.InStats -> {
            LaunchedEffect(Unit) {
                onNavigateToStats()
            }
        }

        is UserState.InTutorial -> {
            LaunchedEffect(Unit) {
                onNavigateToTutorial()
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

        is UserState.InMiniGame -> {
            LaunchedEffect(Unit) {
                onNavigateToMiniGame()
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