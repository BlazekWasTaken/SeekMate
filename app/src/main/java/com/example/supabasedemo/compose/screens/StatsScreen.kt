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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.supabasedemo.compose.viewModels.MainViewModel
import com.example.supabasedemo.data.model.Game
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.ui.theme.MyOutlinedButton
import com.example.supabasedemo.ui.theme.ThemeChoice
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.format.char
import java.text.SimpleDateFormat
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.floor

var games1: List<Game> = ArrayList(0)
var games2: List<Game> = ArrayList(0)

var gamesWon: Int = 0
var gamesLost: Int = 0

var avgRoundWon: Double = 0.0
var avgRoundLost: Double = 0.0

var avgTimeWonAsSeeker: Double = 0.0
var avgTimeWonAsHider: Double = 0.0
var avgTimeLostAsSeeker: Double = 0.0
var avgTimeLostAsHider: Double = 0.0

@Composable
fun StatsScreen(
    onNavigateToMainMenu: () -> Unit,
    getState: () -> MutableState<UserState>,
    setState: (state: UserState) -> Unit,
    viewModel: MainViewModel
) {
    LaunchedEffect(Unit) {
        setState(UserState.InStats)
        games1 = viewModel.supabaseDb.getFinishedUser1Games(viewModel.supabaseAuth.getCurrentUser()) {
            setState(
                UserState.InMainMenu
            )
        }
        games2 = viewModel.supabaseDb.getFinishedUser2Games(viewModel.supabaseAuth.getCurrentUser()) {
            setState(
                UserState.InMainMenu
            )
        }
        gamesWon = games1.count { it.won == true } + games2.count { it.won == false }
        gamesLost = games1.count { it.won == false } + games2.count { it.won == true }

        val roundsWonAsSeeker = games1.filter { it.won == true }.map(Game::round_no)
        val roundsWonAsHider = games2.filter { it.won == false }.map(Game::round_no)
        avgRoundWon = (roundsWonAsSeeker + roundsWonAsHider).average()

        val roundsLostAsSeeker = games1.filter { it.won == false }.map(Game::round_no)
        val roundsLostAsHider = games2.filter { it.won == true }.map(Game::round_no)
        avgRoundLost = (roundsLostAsSeeker + roundsLostAsHider).average()

        avgTimeWonAsSeeker = games1.filter { it.won == true }.map {(Instant.parse(it.end_time!!) - Instant.parse(it.start_time!!)).inWholeSeconds}.average()
        avgTimeWonAsHider = games2.filter { it.won == true }.map {(Instant.parse(it.end_time!!) - Instant.parse(it.start_time!!)).inWholeSeconds}.average()

        avgTimeLostAsSeeker = games1.filter { it.won == false }.map {(Instant.parse(it.end_time!!) - Instant.parse(it.start_time!!)).inWholeSeconds}.average()
        avgTimeLostAsHider = games2.filter { it.won == false }.map {(Instant.parse(it.end_time!!) - Instant.parse(it.start_time!!)).inWholeSeconds}.average()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(textAlign = TextAlign.Center, text = "Games won: $gamesWon")
        Spacer(modifier = Modifier.padding(8.dp))
        Text(textAlign = TextAlign.Center, text = "Games lost: $gamesLost")
        if (avgRoundWon == 0.0) {
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                textAlign = TextAlign.Center,
                text = "Won after an average of rounds: $avgRoundWon")
        }
        if (avgRoundLost == 0.0) {
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                textAlign = TextAlign.Center,
                text = "Lost after an average of rounds: $avgRoundLost")
        }
        if (avgTimeWonAsSeeker == 0.0) {
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                textAlign = TextAlign.Center,
                text = "Won as seeker after an average of: ${(avgTimeWonAsSeeker/60).toInt()}:${if (floor(avgTimeWonAsSeeker%60) < 10) "0" else ""}${floor(avgTimeWonAsSeeker%60).toInt()}")
        }
        if (avgTimeLostAsSeeker == 0.0) {
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                textAlign = TextAlign.Center,
                text = "Lost as seeker after an average of: ${(avgTimeLostAsSeeker/60).toInt()}:${if (floor(avgTimeLostAsSeeker%60) < 10) "0" else ""}${floor(avgTimeLostAsSeeker%60).toInt()}")
        }
        if (avgTimeWonAsHider == 0.0) {
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                textAlign = TextAlign.Center,
                text = "Won as hider after an average of: ${floor(avgTimeWonAsHider/60).toInt()}:${if (floor(avgTimeWonAsHider%60) < 10) "0" else ""}${floor(avgTimeWonAsHider%60).toInt()}")
        }
        if (avgTimeWonAsSeeker == 0.0) {
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                textAlign = TextAlign.Center,
                text = "Lost as hider after an average of: ${(avgTimeWonAsSeeker/60).toInt()}:${if (floor(avgTimeWonAsSeeker%60) < 10) "0" else ""}${floor(avgTimeWonAsSeeker%60).toInt()}")
        }
        Spacer(modifier = Modifier.padding(8.dp))
        MyOutlinedButton(
            onClick = {
                setState(UserState.InMainMenu)
            }) {
            Text(text = "Back to Main Menu")
        }
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
