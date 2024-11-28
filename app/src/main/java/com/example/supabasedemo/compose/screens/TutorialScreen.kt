package com.example.supabasedemo.compose.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.supabasedemo.compose.viewModels.MainViewModel
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.ui.theme.AppTheme
import com.example.supabasedemo.ui.theme.MyOutlinedButton
import com.example.supabasedemo.ui.theme.MyOutlinedTextField
import com.example.supabasedemo.ui.theme.PrimaryContainer
import com.example.supabasedemo.ui.theme.Typography
import kotlinx.datetime.format.Padding

@Composable
fun TutorialScreen(
    onNavigateToMainMenu: () -> Unit,
    getState: () -> MutableState<UserState>,
    setState: (state: UserState) -> Unit
){
    val viewModel = MainViewModel(LocalContext.current, setState = { setState(it) })

    LaunchedEffect(Unit) {
        setState(UserState.InTutorial)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            text = "How to play?",
            style = Typography.titleLarge
        )
        Spacer(modifier = Modifier.padding(12.dp))
        Box(
            modifier = Modifier
                .border(1.dp, AppTheme.colorScheme.outline, RectangleShape)
                .fillMaxWidth()
                .height(600.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
            ) {
                Column(
                    modifier = Modifier
                        .padding(8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "1. Start the game, generate a QR code and let the other player scan it OR scan the QR code generated by the other player",
                        softWrap = true
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        text = "Note: Remember that the person who generates the QR code will be the Seeker and the person scanning will be the Hider.",
                        softWrap = true
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        text = "2. The game will have up to 3 rounds, each with two turns and each turn starting with the Seeker's move",
                        softWrap = true
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        text = "First turn: the Hider will have 30 seconds to move to their hiding place, the Seeker will have 30 seconds to play the mini-game to earn points.",
                        softWrap = true
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        text = "Second turn: the Seeker will have 30 seconds to move to get closer to or find Hider, the Hider will have 30 seconds to play the mini-game to earn points.",
                        softWrap = true
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        text = "Note: the players MUST stay still while playing the mini-game, points will be deducted when movement is detected.",
                        softWrap = true
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        text = "3. The players take turns each round until Hider is found or until the third round is finished.",
                        softWrap = true
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        text = "4. Hider wins if the game ends on the third round and they are not found. Seeker wins if they manage to find the Hider before time runs up.",
                        softWrap = true
                    )
                }
                Spacer(modifier = Modifier.padding(6.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "The mini-game",
                        style = Typography.titleSmall
                    )
                }
                Spacer(modifier = Modifier.padding(4.dp))
                Column(
                    modifier = Modifier
                        .padding(8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "In the mini-game the user can gain points. For the Hider every 10 points are worth one booster, which can be used to extend their hiding time. For the Seeker every 5 points is worth one hint, showing them the proximity and direction of the Hider.",
                        softWrap = true
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        text = "Points are earned by tapping on the green circle in time."
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        text = "Points are deducted if movement of the player is detected or if a red circle is tapped."
                    )
                }
            }
        }
        Spacer(modifier = Modifier.padding(12.dp))
        MyOutlinedButton(
            onClick = {
                setState(UserState.InMainMenu)
            }) {
            Text(text = "Back to Main Menu")
        }

        //TODO: add everywhere going a page back
        BackHandler {
            setState(UserState.InMainMenu)
        }

        val userState = getState().value
        when (userState) {
            is UserState.InMainMenu -> {
                LaunchedEffect(Unit) {
                    onNavigateToMainMenu()
                }
            }
            else -> {}
        }

    }
}