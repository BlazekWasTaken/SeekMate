package com.example.supabasedemo.data.network

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavController
import com.example.supabasedemo.MainActivity
import com.example.supabasedemo.MainActivity.Demo
import com.example.supabasedemo.MainActivity.LoginChoice
import com.example.supabasedemo.MainActivity.MainMenu
import com.example.supabasedemo.MainActivity.MiniGame
import com.example.supabasedemo.MainActivity.Settings
import com.example.supabasedemo.MainActivity.Tutorial
import com.example.supabasedemo.NavControllerProvider
import com.example.supabasedemo.data.model.Game
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.data.network.SupabaseClient.client
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.postgresSingleDataFlow
import io.github.jan.supabase.realtime.selectSingleValueAsFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SupabaseRealtimeHelper(
    private val scope: CoroutineScope,
    val setState: (UserState) -> Unit,
    private val context: Context,
) {
    suspend fun subscribeToGame(uuid: String, onGameUpdate: (Game) -> Unit) {
        val channel = client.channel("games_channel") {}

        val gameFlow: Flow<Game> = channel.postgresSingleDataFlow(
            schema = "public",
            table = "games",
            primaryKey = Game::uuid
        ) {
            eq("uuid", uuid)
        }

        gameFlow.onEach { updatedGame ->
            onGameUpdate(updatedGame)
            Log.e("Supabase-Realtime", "Game updated: $updatedGame")

            if (updatedGame.user1 != null && updatedGame.user2 != null) {
                if (UwbManagerSingleton.isController) {
                    UwbManagerSingleton.startSession(
                        partnerAddress = updatedGame.controlee_address ?: "-5",
                        preamble = "0"
                    )
                } else {
                    UwbManagerSingleton.startSession(
                        partnerAddress = updatedGame.controller_address ?: "-5",
                        preamble = updatedGame.controller_preamble ?: "-5"
                    )
                }
            }

            when (updatedGame.round_no) {
                1 -> {
                    NavControllerProvider.navController.navigate(route = Demo)
                }
                2 -> {
                    NavControllerProvider.navController.navigate(route = MiniGame)
                }
                3 -> {
                    NavControllerProvider.navController.navigate(route = Settings)
                }
            }

        }.launchIn(scope)

        channel.subscribe()
    }
}