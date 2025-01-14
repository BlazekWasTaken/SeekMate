package com.example.supabasedemo.data.network

import android.util.Log
import com.example.supabasedemo.data.model.Game
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.data.network.SupabaseClient.client
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

class SupabaseDbHelper(
    val setState: (UserState) -> Unit,
) {
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

    fun createDirection (a: Int) {
        val b = DirectionRecord(a, 0F)
        runBlocking {
            try {
                client.from("distance_sessions")
                    .insert(b)
            } catch (_: Exception) { }
        }
    }

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
}

@Serializable
class DirectionRecord (
    val id: Int,
    val direction: Float
)