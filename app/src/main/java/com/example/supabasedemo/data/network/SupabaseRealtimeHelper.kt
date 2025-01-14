package com.example.supabasedemo.data.network

import android.content.Context
import android.util.Log
import com.example.supabasedemo.MainActivity.Demo
import com.example.supabasedemo.MainActivity.MiniGame
import com.example.supabasedemo.MainActivity.Settings
import com.example.supabasedemo.NavControllerProvider
import com.example.supabasedemo.data.model.Game
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.data.network.SupabaseClient.client
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresSingleDataFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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

            if (updatedGame.end_time == null) {
                when {
                    updatedGame.round_no == -1 -> {
                        NavControllerProvider.navController.navigate(route = Demo)
                    }

                    updatedGame.round_no == 0 -> {
                        Log.e("Supabase-Realtime", "Waiting for players to join...")
                    }

                    updatedGame.round_no % 2 == 1 -> {
                        if (UwbManagerSingleton.isController) {
                            NavControllerProvider.navController.navigate(
                                "minigame/${updatedGame.round_no}/${updatedGame.uuid}/${UwbManagerSingleton.isController}"
                            )
                        } else {
                            NavControllerProvider.navController.navigate(
                                "waiting/${updatedGame.uuid}/${updatedGame.round_no}/0/${UwbManagerSingleton.isController}"
                            )
                        }
                    }

                    else -> {
                        if (UwbManagerSingleton.isController) {
                            NavControllerProvider.navController.navigate(
                                "waiting/${updatedGame.uuid}/${updatedGame.round_no}/0/${UwbManagerSingleton.isController}"
                            )
                        } else {
                            NavControllerProvider.navController.navigate(
                                "minigame/${updatedGame.round_no}/${updatedGame.uuid}/${UwbManagerSingleton.isController}"
                            )
                        }
                    }
                }
            }
        }.launchIn(scope)

        channel.subscribe()
    }

    suspend fun subscribeToDirection (id: Int, onDirectionUpdate: (DirectionRecord) -> Unit) {
        val channel = client.channel("distance_sessions"){}
        try {
            val directionFlow: Flow<DirectionRecord> = channel.postgresSingleDataFlow(
                schema = "public",
                table = "distance_sessions",
                primaryKey = DirectionRecord::id
            ) {
                eq("id", id)
            }
            directionFlow.onEach { updateDirection ->
                onDirectionUpdate(updateDirection)

            }.launchIn(scope)
        } catch (_: Exception) {}
        channel.subscribe()
    }

    suspend fun subscribeToEndTime (uuid: String, onEndTimeUpdate: (Game) -> Unit) {
        val channel = client.channel("games"){}
        try {
            val endTimeFlow: Flow<Game> = channel.postgresSingleDataFlow(
                schema = "public",
                table = "games",
                primaryKey = Game::uuid
            ) {
                eq("uuid", uuid)
            }
            endTimeFlow.onEach { updateEndTime ->
                onEndTimeUpdate(updateEndTime)
            }.launchIn(scope)
        } catch (_: Exception) {}
        channel.subscribe()
    }
}