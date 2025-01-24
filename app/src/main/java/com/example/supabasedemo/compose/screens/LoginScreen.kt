package com.example.supabasedemo.compose.screens

import android.annotation.SuppressLint
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

/**
 * Login screen composable that handles user authentication.
 * Features:
 * - Email/password input fields
 * - Login button
 * - State feedback messages
 * - Back navigation to login choice screen
 */

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun LoginScreen(
    // Navigation callbacks
    onNavigateToMainMenu: () -> Unit,      // Called on successful login
    onNavigateToLoginChoice: () -> Unit,    // Called when back pressed
    // State management
    getState: () -> MutableState<UserState>, // Get current user state
    setState: (state: UserState) -> Unit     // Update user state
) {
    // Initialize ViewModel with context and state setter
    val viewModel = MainViewModel(LocalContext.current, setState = { setState(it) })

    // Track input field values
    var userEmail by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
    var currentUserState by remember { mutableStateOf("") }

    // Handle back button press
    BackHandler {
        setState(UserState.InLoginChoice)
        onNavigateToLoginChoice()
    }

    // Set initial state when screen launches
    LaunchedEffect(Unit) {
        setState(UserState.InLogin)
    }

    // Main login form layout
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

        // Password input field with security features
        MyOutlinedTextField(
            value = userPassword,
            placeholder = { Text(text = "Enter password") },
            onValueChange = { userPassword = it },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )

        Spacer(modifier = Modifier.padding(8.dp))

        // Login button - triggers authentication
        MyOutlinedButton(onClick = {
            viewModel.supabaseAuth.login(userEmail, userPassword)
        }) {
            Text(text = "Log in")
        }

        // Handle different user states and show appropriate feedback
        when (val userState = getState().value) {
            is UserState.LoginOrSignupLoading -> {
                currentUserState = "Loading..."
            }
            is UserState.LoginOrSignupSucceeded -> {
                currentUserState = userState.message
                LaunchedEffect(Unit) {
                    onNavigateToMainMenu()
                }
            }
            is UserState.LoginOrSignupFailed -> {
                currentUserState = userState.message
            }
            else -> { }
        }

        // Display current state message if any
        if (currentUserState.isNotEmpty()) {
            Text(text = currentUserState)
        }
    }
}