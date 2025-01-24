package com.example.supabasedemo.compose.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.supabasedemo.compose.viewModels.MainViewModel
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.ui.theme.AppTheme

/**
 * Screen that displays the authenticated user's account information.
 * Currently shows the user's email address in a simple layout.
 * Provides back navigation to the settings screen.
 */

@Composable
fun AccountInfoScreen(
    // Navigate back to settings screen
    onNavigateToSettings: () -> Unit,
    // Update app-wide user state
    setState: (state: UserState) -> Unit
){
    // Set user state when screen becomes active
    LaunchedEffect(Unit) {
        setState(UserState.InAccountInfo)
    }

    // Initialize view model and get current user's email
    val viewModel = MainViewModel(LocalContext.current, setState = { setState(it) })
    val userEmail = viewModel.supabaseAuth.getCurrentUserInfo().email.toString()

    // Main layout container
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        // Email display section
        Text(
            text = "E-mail:"
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Box(modifier = Modifier
            .border(1.dp, AppTheme.colorScheme.outlineVariant, RectangleShape)
            .padding(4.dp)
        ){
            Text(text = userEmail)
        }

        // Handle back button press
        BackHandler {
            setState(UserState.InSettings)
            onNavigateToSettings()
        }
    }
}