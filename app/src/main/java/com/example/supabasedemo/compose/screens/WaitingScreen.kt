package com.example.supabasedemo.compose.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.supabasedemo.compose.viewModels.MainViewModel
import com.example.supabasedemo.compose.views.AccelerometerView
import com.example.supabasedemo.compose.views.ArrowView
import com.example.supabasedemo.compose.views.RotationView
import com.example.supabasedemo.compose.views.UwbDataView
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.data.network.SensorManagerSingleton
import com.example.supabasedemo.data.network.UwbManagerSingleton
import com.example.supabasedemo.ui.theme.MyOutlinedButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.internal.wait

@Composable
fun WaitingScreen(
    onNavigateToEndGame: () -> Unit,
    getState: () -> MutableState<UserState>,
    setState: (UserState) -> Unit,
    getEndTime: () -> Int,
    gameUuid: String,
    viewModel: MainViewModel
) {
    val uwbDistance by UwbManagerSingleton.distanceReadingsFlow.collectAsState()

    val userState = getState().value
    val directionId: Int by remember { mutableIntStateOf(0) }

    var notEnoughHints: Boolean by remember { mutableStateOf(false) }
    var isHintVisible: Boolean by remember { mutableStateOf(false) }
    var wait: Boolean by remember { mutableStateOf(false) }

    var endTimeSubscription by remember { mutableStateOf<String?>(null) }

    if (userState !is UserState.InWaitingScreen) return

    //TODO: check if there is end time in the database
    LaunchedEffect(Unit) {
        viewModel.supabaseRealtime.subscribeToEndTime(
            uuid = gameUuid,
            onEndTimeUpdate = { updateEndTime ->
                endTimeSubscription = updateEndTime.end_time
            }
        )
    }

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
        Spacer(modifier = Modifier.padding(8.dp))
        Text(text = "Round: ${userState.round}")
        Spacer(modifier = Modifier.padding(24.dp))
        notEnoughHints = viewModel.score.value < 1
        if (!isHintVisible) {
            if (!notEnoughHints) {
                MyOutlinedButton(onClick = {
                    if (viewModel.score.value >= 1) {
                        viewModel.decrementScore()
                        isHintVisible = true
                        wait = true
                        CoroutineScope(Dispatchers.Main).launch {
                            while (wait) {
                                delay(5000)
                                isHintVisible = false
                                wait = false
                            }
                        }
                    }
                }) {
                    Text(text = "Use hint, ${viewModel.score.value} left")
                }
            }
            if (notEnoughHints) {
                Text(text = "Not enough hints")
            }
        }
        Spacer(modifier = Modifier.padding(24.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,) {
            if(isHintVisible) {
                ArrowView(viewModel, getId = { return@ArrowView directionId })
                Spacer(modifier = Modifier.padding(10.dp, 0.dp))
                UwbDataView()
            }
        }
    }
    if (uwbDistance.takeLast(10).count {it < 1} == 10 && (userState.round%2 == 0)) {
        viewModel.supabaseDb.updateEndTime(
            gameUuid,
            onError = {
                Log.e("a", "Something went wrong")
            }
        )
        setState(UserState.InEndGame)
        onNavigateToEndGame()
    }
    if (endTimeSubscription != null) {
        setState(UserState.InEndGame)
        onNavigateToEndGame()
    }
}