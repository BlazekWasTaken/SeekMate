package com.example.supabasedemo.compose.viewModels

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.data.network.SupabaseAuthHelper
import com.example.supabasedemo.data.network.SupabaseDbHelper
import com.example.supabasedemo.data.network.SupabaseRealtimeHelper

class MainViewModel(
    context: Context,
    setState: (state: UserState) -> Unit
) : ViewModel() {
    val supabaseAuth: SupabaseAuthHelper =
        SupabaseAuthHelper(viewModelScope, setState = { setState(it) }, context)
    val supabaseDb: SupabaseDbHelper =
        SupabaseDbHelper(setState = { setState(it) })
    val supabaseRealtime: SupabaseRealtimeHelper =
        SupabaseRealtimeHelper(viewModelScope, setState = { setState(it) }, context)

    private val _score = mutableIntStateOf(0)
    val score: State<Int> get() = _score

    fun incrementScore() {
        _score.value += 1
    }

    fun decrementScore() {
        if (_score.intValue > 0) {
            _score.value -= 1
        }
    }
}