package com.example.supabasedemo.data.model

import kotlinx.serialization.Serializable

/**
 * Represents all possible states a user can be in within the application.
 * Used for state management and navigation control.
 *
 * States are grouped into categories:
 * - Authentication (login, signup, logout)
 * - Navigation (current screen position)
 * - Game Flow (waiting, playing, ended)
 * - Loading/Error states
 */
@Serializable
sealed class UserState {
    // --- Game Flow States ---

    /**
     * Represents user in waiting screen between game rounds
     * @param score Current game score
     * @param round Current round number
     * @param gameUuid Unique game identifier
     * @param isController Whether this device controls the game
     */
    data class InWaitingScreen(
        val score: Int,
        val round: Int,
        val gameUuid: String,
        val isController: Boolean
    ) : UserState()

    // --- Authentication States ---
    data object InLoginChoice : UserState()  // Initial auth screen
    data object InLogin : UserState()        // Login form screen
    data object InSignup : UserState()       // Signup form screen

    // --- Auth Loading/Result States ---
    data object LoginOrSignupLoading : UserState()
    data class LoginOrSignupFailed(val message: String) : UserState()
    data class LoginOrSignupSucceeded(val message: String) : UserState()

    data object LogoutLoading : UserState()
    data class LogoutSucceeded(val message: String) : UserState()
    data class LogoutFailed(val message: String) : UserState()

    // --- Auth Check States ---
    data object CheckingLoginStatus : UserState()
    data class CheckedLoginStatusSucceeded(val message: String) : UserState()
    data class CheckedLoginStatusFailed(val message: String) : UserState()

    // --- Navigation States ---
    data object InMainMenu : UserState()    // Main menu screen
    data object Logout : UserState()        // Logout transition
    data object InSettings : UserState()    // Settings menu
    data object InGameCreation : UserState()// New game setup
    data object InStats : UserState()       // Statistics screen
    data object InTutorial : UserState()    // Tutorial screen
    data object InMiniGame : UserState()    // Mini-game screen
    data object InEndGame : UserState()     // Game over screen

    // --- Settings States ---
    data object InAccountInfo : UserState() // Account settings
    data object InThemeChoice : UserState() // Theme settings
    data object InDemo : UserState()        // Demo features

    // --- Game Creation States ---
    data object GameCreated : UserState()   // Game successfully created
    data object CameraOpened : UserState()  // Camera ready for QR scan
    data object QrScanned : UserState()     // QR code successfully scanned
    data object QrScanFailed : UserState()  // QR code scan failed
}