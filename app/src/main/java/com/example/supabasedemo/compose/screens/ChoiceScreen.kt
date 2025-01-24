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

/**
 * Initial authentication choice screen that:
 * - Checks if user is already logged in
 * - Provides options to log in or sign up
 * - Handles back press by minimizing app
 * - Auto-navigates to main menu if already authenticated
 */

@Composable
fun ChoiceScreen(
    onNavigateToLogIn: () -> Unit,        // Navigate to login screen
    onNavigateToSignUp: () -> Unit,       // Navigate to signup screen
    onNavigateToMainMenu: () -> Unit,     // Navigate to main menu (if already logged in)
    getState: () -> MutableState<UserState>,  // Get current auth state
    setState: (state: UserState) -> Unit   // Update auth state
) {
    // Initialize ViewModel for auth operations
    val viewModel = MainViewModel(LocalContext.current, setState = { setState(it) })

    // Controls whether login/signup buttons should be shown
    var shouldCompose by remember { mutableStateOf(false) }

    val activity = LocalActivity.current

    // Minimize app on back press instead of navigating back
    BackHandler {
        activity?.moveTaskToBack(true)
    }

    // Check login status when screen loads
    LaunchedEffect(Unit) {
        viewModel.supabaseAuth.isUserLoggedIn()
    }

    // Handle different auth states
    when (val state = getState().value) {
        // Hide buttons while checking login
        is UserState.CheckingLoginStatus -> shouldCompose = false

        is UserState.CheckedLoginStatusSucceeded -> {
            if (state.message == "User already logged in!") {
                // Auto-navigate to main menu if logged in
                shouldCompose = false
                LaunchedEffect(Unit) {
                    onNavigateToMainMenu()
                }
            } else {
                // Show auth buttons if not logged in
                shouldCompose = true
            }
        }

        // Show auth buttons if login check fails
        is UserState.CheckedLoginStatusFailed -> shouldCompose = true

        else -> { }
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
