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

@Composable
fun AccountInfoScreen(
    onNavigateToSettings: () -> Unit,
    setState: (state: UserState) -> Unit
){
    LaunchedEffect(Unit) {
        setState(UserState.InAccountInfo)
    }

    val viewModel = MainViewModel(LocalContext.current, setState = { setState(it) })
    val userEmail = viewModel.supabaseAuth.getCurrentUserInfo().email.toString()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
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

        BackHandler {
            setState(UserState.InSettings)
            onNavigateToSettings()
        }
    }
}