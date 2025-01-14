package com.example.supabasedemo.compose.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.supabasedemo.R
import com.example.supabasedemo.compose.viewModels.MainViewModel
import com.example.supabasedemo.data.network.SensorManagerSingleton
import com.example.supabasedemo.data.network.UwbManagerSingleton
import com.example.supabasedemo.data.network.getForwardAcceleration
import com.example.supabasedemo.ui.theme.AppTheme
import kotlinx.coroutines.delay

@Composable
fun ArrowView(
    viewModel: MainViewModel,
    getId: () -> Int
) {
    val uwbAngle by UwbManagerSingleton.azimuth.collectAsState()
    val uwbDistance by UwbManagerSingleton.distance.collectAsState()
    val compass by SensorManagerSingleton.compassReadingsFlow.collectAsState()

    val isFront = if (isOtherPhoneStationary()) {
        var refDirection = 0F
        LaunchedEffect(Unit) {
            refDirection = otherPhoneDirection(viewModel, getId)
        }

        val distance1 = uwbDistance
        var distance2: Float = -1F

        LaunchedEffect(Unit) {
            while (true) {
                distance2 = uwbDistance
                if (UwbManagerSingleton.isController) viewModel.supabaseDb.sendDirection(getId(), SensorManagerSingleton.compassReadingsFlow.value.last())
                delay(1000)
            }
        }

        if (isAngleInRange(refDirection, uwbAngle, compass)) {
            if (isDistanceSmaller(distance1, distance2)) {
                !isAccPositive()
            } else {
                isAccPositive()
            }
        } else true
    } else true
    Box(
        modifier = Modifier
            .border(1.dp, AppTheme.colorScheme.outline)
            .size(150.dp, 150.dp),

    ) {
        Image(
            painter = painterResource(R.drawable.arrow),
            "Arrow image",
            Modifier
                .rotate(uwbAngle)
                .fillMaxSize()
                .padding(25.dp)
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Text(text = isFront.toString())
    }
}

fun isDistanceSmaller (
    firstDist: Float,
    secondDist: Float
):Boolean {
    if (firstDist - secondDist >= 0) return true
    return false
}

fun isAngleInRange (
    refDirection: Float,
    uwbAngle: Float,
    devAngle: List<Float>
):Boolean {
    val angleOfMovement = devAngle.takeLast(20).average()
    return if (uwbAngle >= 0) {
        val end = refDirection + 90
        angleOfMovement in refDirection..end
    } else {
        val start = refDirection - 90
        angleOfMovement in start..refDirection
    }
}

fun isAccPositive ():Boolean {
    return getForwardAcceleration() >= 0
}

fun isOtherPhoneStationary ():Boolean {
    return true
}

suspend fun otherPhoneDirection (viewModel: MainViewModel, getKochamGotowac: () -> Int):Float {
    var a: Float = 0F
    viewModel.supabaseRealtime.subscribeToDirection(getKochamGotowac(), onDirectionUpdate = { a = it.direction})
    return a
}
