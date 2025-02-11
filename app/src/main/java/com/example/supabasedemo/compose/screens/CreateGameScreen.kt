package com.example.supabasedemo.compose.screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.supabasedemo.compose.viewModels.MainViewModel
import com.example.supabasedemo.compose.views.QRCodeScanner
import com.example.supabasedemo.data.model.Game
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.data.network.UwbManagerSingleton
import com.example.supabasedemo.ui.theme.MyOutlinedButton
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.util.UUID

/**
 * Screen for creating or joining a game session.
 * Handles:
 * - Game creation with QR code generation
 * - QR code scanning to join games
 * - UWB device setup and permissions
 * - Real-time game state updates
 */

@Composable
fun CreateGameScreen(
    getState: () -> MutableState<UserState>,     // Provides current app state
    onNavigateToMainMenu: () -> Unit,            // Navigation callback
    setState: (state: UserState) -> Unit         // Updates app state
) {
    // Handle back button press
    BackHandler {
        setState(UserState.InMainMenu)
        onNavigateToMainMenu()
    }

    // Core state management
    val context = LocalContext.current
    val viewModel = MainViewModel(context, setState = { setState(it) })

    // QR code and game joining states
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var scannedGameUuid by remember { mutableStateOf<String?>(null) }
    var scannedDeviceAddress by remember { mutableStateOf<String?>(null) }
    var scannedDevicePreamble by remember { mutableStateOf<String?>(null) }

    // Game session states
    var gameDetails by remember { mutableStateOf<Game?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // UWB device states
    val deviceAddress by UwbManagerSingleton.address.collectAsState(initial = "-1")
    val devicePreamble by UwbManagerSingleton.preamble.collectAsState(initial = "-1")

    // Permission handling
    var permissionGranted by remember { mutableStateOf(false) }

    // Real-time game subscription state
    var gameSubscription by remember { mutableStateOf<Game?>(null) }

    // Initial setup effect
    LaunchedEffect(Unit) {
        setState(UserState.InGameCreation)
        // Check/request UWB permissions
        permissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.UWB_RANGING
        ) == PackageManager.PERMISSION_GRANTED
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.UWB_RANGING),
                101
            )
        }

        viewModel.supabaseAuth.isUserLoggedIn()
        UwbManagerSingleton.initialize(context, true)
    }

    // Subscribe to game updates when game is created/joined
    if (gameDetails != null) {
        LaunchedEffect(gameDetails!!.uuid) {
            viewModel.supabaseRealtime.subscribeToGame(
                uuid = gameDetails!!.uuid,
                onGameUpdate = { updatedGame ->
                    gameSubscription = updatedGame
                }
            )
        }
    }

    // UI Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        gameSubscription?.let { game ->
            when (game.round_no) {
                0 -> Text("Waiting for players to join...")
                1 -> Text("Round 1: Game Started!")
                2 -> Text("Round 2: Continue the game!")
                3 -> Text("Round 3: Final round!")
            }
        } ?: Text("No game subscription yet.")

        Spacer(modifier = Modifier.padding(16.dp))
        MyOutlinedButton(
            onClick = {
                val gameUuid = UUID.randomUUID().toString()
                val bitmap = generateQRCode(gameUuid, deviceAddress, devicePreamble)
                gameDetails = viewModel.supabaseDb.createGameInSupabase(
                    gameUuid,
                    onGameCreated = {
                        runBlocking {
                            delay(1000)
                        }
                        qrCodeBitmap = bitmap ?: run {
                            errorMessage = "Error generating QR code"
                            return@createGameInSupabase
                        }
                        setState(UserState.GameCreated)
                    },
                    onError = { errorMessage = it },
                    currentUser = viewModel.supabaseAuth.getCurrentUser(),
                    controllerAddress = deviceAddress,
                    controllerPreamble = devicePreamble
                )
            }
        ) {
            Text("Generate QR Code aka Create Game")
        }
        Spacer(modifier = Modifier.padding(16.dp))
        MyOutlinedButton(
            onClick = {
                setState(UserState.CameraOpened)
                UwbManagerSingleton.setRoleAsController(false, context)
            }
        ) {
            Text("Join Game")
        }
        Spacer(modifier = Modifier.padding(16.dp))

        val userState = getState().value
        when (userState) {
            is UserState.GameCreated -> {
                Image(
                    painter = BitmapPainter(qrCodeBitmap!!.asImageBitmap()),
                    contentDescription = "QR Code",
                    modifier = Modifier.size(200.dp)
                )
            }

            is UserState.CameraOpened -> {
                QRCodeScanner(
                    onScanSuccess = { gameUuid, scannedAddress, scannedPreamble ->
                        scannedGameUuid = gameUuid
                        scannedDeviceAddress = scannedAddress
                        scannedDevicePreamble = scannedPreamble
                        Log.d("QRCodeScanner", "Scanned Data - Game UUID: $gameUuid, Address: $deviceAddress, Preamble: $devicePreamble")

                        viewModel.supabaseDb.joinGameInSupabase(
                            gameUuid = gameUuid,
                            onGameJoined = { game ->
                                gameDetails = game
                                Log.d("QRCodeScanner", "Joined game: $game")
                            },
                            onError = { errorMessage = it },
                            currentUser = viewModel.supabaseAuth.getCurrentUser(),
                            controleeAddress = deviceAddress,
                        )

                        setState(UserState.QrScanned)
                    },
                    onScanError = {
                        setState(UserState.QrScanFailed)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            is UserState.QrScanned -> {
                Spacer(modifier = Modifier.padding(16.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Game Details")
                    Text("Game UUID: ${gameDetails!!.uuid}")
                    Text("User 1: ${gameDetails!!.user1}")
                    Text("User 2: ${gameDetails!!.user2 ?: "Waiting for player"}")
                    Text("Round: ${gameDetails!!.round_no}")
                    Text("Start Time: ${gameDetails!!.start_time ?: "Not started yet"}")
                    Text("End Time: ${gameDetails!!.end_time ?: "Not ended yet"}")
                    Text("Winner: ${gameDetails!!.won?.let { if (it) "User 1" else "User 2" } ?: "TBD"}")
                }
            }

            is UserState.QrScanFailed -> {
                Text("Error: $errorMessage", color = Color.Red)
            } else -> {

            }
        }
    }
}

/**
 * Generates a QR code containing game session details
 * @param gameUuid Unique identifier for the game session
 * @param deviceAddress UWB device address of the game creator
 * @param devicePreamble UWB preamble code of the game creator
 * @return Bitmap containing the QR code or null if generation fails
 */
private fun generateQRCode(gameUuid: String, deviceAddress: String, devicePreamble: String): Bitmap? {
    val qrData = JSONObject().apply {
        put("game_uuid", gameUuid)
        put("device_address", deviceAddress)
        put("device_preamble", devicePreamble)
    }.toString()

    val writer = QRCodeWriter()
    return try {
        Log.d("QRCode", "Attempting to encode QR code for qrData: $qrData")

        val bitMatrix = writer.encode(qrData, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height

        Log.d("QRCode", "QR code BitMatrix created with dimensions: $width x $height")

        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                )
            }
        }
        Log.d("QRCode", "QR code Bitmap successfully generated")

        bmp
    } catch (e: WriterException) {
        Log.e("QRCode", "Failed to generate QR code: ${e.message}")
        e.printStackTrace()
        null
    } catch (e: Exception) {
        Log.e("QRCode", "Unexpected error: ${e.message}")
        e.printStackTrace()
        null
    }
}