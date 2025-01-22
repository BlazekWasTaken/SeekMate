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

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun LoginScreen(
    onNavigateToMainMenu: () -> Unit,
    onNavigateToLoginChoice: () -> Unit,
    getState: () -> MutableState<UserState>,
    setState: (state: UserState) -> Unit
) {
    val viewModel = MainViewModel(LocalContext.current, setState = { setState(it) })

    var userEmail by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
    var currentUserState by remember { mutableStateOf("") }

    BackHandler {
        setState(UserState.InLoginChoice)
        onNavigateToLoginChoice()
    }

    LaunchedEffect(Unit) {
        setState(UserState.InLogin)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MyOutlinedTextField(
            value = userEmail,
            placeholder = {
                Text(text = "Enter email")
            },
            onValueChange = {
                userEmail = it
            }
        )
        Spacer(modifier = Modifier.padding(8.dp))
        MyOutlinedTextField(
            value = userPassword,
            placeholder = {
                Text(text = "Enter password")
            },
            onValueChange = {
                userPassword = it
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )
        Spacer(modifier = Modifier.padding(8.dp))
        MyOutlinedButton(onClick = {
            viewModel.supabaseAuth.login(
                userEmail,
                userPassword
            )
        }) {
            Text(text = "Log in")
        }

        when (val userState = getState().value) {
            is UserState.LoginOrSignupLoading -> {
                currentUserState = "Loading..."
            }

            is UserState.LoginOrSignupSucceeded -> {
                val message = userState.message
                currentUserState = message
                LaunchedEffect(Unit) {
                    onNavigateToMainMenu()
                }
            }

            is UserState.LoginOrSignupFailed -> {
                val message = userState.message
                currentUserState = message
            }

            else -> {

            }
        }

        if (currentUserState.isNotEmpty()) {
            Text(text = currentUserState)
        }
    }
}