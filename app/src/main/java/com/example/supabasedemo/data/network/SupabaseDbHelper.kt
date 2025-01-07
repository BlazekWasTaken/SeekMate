package com.example.supabasedemo.data.network

import android.util.Log
import com.example.supabasedemo.data.model.Game
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.data.network.SupabaseClient.client
import io.github.jan.supabase.postgrest.from
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

    fun sendReadingToDb(
        distance: Double,
        angle: Double,
        stDev: Double,
        accelerometer: List<Reading>,
        gyroscope: List<Reading>,
        isFront: Boolean
    ) {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            try{
                client.from("ml_test").insert(TestData(
                    distance = distance,
                    angle = angle,
                    st_dev = stDev,
                    acc_x = accelerometer.map { it.x.toDouble() }.toDoubleArray(),
                    acc_y = accelerometer.map { it.y.toDouble() }.toDoubleArray(),
                    acc_z = accelerometer.map { it.z.toDouble() }.toDoubleArray(),
                    gyr_x = gyroscope.map { it.x.toDouble() }.toDoubleArray(),
                    gyr_y = gyroscope.map { it.y.toDouble() }.toDoubleArray(),
                    gyr_z = gyroscope.map { it.z.toDouble() }.toDoubleArray(),
                    is_front = isFront
                ))
            } catch (_: Exception) {

            }
        }
    }
}

@Serializable
class TestData(
    val distance: Double,
    val angle: Double,
    val st_dev: Double,
    val acc_x: DoubleArray,
    val acc_y: DoubleArray,
    val acc_z: DoubleArray,
    val gyr_x: DoubleArray,
    val gyr_y: DoubleArray,
    val gyr_z: DoubleArray,
//    val com_x: DoubleArray,
//    val com_y: DoubleArray,
//    val com_z: DoubleArray,
    val is_front: Boolean
)