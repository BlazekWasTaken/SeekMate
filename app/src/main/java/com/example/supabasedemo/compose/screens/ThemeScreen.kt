package com.example.supabasedemo.compose.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.ui.theme.ThemeChoice

@Composable
fun ThemeScreen(
    onNavigateToSettings: () -> Unit,
    setTheme: (theme: ThemeChoice) -> Unit,
    setState: (state: UserState) -> Unit
){
    LaunchedEffect(Unit) {
        setState(UserState.InThemeChoice)
    }

    var matchDeviceTheme by remember { mutableStateOf(true) }
    var dark by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            Text(
                text = "Match device theme?"
            )
            Spacer(modifier = Modifier.padding(6.dp))
            Switch(
                checked = matchDeviceTheme,
                onCheckedChange = {
                    matchDeviceTheme = it
                    if (matchDeviceTheme) {
                        setTheme(ThemeChoice.System)
                    }
                }
            )
        }
        if (!matchDeviceTheme) {
            Spacer(modifier = Modifier.padding(8.dp))
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                Text(
                    text = "Dark?"
                )
                Spacer(modifier = Modifier.padding(6.dp))
                Switch(
                    checked = dark,
                    onCheckedChange = {
                        dark = it
                        if (dark) {
                            setTheme(ThemeChoice.Dark)
                        }
                        else {
                            setTheme(ThemeChoice.Light)
                        }
                    }
                )
            }
        }

        BackHandler {
            setState(UserState.InSettings)
            onNavigateToSettings()
        }
    }
}