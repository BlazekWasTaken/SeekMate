package com.example.supabasedemo.compose.screens

import android.content.ContentValues.TAG
import android.content.Context
import android.content.res.Resources.getSystem
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.supabasedemo.R
import com.example.supabasedemo.compose.viewModels.MainViewModel
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.data.network.UwbManagerSingleton
import com.example.supabasedemo.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import kotlin.random.Random


data class RedCircle(
    val id: Int,
    val offsetX: Animatable<Float, *> = Animatable(0f),
    val offsetY: Animatable<Float, *> = Animatable(0f),
    var isVisible: Boolean = false
)

data class GreenSquare(
    val id: Int,
    val offsetX: Animatable<Float, *> = Animatable(0f),
    val offsetY: Animatable<Float, *> = Animatable(0f),
    var isVisible: Boolean = false
)

val Int.px: Int get() = (this * getSystem().displayMetrics.density).toInt()

@Composable
fun MinigameScreen(
    onNavigateToEndGame: () -> Unit,
    onNavigateToMainMenu: () -> Unit,
    getState: () -> MutableState<UserState>,
    setState: (state: UserState) -> Unit,
    round: Int = 0,
    gameUuid: String,
    viewModel: MainViewModel
) {
    LaunchedEffect(Unit) {
        setState(UserState.InMiniGame)
    }

    var isMoving by remember { mutableStateOf(false) }
    var latestSensorRead by remember { mutableStateOf(System.currentTimeMillis()) }

    val context = LocalContext.current

    val soundPool = remember {
        SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    }

    var isHitSoundLoaded by remember { mutableStateOf(false) }
    var isMissSoundLoaded by remember { mutableStateOf(false) }

    val hitSoundId = remember { soundPool.load(context, R.raw.hit, 1) }
    val missSoundId = remember { soundPool.load(context, R.raw.miss, 1) }

    soundPool.setOnLoadCompleteListener { _, loadedSoundId, status ->
        when {
            status == 0 && loadedSoundId == hitSoundId -> {
                isHitSoundLoaded = true
            }

            status == 0 && loadedSoundId == missSoundId -> {
                isMissSoundLoaded = true
            }

            else -> {
                Log.e("sound", "Sound $loadedSoundId load failed with status: $status")
            }
        }
    }

    fun playSoundForEntity(isMissClicked: Boolean) {
        if (isMissClicked) {
            if (isMissSoundLoaded) {
                soundPool.play(
                    missSoundId,
                    1f,
                    1f,
                    1,
                    0,
                    1f
                )
            }
        } else {
            if (isHitSoundLoaded) {
                soundPool.play(
                    hitSoundId,
                    1f,
                    1f,
                    1,
                    0,
                    1f
                )
            }
        }
    }


    val totalTime = 30
    var timeLeft by remember { mutableIntStateOf(totalTime) }

    var endTimeSubscription by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }

        if (round == 6) {
            viewModel.supabaseDb.updateEndTime(
                gameUuid,
                onError = {
                    Log.e("a", "Something went wrong")
                }
            )
            //TODO: this user won
            viewModel.supabaseDb.updateWinner(
                gameUuid,
                didUser1Win = false,
                onError = {
                    Log.e("a", "Something went wrong")
                }
            )
            setState(UserState.InEndGame)
            onNavigateToEndGame()
        } else {
            viewModel.supabaseDb.updateRoundNumber(
                gameUuid,
                round + 1,
                onSuccess = {
                    setState(UserState.InMainMenu)
                },
                onError = {
                    setState(UserState.InMainMenu)
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.supabaseRealtime.subscribeToEndTime(
            uuid = gameUuid,
            onEndTimeUpdate = { updateEndTime ->
                endTimeSubscription = updateEndTime.end_time
            }
        )
    }

    //TODO: this user lost
    if (endTimeSubscription != null) {
        LaunchedEffect(Unit) {
            viewModel.supabaseDb.updateWinner(
                gameUuid,
                didUser1Win = !UwbManagerSingleton.isController,
                onError = {
                    Log.e("a", "Something went wrong")
                }
            )
            setState(UserState.InEndGame)
            onNavigateToEndGame()
        }
    }


    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val linearAccelerationSensor =
            sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val acceleration = sqrt(x * x + y * y + z * z)
                    val movementThreshold = 3.0f
                    val movementTimeThreshold = 50
                    if (acceleration > movementThreshold) {
                        if (System.currentTimeMillis() - latestSensorRead <= movementTimeThreshold) {
                            isMoving = true
                        } else {
                            isMoving = false
                        }
                        latestSensorRead = System.currentTimeMillis()
                        Log.i(TAG, "$x $y $z")
                    } else {
                        isMoving = false
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }
        sensorManager.registerListener(
            sensorEventListener,
            linearAccelerationSensor,
            SensorManager.SENSOR_DELAY_GAME
        )
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
            soundPool.release()

        }
    }


    LaunchedEffect(isMoving) {
        while (true) {
            if (isMoving) {
                viewModel.decrementScore()
            }
            delay(1000)
        }
    }

    val circleSize = 50.dp
    val density = LocalDensity.current
    val animationDuration = 1000
    val delayDuration = 1500
    val fadeoutDuration = 500

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.padding(16.dp))
            Text(
                text = "ROUND: $round",
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = "Score: ${viewModel.score.value}",
                fontSize = 24.sp,
                modifier = Modifier.padding(8.dp)
            )

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .fillMaxWidth()
                    .background(AppTheme.colorScheme.surface)
                    .border(2.dp, AppTheme.colorScheme.outline)
            ) {
                val maxWidthPx = (this.maxWidth - circleSize).value.toInt().px
                val maxHeightPx = (this.maxHeight - circleSize - 50.dp).value.toInt().px

                val greenOffsetX = remember { Animatable(Random.nextFloat() * maxWidthPx) }
                val greenOffsetY = remember { Animatable(Random.nextFloat() * maxHeightPx) }

                var isGreenVisible by remember { mutableStateOf(true) }

                val redCircles = remember { mutableStateListOf<RedCircle>() }
                var redCircleId by remember { mutableIntStateOf(0) }

                val greenSquares = remember { mutableStateListOf<GreenSquare>() }
                var greenSquareId by remember { mutableIntStateOf(0) }

                LaunchedEffect(Unit) {
                    redCircles.add(
                        RedCircle(
                            id = redCircleId++,
                            offsetX = Animatable(Random.nextFloat() * maxWidthPx),
                            offsetY = Animatable(Random.nextFloat() * maxHeightPx)
                        )
                    )
                }

                var cycleCount by remember { mutableIntStateOf(0) }

                LaunchedEffect(key1 = "AnimationLoop") {
                    println("Animation loop started")
                    while (true) {
                        cycleCount++

                        redCircles.forEach { it.isVisible = true }
                        greenSquares.forEach { it.isVisible = true }

                        isGreenVisible = true

                        val redAnimationsX = redCircles.map { redCircle ->
                            launch {
                                val targetRedX = Random.nextFloat() * maxWidthPx

                                redCircle.offsetX.animateTo(
                                    targetValue = targetRedX,
                                    animationSpec = tween(durationMillis = animationDuration)
                                )

                            }
                        }

                        val redAnimationsY = redCircles.map { redCircle ->
                            launch {
                                val targetRedY = Random.nextFloat() * maxHeightPx
                                redCircle.offsetY.animateTo(
                                    targetValue = targetRedY,
                                    animationSpec = tween(durationMillis = animationDuration)
                                )

                            }
                        }

                        val greenSquaresAnimationsX = greenSquares.map { greenSquare ->
                            launch {
                                val targetGreenSquareX = Random.nextFloat() * maxWidthPx

                                greenSquare.offsetX.animateTo(
                                    targetValue = targetGreenSquareX,
                                    animationSpec = tween(durationMillis = animationDuration)
                                )

                            }
                        }

                        val greenSquaresAnimationsY = greenSquares.map { greenSquare ->
                            launch {
                                val targetGreenSquareY = Random.nextFloat() * maxHeightPx
                                greenSquare.offsetY.animateTo(
                                    targetValue = targetGreenSquareY,
                                    animationSpec = tween(durationMillis = animationDuration)
                                )

                            }
                        }

                        val targetGreenX = Random.nextFloat() * maxWidthPx
                        val targetGreenY = Random.nextFloat() * maxHeightPx

                        val greenAnimX = launch {
                            greenOffsetX.animateTo(
                                targetValue = targetGreenX,
                                animationSpec = tween(durationMillis = animationDuration)
                            )
                        }
                        val greenAnimY = launch {
                            greenOffsetY.animateTo(
                                targetValue = targetGreenY,
                                animationSpec = tween(durationMillis = animationDuration)
                            )
                        }

                        redAnimationsX.forEach { it.join() }
                        redAnimationsY.forEach { it.join() }

                        greenSquaresAnimationsX.forEach { it.join() }
                        greenSquaresAnimationsY.forEach { it.join() }

                        greenAnimX.join()
                        greenAnimY.join()

                        isGreenVisible = false

                        redCircles.forEach { it.isVisible = false }

                        greenSquares.forEach { it.isVisible = false }

                        if (cycleCount % 2 == 0) {
                            redCircles.add(
                                RedCircle(
                                    id = redCircleId++,
                                    offsetX = Animatable(Random.nextFloat() * maxWidthPx),
                                    offsetY = Animatable(Random.nextFloat() * maxHeightPx)
                                )
                            )
                        }

                        if (cycleCount % 3 == 0) {
                            greenSquares.add(
                                GreenSquare(
                                    id = greenSquareId++,
                                    offsetX = Animatable(Random.nextFloat() * maxWidthPx),
                                    offsetY = Animatable(Random.nextFloat() * maxHeightPx)
                                )
                            )
                        }

                        delay(fadeoutDuration.toLong())

                        greenOffsetX.snapTo(Random.nextFloat() * maxWidthPx)
                        greenOffsetY.snapTo(Random.nextFloat() * maxHeightPx)
                        redCircles.forEach {
                            it.offsetX.snapTo(Random.nextFloat() * maxWidthPx)
                        }
                        redCircles.forEach {
                            it.offsetY.snapTo(Random.nextFloat() * maxHeightPx)
                        }

                        greenSquares.forEach {
                            it.offsetX.snapTo(Random.nextFloat() * maxWidthPx)
                        }

                        greenSquares.forEach {
                            it.offsetY.snapTo(Random.nextFloat() * maxHeightPx)
                        }

                        delay(delayDuration.toLong())
                    }
                }

                val greenOffsetXDp = with(density) { greenOffsetX.value.toDp() }
                val greenOffsetYDp = with(density) { greenOffsetY.value.toDp() }

                redCircles.forEach { redCircle ->
                    val redOffsetXDp = with(density) { redCircle.offsetX.value.toDp() }
                    val redOffsetYDp = with(density) { redCircle.offsetY.value.toDp() }

                    if (redCircle.isVisible) {
                        Image(painter = painterResource(id = R.drawable.bombb),
                            contentDescription = "bomb",
                            modifier = Modifier
                                .size(circleSize)
                                .offset(x = redOffsetXDp, y = redOffsetYDp)
                                .clickable {
                                    playSoundForEntity(true)
                                    viewModel.decrementScore()
                                    redCircle.isVisible = false
                                }
                        )
                    }
                }

                greenSquares.forEach { greenSquare ->
                    val greenSquareOffsetXDp = with(density) { greenSquare.offsetX.value.toDp() }
                    val greenSquareOffsetYDp = with(density) { greenSquare.offsetY.value.toDp() }

                    if (greenSquare.isVisible) {
                        Image(painter = painterResource(id = R.drawable.dynamite),
                            contentDescription = "dynamite",
                            modifier = Modifier
                                .size(circleSize)
                                .offset(x = greenSquareOffsetXDp, y = greenSquareOffsetYDp)
                                .clickable {
                                    playSoundForEntity(true)
                                    viewModel.decrementScore()
                                    greenSquare.isVisible = false
                                }
                        )
                    }
                }

                if (isGreenVisible) {
                    Image(painter = painterResource(id = R.drawable.star),
                        contentDescription = "star",
                        modifier = Modifier
                            .size(circleSize)
                            .offset(x = greenOffsetXDp, y = greenOffsetYDp)
                            .clickable {
                                playSoundForEntity(false)
                                viewModel.incrementScore()
                                isGreenVisible = false
                            }
                    )
                }
            }
            Text(
                text = "Time left: $timeLeft",
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 60.dp)
            )
        }
    }
}
