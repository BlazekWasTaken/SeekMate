package com.example.supabasedemo.data.network

import android.content.Context
import android.util.Log
import com.example.supabasedemo.MainActivity.Companion.DEMO_ROUTE
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

/**
 * Helper class that manages Supabase real-time subscriptions for game state updates.
 * Handles:
 * - Game state synchronization
 * - Player position/direction updates
 * - Game timing and round management
 *
 * @param scope CoroutineScope for managing subscription lifecycles
 * @param setState Callback to update the app's user state
 * @param context Android application context
 */
class SupabaseRealtimeHelper(
    private val scope: CoroutineScope,
    val setState: (UserState) -> Unit,
    private val context: Context,
) {
    /**
     * Subscribes to real-time updates for a specific game session.
     * Handles navigation between game states and UWB session management.
     *
     * @param uuid The unique identifier of the game to monitor
     * @param onGameUpdate Callback invoked when game state changes
     */
    suspend fun subscribeToGame(uuid: String, onGameUpdate: (Game) -> Unit) {
        // Create channel for game updates
        val channel = client.channel("games_channel") {}

        // Configure flow to monitor game changes
        val gameFlow: Flow<Game> = channel.postgresSingleDataFlow(
            schema = "public",
            table = "games",
            primaryKey = Game::uuid
        ) {
            eq("uuid", uuid)
        }

        // Process game updates
        gameFlow.onEach { updatedGame ->
            onGameUpdate(updatedGame)
            Log.e("Supabase-Realtime", "Game updated: $updatedGame")

            // Initialize UWB session when both players have joined
            if (updatedGame.user1 != null && updatedGame.user2 != null && updatedGame.round_no == 1) {
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

            // Handle navigation based on game state
            if (updatedGame.end_time == null) {
                when {
                    // Demo mode
                    updatedGame.round_no == -1 -> {
                        NavControllerProvider.navController.navigate(route = DEMO_ROUTE)
                    }
                    // Waiting for players
                    updatedGame.round_no == 0 -> {
                        Log.e("Supabase-Realtime", "Waiting for players to join...")
                    }
                    // Odd rounds: Controller plays, Controlee waits
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
                    // Even rounds: Controlee plays, Controller waits
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

    /**
     * Subscribes to direction/position updates for a player
     *
     * @param id Player identifier
     * @param onDirectionUpdate Callback for direction changes
     */
    suspend fun subscribeToDirection(id: Int, onDirectionUpdate: (DirectionRecord) -> Unit) {
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

    /**
     * Subscribes to game end time updates
     *
     * @param uuid Game identifier
     * @param onEndTimeUpdate Callback when game end time changes
     */
    suspend fun subscribeToEndTime(uuid: String, onEndTimeUpdate: (Game) -> Unit) {
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