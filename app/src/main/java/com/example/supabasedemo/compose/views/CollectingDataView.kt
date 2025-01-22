package com.example.supabasedemo.compose.views

import android.media.AudioAttributes
import android.media.SoundPool
import android.os.CountDownTimer
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.example.supabasedemo.R
import com.example.supabasedemo.compose.viewModels.MainViewModel
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.data.network.UwbManagerSingleton
import com.example.supabasedemo.ui.theme.AppTheme
import com.example.supabasedemo.ui.theme.MyOutlinedButton
import com.example.supabasedemo.ui.theme.MyOutlinedTextField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.log

@Composable
fun CollectingDataView (
    setState: (state: UserState) -> Unit
) {

    val context = LocalContext.current
    val viewModel = MainViewModel(context, setState = { setState(it) })

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val uwbAngleReading by UwbManagerSingleton.angleReadingsFlow.collectAsState()
    val uwbDistanceReading by UwbManagerSingleton.distanceReadingsFlow.collectAsState()

    var physicalDistance by remember { mutableStateOf("") }
    var physicalAngle by remember { mutableStateOf("") }

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

    val timer1 = object: CountDownTimer(10000, 250) {
        override fun onTick(millisUntilFinished: Long) {
            viewModel.supabaseDb.createCollectingData(
                //id,
                physicalAngle.toFloat(),
                physicalDistance.toFloat(),
                uwbAngleReading.last(),
                uwbDistanceReading.last(),
                onError = { errorMessage = it}
            )
            Log.i("collecting", "${uwbAngleReading.last()} ${uwbDistanceReading.last()}" )
        }
        override fun onFinish() {
            Log.i("collecting", "finished" )
            playSoundForEntity(false)
        }
    }
    val timer2 = object: CountDownTimer(3000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            Log.i("collecting", "waiting" )
            playSoundForEntity(true)
        }
        override fun onFinish() {
            timer1.start()
        }
    }

    Box(
        modifier = Modifier.border(1.dp, AppTheme.colorScheme.outline)
    ){
        Column(
            modifier = Modifier
                .padding(4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            MyOutlinedTextField(
                value = physicalDistance,
                placeholder = {
                    Text(text = "Enter measured distance")
                },
                onValueChange = {
                    physicalDistance = it
                }
            )
            Spacer(modifier = Modifier.padding(8.dp))
            MyOutlinedTextField(
                value = physicalAngle,
                placeholder = {
                    Text(text = "Enter measured angle")
                },
                onValueChange = {
                    physicalAngle = it
                }
            )
            Spacer(modifier = Modifier.padding(8.dp))
            MyOutlinedButton(
                onClick = {
                    timer2.start()
                }
            ) { Text(text = "Start") }
        }
    }
}