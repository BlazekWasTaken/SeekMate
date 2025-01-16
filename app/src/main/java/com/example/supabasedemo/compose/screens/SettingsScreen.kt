package com.example.supabasedemo.compose.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.ui.theme.MyOutlinedButton

@Composable
fun SettingsScreen(
    onNavigateToMainMenu: () -> Unit,
    onNavigateToAccountInfo: () -> Unit,
    onNavigateToThemeChoice: () -> Unit,
    onNavigateToDemo: () -> Unit,
    setState: (state: UserState) -> Unit
){
    LaunchedEffect(Unit) {
        setState(UserState.InSettings)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        MyOutlinedButton(
            onClick = {
                setState(UserState.InAccountInfo)
                onNavigateToAccountInfo()
            }) {
            Text(text = "Account Info")
        }
        Spacer(modifier = Modifier.padding(8.dp))
        MyOutlinedButton(
            onClick = {
                setState(UserState.InThemeChoice)
                onNavigateToThemeChoice()
            }) {
            Text(text = "Theme choice")
        }
        Spacer(modifier = Modifier.padding(8.dp))
        MyOutlinedButton(
            onClick = {
                setState(UserState.InDemo)
                onNavigateToDemo()
            }) {
            Text(text = "Demo")
        }

        BackHandler {
            setState(UserState.InMainMenu)
            onNavigateToMainMenu()
        }
    }
}