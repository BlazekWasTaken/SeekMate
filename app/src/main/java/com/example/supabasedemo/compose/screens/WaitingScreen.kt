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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.supabasedemo.compose.viewModels.MainViewModel
import com.example.supabasedemo.data.model.UserState

@Composable
fun WaitingScreen(
    getState: () -> MutableState<UserState>,
    setState: (UserState) -> Unit,
    viewModel: MainViewModel
) {
    val userState = getState().value
    if (userState is UserState.InWaitingScreen) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Waiting for the other player...")
            Spacer(modifier = Modifier.padding(8.dp))
            Text(text = "Score: ${viewModel.score.value}")
            Text(text = "Round: ${userState.round}")
        }
    }
}