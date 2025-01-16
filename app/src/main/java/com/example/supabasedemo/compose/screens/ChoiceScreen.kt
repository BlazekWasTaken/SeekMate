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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.supabasedemo.compose.viewModels.MainViewModel
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.ui.theme.MyOutlinedButton

@Composable
fun ChoiceScreen(
    onNavigateToLogIn: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToMainMenu: () -> Unit,
    getState: () -> MutableState<UserState>,
    setState: (state: UserState) -> Unit
) {
    val viewModel = MainViewModel(LocalContext.current, setState = { setState(it) })

    var shouldCompose by remember { mutableStateOf(false) }

    val activity = LocalActivity.current

    BackHandler {
        activity?.moveTaskToBack(true)
    }

    LaunchedEffect(Unit) {
        viewModel.supabaseAuth.isUserLoggedIn()
    }

    when (val state = getState().value) {
        is UserState.CheckingLoginStatus -> shouldCompose = false

        is UserState.CheckedLoginStatusSucceeded -> {
            if (state.message == "User already logged in!") {
                shouldCompose = false
                LaunchedEffect(Unit) {
                    onNavigateToMainMenu()
                }
            }
            else {
                shouldCompose = true
            }
        }

        is UserState.CheckedLoginStatusFailed -> shouldCompose = true

        else -> {

        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (shouldCompose) {
            MyOutlinedButton(
                onClick = {
                    setState(UserState.InLogin)
                    onNavigateToLogIn()
                }) {
                Text(text = "Log In")
            }
            Spacer(modifier = Modifier.padding(8.dp))
            MyOutlinedButton(
                onClick = {
                    setState(UserState.InSignup)
                    onNavigateToSignUp()
                }) {
                Text(text = "Sign Up")
            }
        }
    }
}
