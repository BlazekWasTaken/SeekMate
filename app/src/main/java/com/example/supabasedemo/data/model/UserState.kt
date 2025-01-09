package com.example.supabasedemo.data.model

import kotlinx.serialization.Serializable

@Serializable
sealed class UserState {
    data class InWaitingScreen(val score: Int, val round: Int, val gameUuid: String, val isController: Boolean) : UserState()

    data object InLoginChoice : UserState()
    data object InLogin : UserState()
    data object InSignup : UserState()

    data object LoginOrSignupLoading : UserState()
    data class LoginOrSignupFailed(val message: String) : UserState()
    data class LoginOrSignupSucceeded(val message: String) : UserState()

    data object LogoutLoading : UserState()
    data class LogoutSucceeded(val message: String) : UserState()
    data class LogoutFailed(val message: String) : UserState()

    data object CheckingLoginStatus : UserState()
    data class CheckedLoginStatusSucceeded(val message: String) : UserState()
    data class CheckedLoginStatusFailed(val message: String) : UserState()

    data object InMainMenu : UserState()
    data object Logout : UserState()
    data object InSettings : UserState()
    data object InGameCreation : UserState()
    data object InStats : UserState()
    data object InTutorial : UserState()
    data object InMiniGame : UserState()

    data object InAccountInfo : UserState()
    data object InSounds : UserState()
    data object InThemeChoice : UserState()
    data object InDemo : UserState()

    data object GameCreated : UserState()
    data object CameraOpened : UserState()
    data object QrScanned : UserState()
    data object QrScanFailed : UserState()
}