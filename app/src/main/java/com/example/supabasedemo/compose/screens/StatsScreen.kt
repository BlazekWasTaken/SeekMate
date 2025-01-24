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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.supabasedemo.compose.viewModels.MainViewModel
import com.example.supabasedemo.data.model.Game
import com.example.supabasedemo.data.model.UserState
import kotlinx.datetime.Instant
import kotlin.math.floor

/**
 * Statistics screen that displays game performance metrics including:
 * - Win/loss records
 * - Average rounds per game outcome
 * - Average game duration as seeker/hider
 * The data is fetched from Supabase when the screen loads.
 */

// Storage for games where current user was player 1 (seeker) or player 2 (hider)
var games1: List<Game> = ArrayList(0)  // Games as seeker
var games2: List<Game> = ArrayList(0)  // Games as hider

// Basic game statistics
var gamesWon: Int = 0
var gamesLost: Int = 0

// Average rounds statistics
var avgRoundWon: Double = 0.0   // Average rounds for won games
var avgRoundLost: Double = 0.0  // Average rounds for lost games

// Average duration statistics by role and outcome
var avgTimeWonAsSeeker: Double = 0.0
var avgTimeWonAsHider: Double = 0.0
var avgTimeLostAsSeeker: Double = 0.0
var avgTimeLostAsHider: Double = 0.0

/**
 * Main stats screen composable that fetches and displays game statistics
 *
 * @param onNavigateToMainMenu Callback to return to main menu
 * @param setState Updates the global app state
 * @param viewModel Main view model for database access
 */
@Composable
fun StatsScreen(
    onNavigateToMainMenu: () -> Unit,
    setState: (state: UserState) -> Unit,
    viewModel: MainViewModel
) {
    // Fetch and calculate statistics when screen loads
    LaunchedEffect(Unit) {
        setState(UserState.InStats)

        // Fetch games from database
        games1 = viewModel.supabaseDb.getFinishedUser1Games(viewModel.supabaseAuth.getCurrentUser()) {
            setState(UserState.InMainMenu)
        }
        games2 = viewModel.supabaseDb.getFinishedUser2Games(viewModel.supabaseAuth.getCurrentUser()) {
            setState(UserState.InMainMenu)
        }

        // Calculate win/loss totals
        gamesWon = games1.count { it.won == true } + games2.count { it.won == false }
        gamesLost = games1.count { it.won == false } + games2.count { it.won == true }

        // Calculate average rounds for wins/losses
        val roundsWonAsSeeker = games1.filter { it.won == true }.map(Game::round_no)
        val roundsWonAsHider = games2.filter { it.won == false }.map(Game::round_no)
        avgRoundWon = (roundsWonAsSeeker + roundsWonAsHider).average()

        val roundsLostAsSeeker = games1.filter { it.won == false }.map(Game::round_no)
        val roundsLostAsHider = games2.filter { it.won == true }.map(Game::round_no)
        avgRoundLost = (roundsLostAsSeeker + roundsLostAsHider).average()

        // Calculate average game durations
        avgTimeWonAsSeeker = games1.filter { it.won == true }
            .map {(Instant.parse(it.end_time!!) - Instant.parse(it.start_time!!)).inWholeSeconds}
            .average()
        avgTimeWonAsHider = games2.filter { it.won == true }
            .map {(Instant.parse(it.end_time!!) - Instant.parse(it.start_time!!)).inWholeSeconds}
            .average()
        avgTimeLostAsSeeker = games1.filter { it.won == false }
            .map {(Instant.parse(it.end_time!!) - Instant.parse(it.start_time!!)).inWholeSeconds}
            .average()
        avgTimeLostAsHider = games2.filter { it.won == false }
            .map {(Instant.parse(it.end_time!!) - Instant.parse(it.start_time!!)).inWholeSeconds}
            .average()
    }

    // Display statistics in a centered column layout
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
        if (avgRoundWon != 0.0) {
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                textAlign = TextAlign.Center,
                text = "Won after an average of rounds: $avgRoundWon")
        }
        if (avgRoundLost != 0.0) {
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                textAlign = TextAlign.Center,
                text = "Lost after an average of rounds: $avgRoundLost")
        }
        if (avgTimeWonAsSeeker != 0.0) {
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                textAlign = TextAlign.Center,
                text = "Won as seeker after an average of: ${(avgTimeWonAsSeeker/60).toInt()}:${if (floor(avgTimeWonAsSeeker%60) < 10) "0" else ""}${floor(avgTimeWonAsSeeker%60).toInt()}")
        }
        if (avgTimeLostAsSeeker != 0.0) {
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                textAlign = TextAlign.Center,
                text = "Lost as seeker after an average of: ${(avgTimeLostAsSeeker/60).toInt()}:${if (floor(avgTimeLostAsSeeker%60) < 10) "0" else ""}${floor(avgTimeLostAsSeeker%60).toInt()}")
        }
        if (avgTimeWonAsHider != 0.0) {
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                textAlign = TextAlign.Center,
                text = "Won as hider after an average of: ${floor(avgTimeWonAsHider/60).toInt()}:${if (floor(avgTimeWonAsHider%60) < 10) "0" else ""}${floor(avgTimeWonAsHider%60).toInt()}")
        }
        if (avgTimeWonAsSeeker != 0.0) {
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                textAlign = TextAlign.Center,
                text = "Lost as hider after an average of: ${(avgTimeWonAsSeeker/60).toInt()}:${if (floor(avgTimeWonAsSeeker%60) < 10) "0" else ""}${floor(avgTimeWonAsSeeker%60).toInt()}")
        }
        Spacer(modifier = Modifier.padding(8.dp))
    }

    // Handle back button press
    BackHandler {
        setState(UserState.InMainMenu)
        onNavigateToMainMenu()
    }
}
