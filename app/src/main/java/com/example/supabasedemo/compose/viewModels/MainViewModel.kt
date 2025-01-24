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

/**
 * Primary ViewModel that manages:
 * - Authentication state via Supabase
 * - Database operations
 * - Realtime updates/subscriptions
 * - Game score tracking
 */

class MainViewModel(
    context: Context,
    setState: (state: UserState) -> Unit
) : ViewModel() {
    // Supabase service helpers for auth, database and realtime updates
    val supabaseAuth: SupabaseAuthHelper =
        SupabaseAuthHelper(viewModelScope, setState = { setState(it) }, context)
    val supabaseDb: SupabaseDbHelper =
        SupabaseDbHelper(viewModelScope, setState = { setState(it) })
    val supabaseRealtime: SupabaseRealtimeHelper =
        SupabaseRealtimeHelper(viewModelScope, setState = { setState(it) }, context)

    // Game score state management
    private val _score = mutableIntStateOf(0)
    val score: State<Int> get() = _score

    /**
     * Increases the game score by 1 point
     */
    fun incrementScore() {
        _score.value += 1
    }

    /**
     * Decreases the game score by 1 point, preventing negative scores
     */
    fun decrementScore() {
        if (_score.intValue > 0) {
            _score.value -= 1
        }
    }
}