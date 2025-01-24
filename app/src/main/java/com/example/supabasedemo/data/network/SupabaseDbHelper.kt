package com.example.supabasedemo.data.network

import android.util.Log
import com.example.supabasedemo.data.model.Game
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.data.network.SupabaseClient.client
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Helper class for interacting with Supabase database.
 * Handles:
 * - Game CRUD operations
 * - Player statistics tracking
 * - Direction and sensor data collection
 * - Error handling and state management
 */

class SupabaseDbHelper(
    private val scope: CoroutineScope,
    val setState: (UserState) -> Unit,
) {
    // ----- Game Query Operations -----

    /**
     * Retrieves all finished games where user was player 1
     * @param currentUser The authenticated user's data
     * @param onError Callback for error handling
     * @return List of completed games
     */
    fun getFinishedUser1Games(
        currentUser: JsonObject?,
        onError: (String) -> Unit
    ): List<Game> {
        val user1 = currentUser?.get("sub").toString().trim().replace("\"", "")
        var games: List<Game> = ArrayList(0)
        runBlocking {
            try {
                games = client.from("games").select {
                    filter {
                        Game::user1 eq user1
                        filterNot("end_time", FilterOperator.IS, "NULL")
                    }
                }.decodeList<Game>()
            } catch (e: Exception) {
                Log.e("supabase", e.message!!)
                onError(e.message ?: "Unexpected error occurred.")
            }
        }
        return games
    }

    /**
     * Retrieves all finished games where user was player 2
     */
    fun getFinishedUser2Games(
        currentUser: JsonObject?,
        onError: (String) -> Unit
    ): List<Game> {
        val user2 = currentUser?.get("sub").toString().trim().replace("\"", "")
        var games: List<Game> = ArrayList(0)
        runBlocking {
            try {
                games = client.from("games").select {
                    filter {
                        Game::user2 eq user2
//                        exact("end_time", null)
                        filterNot("end_time", FilterOperator.IS, "NULL")
                    }
                }.decodeList<Game>()
            } catch (e: Exception) {
                Log.e("supabase", e.message!!)
                onError(e.message ?: "Unexpected error occurred.")
            }
        }
        return games
    }

    /**
     * Gets the most recently completed game for a user
     */
    fun getLastFinishedUserGame(
        currentUser: JsonObject?,
        onError: (String) -> Unit
    ): Game {
        val user = currentUser?.get("sub").toString().trim().replace("\"", "")
        lateinit var game: Game
        runBlocking {
            try {
                game = client.from("games").select {
                    filter {
                        or {
                            Game::user1 eq user
                            Game::user2 eq user
                        }
                        filterNot("end_time", FilterOperator.IS, "NULL")
                    }
                    order(column = "end_time", order = Order.DESCENDING)
                }.decodeSingle()
            } catch (e: Exception) {
                Log.e("supabase", e.message!!)
                onError(e.message ?: "Unexpected error occurred.")
            }
        }
        return game
    }

    // ----- Game Update Operations -----

    /**
     * Updates the current round number for an active game
     */
    fun updateRoundNumber(
        gameUuid: String,
        newRound: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) = runBlocking {
        try {
            client.from("games").update(
                {
                    Game::round_no setTo newRound
                }
            ) {
                select()
                filter {
                    Game::uuid eq gameUuid
                }
            }
            onSuccess()
        } catch (e: Exception) {
            onError(e.message ?: "Unexpected error.")
        }
    }

    /**
     * Sets the completion time when a game ends
     */
    fun updateEndTime(
        gameUuid: String,
        onError: (String) -> Unit
    ) = runBlocking {
        try {
            client.from("games").update(
                {
                    Game::end_time setTo Clock.System.now().toLocalDateTime(timeZone = TimeZone.currentSystemDefault()).toString()
                }
            ){
                select()
                filter {
                    Game::uuid eq gameUuid
                }
            }
        } catch (e: Exception){
            onError(e.message ?: "Something went wrong")
        }
    }

    /**
     * Records the winner of a completed game
     */
    fun updateWinner(
        gameUuid: String,
        didUser1Win: Boolean,
        onError: (String) -> Unit
    ) = runBlocking {
        try{
            client.from("games").update(
                {
                    Game::won setTo didUser1Win
                }
            ){
                select()
                filter {
                    Game::uuid eq gameUuid
                }
            }
        } catch (e: Exception) {
            onError(e.message ?: "Something went wrong")
        }
    }

    /**
     * Allows a second player to join an existing game
     */
    fun joinGameInSupabase(
        gameUuid: String,
        onGameJoined: (Game) -> Unit,
        onError: (String) -> Unit,
        currentUser: JsonObject?,
        controleeAddress: String,
        ) {
        val user2Uuid = currentUser?.get("sub").toString().trim().replace("\"", "")

        runBlocking {
            try {
                val updatedGame =
                    client.from("games").update(
                        {
                            Game::user2 setTo user2Uuid
                            Game::controlee_address setTo controleeAddress
                            Game::round_no setTo 1
                        }
                    ) {
                        select()
                        filter {
                            Game::uuid eq gameUuid
                        }
                    }.decodeSingle<Game>()

                onGameJoined(updatedGame)
            } catch (e: Exception) {
                onError(e.message ?: "Unexpected error occurred.")
            }
        }
    }

    /**
     * Creates a new game instance in the database
     */
    fun createGameInSupabase(
        gameUuid: String,
        onGameCreated: (Game) -> Unit,
        onError: (String) -> Unit,
        currentUser: JsonObject?,
        controllerAddress: String,
        controllerPreamble: String
    ): Game {
        val gameData = Game(
            uuid = gameUuid,
            user1 = currentUser?.get("sub").toString().trim().replace("\"", ""),
            controller_address = controllerAddress,
            controller_preamble = controllerPreamble
        )

        runBlocking {
            try {
                val createdGame = client.from("games")
                    .insert(gameData) {
                        select()
                    }.decodeSingle<Game>()

                onGameCreated(createdGame)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.d("Supabase", "catch (e: Exception: $e")
                    onError(e.message ?: "Unexpected error occurred.")
                }
            }
        }

        return gameData
    }

    // ----- Sensor Data Collection -----

    /**
     * Creates a new direction measurement record
     */
    fun createDirection (a: Int) {
        val b = DirectionRecord(a, 0F)
        runBlocking {
            try {
                client.from("distance_sessions")
                    .insert(b)
            } catch (_: Exception) { }
        }
    }

    /**
     * Updates a direction measurement
     */
    fun sendDirection (id: Int, direction: Float) {
        runBlocking {
            try {
                client.from("distance_sessions").update(
                    {
                        DirectionRecord::direction setTo direction
                    }
                ) {
                    select()
                    filter { DirectionRecord::id eq id }
                }
                Log.e("direction", "try $id")
            } catch (e: Exception){
                Log.e("direction","$id");
            }
        }
    }

    /**
     * Records physical and UWB sensor measurements
     */
    fun createCollectingData(
//        id: Int,
        physicalAngle: Float,
        physicalDistance: Float,
        uwbAngle: Float,
        uwbDistance: Float,
        onError: (String) -> Unit,
    ) {
        val data = DataRecord(
//            id,
            physicalAngle,
            physicalDistance,
            uwbAngle,
            uwbDistance
        )

        scope.launch {
            try {
                 client.from("data_for_report")
                    .insert(data) {
                        select()
                    }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.d("Supabase", "catch (e: Exception: $e")
                    onError(e.message ?: "Unexpected error occurred.")
                }
            }
        }
    }
}

/**
 * Data class representing a direction measurement
 */
@Serializable
class DirectionRecord (
    val id: Int,
    val direction: Float
)

/**
 * Data class for storing sensor measurement comparisons
 */
@Serializable
class DataRecord (
//    val id: Int,
    val physical_angle: Float,
    val physical_distance: Float,
    val uwb_angle: Float,
    val uwb_distance: Float
)