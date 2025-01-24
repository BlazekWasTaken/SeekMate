package com.example.supabasedemo.compose.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.supabasedemo.compose.viewModels.MainViewModel
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.ui.theme.MyOutlinedButton
import com.example.supabasedemo.ui.theme.MyOutlinedTextField
import com.example.supabasedemo.ui.theme.Typography

/**
 * SignupScreen allows new users to create an account by providing:
 * - Email address
 * - Username
 * - Password
 *
 * The screen handles input validation, signup API calls, and displays status messages.
 * It provides navigation back to the login choice screen.
 */

@Composable
fun SignupScreen(
    // Navigation callback to return to login choice screen
    onNavigateToLoginChoice: () -> Unit,
    // State management callbacks
    getState: () -> MutableState<UserState>,
    setState: (state: UserState) -> Unit
) {
    // Initialize ViewModel for auth operations
    val viewModel = MainViewModel(LocalContext.current, setState = { setState(it) })

    // Input field state
    var userEmail by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var currentUserState by remember { mutableStateOf("") }

    // Handle back button press
    BackHandler {
        setState(UserState.InLoginChoice)
        onNavigateToLoginChoice()
    }

    // Initial setup on screen launch
    LaunchedEffect(Unit) {
        setState(UserState.InSignup)
        viewModel.supabaseAuth.isUserLoggedIn()
    }

    // Main UI Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Email input field
        MyOutlinedTextField(
            value = userEmail,
            placeholder = { Text(text = "Enter email") },
            onValueChange = { userEmail = it }
        )
        Spacer(modifier = Modifier.padding(8.dp))

        // Username input field
        MyOutlinedTextField(
            value = username,
            placeholder = { Text(text = "Enter username") },
            onValueChange = { username = it }
        )
        Spacer(modifier = Modifier.padding(8.dp))

        // Password input field with security options
        MyOutlinedTextField(
            value = userPassword,
            placeholder = { Text(text = "Enter password") },
            onValueChange = { userPassword = it },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )
        Spacer(modifier = Modifier.padding(8.dp))

        // Signup button
        MyOutlinedButton(
            onClick = {
                viewModel.supabaseAuth.signUp(userEmail, userPassword, username)
            }
        ) {
            Text(text = "Sign Up")
        }

        // State handling and status messages
        val userState = getState().value
        when (userState) {
            is UserState.LoginOrSignupLoading -> {
                currentUserState = "Loading..."
            }
            is UserState.LoginOrSignupSucceeded -> {
                currentUserState = userState.message
                // Navigate away on successful signup
                if (getState().value is UserState.LoginOrSignupSucceeded) {
                    LaunchedEffect(Unit) {
                        onNavigateToLoginChoice()
                    }
                }
            }
            is UserState.LoginOrSignupFailed -> {
                currentUserState = userState.message
            }
            is UserState.CheckedLoginStatusSucceeded -> {
                currentUserState = userState.message
            }
            else -> { }
        }

        // Display current status message if any
        if (currentUserState.isNotEmpty()) {
            Text(
                style = Typography.bodyLarge,
                text = currentUserState
            )
        }
    }
}