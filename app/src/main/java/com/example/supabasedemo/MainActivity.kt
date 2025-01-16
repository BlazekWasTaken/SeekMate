package com.example.supabasedemo

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.supabasedemo.compose.screens.AccountInfoScreen
import com.example.supabasedemo.compose.screens.ChoiceScreen
import com.example.supabasedemo.compose.screens.CreateGameScreen
import com.example.supabasedemo.compose.screens.EndGameScreen
import com.example.supabasedemo.compose.screens.LoginScreen
import com.example.supabasedemo.compose.screens.MainMenuScreen
import com.example.supabasedemo.compose.screens.SettingsScreen
import com.example.supabasedemo.compose.screens.MinigameScreen
import com.example.supabasedemo.compose.screens.SignupScreen
import com.example.supabasedemo.compose.screens.StatsScreen
import com.example.supabasedemo.compose.screens.ThemeScreen
import com.example.supabasedemo.compose.screens.TutorialScreen
import com.example.supabasedemo.compose.screens.UwbScreen
import com.example.supabasedemo.compose.screens.WaitingScreen
import com.example.supabasedemo.compose.viewModels.MainViewModel
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.ui.theme.AppTheme
import com.example.supabasedemo.ui.theme.ThemeChoice

object NavControllerProvider {
    @SuppressLint("StaticFieldLeak")
    lateinit var navController: NavController
}

class MainActivity : ComponentActivity() {

    companion object {
        // Subflow (parent) routes:
        const val ROOT_ROUTE = "root"
        const val LOGIN_FLOW_ROUTE = "loginFlow"
        const val MAIN_MENU_FLOW_ROUTE = "mainMenuFlow"
        const val SETTINGS_FLOW_ROUTE = "settingsFlow"
        const val GAME_FLOW_ROUTE = "gameFlow"

        // Auth child routes:
        const val LOGIN_CHOICE_ROUTE = "loginChoice"
        const val LOGIN_ROUTE = "login"
        const val SIGNUP_ROUTE = "signup"

        // Main menu child routes:
        const val MENU_ROUTE = "menu"
        const val STATS_ROUTE = "stats"
        const val TUTORIAL_ROUTE = "tutorial"
        const val MINI_GAME_ROUTE_STATIC = "minigameStatic"
        const val END_GAME_ROUTE = "endGame"

        // Settings child routes:
        const val SETTINGS_MENU_ROUTE = "settingsMenu"
        const val ACCOUNT_INFO_ROUTE = "accountInfo"
        const val THEME_ROUTE = "theme"
        const val DEMO_ROUTE = "demo"

        // Game child routes:
        const val GAME_START_ROUTE = "gameStart"

        // Shared composables that need parameters:
        const val MINIGAME_ROUTE = "minigame/{round}/{gameUuid}/{isController}"
        const val WAITING_ROUTE = "waiting/{gameUuid}/{round}/{score}/{isController}"
    }

    private val permissionRequestCode = 101
    private val _userState = mutableStateOf<UserState>(UserState.InLoginChoice)
    private val _theme = mutableStateOf<ThemeChoice>(ThemeChoice.System)
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Instantiate your view model
        viewModel = MainViewModel(this, ::setState)

        setContent {
            AppTheme(getThemeChoice = { _theme.value }) {
                Surface {
                    Navigation()
                }
            }
        }
    }

    /**
     * Top-level NavHost using a ROOT_ROUTE as the single startDestination;
     * each subflow is defined inside as a separate navigation() block.
     */
    @Composable
    private fun Navigation() {
        val navController = rememberNavController()
        NavControllerProvider.navController = navController

        NavHost(
            navController = navController,
            startDestination = LOGIN_FLOW_ROUTE, // start in the login flow
            route = ROOT_ROUTE
        ) {
            // ---------- LOGIN FLOW ----------
            navigation(
                route = LOGIN_FLOW_ROUTE,
                startDestination = LOGIN_CHOICE_ROUTE
            ) {
                // "Choice" screen
                composable(LOGIN_CHOICE_ROUTE) {
                    ChoiceScreen(
                        onNavigateToLogIn = {
                            navController.navigate(LOGIN_ROUTE)
                        },
                        onNavigateToSignUp = {
                            navController.navigate(SIGNUP_ROUTE)
                        },
                        // Navigate to the main menu flow route if login is skipped
                        onNavigateToMainMenu = {
                            navController.navigate(MAIN_MENU_FLOW_ROUTE) {
                                popUpTo(LOGIN_FLOW_ROUTE) { inclusive = true }
                            }
                        },
                        getState = { _userState },
                        setState = { setState(it) }
                    )
                }
                // "Login" screen
                composable(LOGIN_ROUTE) {
                    LoginScreen(
                        onNavigateToMainMenu = {
                            navController.navigate(MAIN_MENU_FLOW_ROUTE) {
                                popUpTo(LOGIN_FLOW_ROUTE) { inclusive = true }
                            }
                        },
                        onNavigateToLoginChoice = { navController.popBackStack() },
                        getState = { _userState },
                        setState = { setState(it) }
                    )
                }
                // "Signup" screen
                composable(SIGNUP_ROUTE) {
                    SignupScreen(
                        onNavigateToLoginChoice = { navController.popBackStack() },
                        getState = { _userState },
                        setState = { setState(it) }
                    )
                }
            }

            // ---------- MAIN MENU FLOW ----------
            navigation(
                route = MAIN_MENU_FLOW_ROUTE,
                startDestination = MENU_ROUTE
            ) {
                composable(MENU_ROUTE) {
                    MainMenuScreen(
                        // If user logs out, go back to LOGIN_FLOW_ROUTE
                        onNavigateToLoginChoice = {
                            navController.navigate(LOGIN_FLOW_ROUTE) {
                                popUpTo(MAIN_MENU_FLOW_ROUTE) { inclusive = true }
                            }
                        },
                        onNavigateToGame = {
                            navController.navigate(GAME_FLOW_ROUTE) {
                                popUpTo(MAIN_MENU_FLOW_ROUTE) { inclusive = false }
                            }
                        },
                        onNavigateToTutorial = { navController.navigate(TUTORIAL_ROUTE) },
                        onNavigateToSettings = { navController.navigate(SETTINGS_FLOW_ROUTE) },
                        onNavigateToStats = { navController.navigate(STATS_ROUTE) },
                        onNavigateToMiniGame = { navController.navigate(MINI_GAME_ROUTE_STATIC) },
                        onNavigateToEndGame = {
                            navController.navigate(END_GAME_ROUTE) {
                                popUpTo(END_GAME_ROUTE) { inclusive = true }
                            }
                        },
                        getState = { _userState },
                        setState = { setState(it) }
                    )
                }
                composable(STATS_ROUTE) {
                    StatsScreen(
                        onNavigateToMainMenu = { navController.popBackStack() },
                        setState = { setState(it) },
                        viewModel
                    )
                }
                composable(TUTORIAL_ROUTE) {
                    TutorialScreen(
                        onNavigateToMainMenu = { navController.popBackStack() },
                        getState = { _userState },
                        setState = { setState(it) }
                    )
                }
                composable(END_GAME_ROUTE) {
                    EndGameScreen(
                        getState = { _userState },
                        onNavigateToStats = {
                            navController.navigate(STATS_ROUTE) {
                                popUpTo(STATS_ROUTE) { inclusive = false }
                            }
                        },
                        onNavigateToMainMenu = {
                            navController.navigate(MENU_ROUTE) {
                                popUpTo(MENU_ROUTE) { inclusive = false }
                            }
                        },
                        setState = { setState(it) },
                        getGame = {
                            viewModel.supabaseDb.getLastFinishedUserGame(
                                viewModel.supabaseAuth.getCurrentUser()
                            ) { setState(UserState.InMainMenu) }
                        },
                        viewModel = viewModel
                    )
                }
                composable(MINI_GAME_ROUTE_STATIC) {
                    MinigameScreen(
                        onNavigateToEndGame = { navController.navigate(END_GAME_ROUTE) },
                        setState = { setState(it) },
                        round = 0,
                        gameUuid = "test-uuid",
                        viewModel = viewModel
                    )
                }
            }

            // ---------- SETTINGS FLOW ----------
            navigation(
                route = SETTINGS_FLOW_ROUTE,
                startDestination = SETTINGS_MENU_ROUTE
            ) {
                composable(SETTINGS_MENU_ROUTE) {
                    SettingsScreen(
                        onNavigateToMainMenu = { navController.popBackStack() },
                        onNavigateToAccountInfo = { navController.navigate(ACCOUNT_INFO_ROUTE) },
                        onNavigateToThemeChoice = { navController.navigate(THEME_ROUTE) },
                        onNavigateToDemo = { navController.navigate(DEMO_ROUTE) },
                        setState = { setState(it) }
                    )
                }
                composable(ACCOUNT_INFO_ROUTE) {
                    AccountInfoScreen(
                        onNavigateToSettings = { navController.popBackStack() },
                        setState = { setState(it) }
                    )
                }
                composable(THEME_ROUTE) {
                    ThemeScreen(
                        onNavigateToSettings = { navController.popBackStack() },
                        setTheme = { _theme.value = it },
                        setState = { setState(it) }
                    )
                }
                composable(DEMO_ROUTE) {
                    UwbScreen(
                        onNavigateToSettings = { navController.popBackStack() },
                        setState = { setState(it) }
                    )
                }
            }

            // ---------- GAME FLOW ----------
            navigation(
                route = GAME_FLOW_ROUTE,
                startDestination = GAME_START_ROUTE
            ) {
                composable(GAME_START_ROUTE) {
                    CreateGameScreen(
                        getState = { _userState },
                        setState = { setState(it) }
                    )
                }
            }

            // ---------- SHARED PARAM COMPOSABLES (minigame/waiting) ----------
            composable(
                route = MINIGAME_ROUTE,
                arguments = listOf(
                    navArgument("round") { type = NavType.IntType },
                    navArgument("gameUuid") { type = NavType.StringType },
                    navArgument("isController") { type = NavType.BoolType }
                )
            ) { backStackEntry ->
                MinigameScreen(
                    onNavigateToEndGame = { navController.navigate(END_GAME_ROUTE) },
                    setState = { setState(it) },
                    round = backStackEntry.arguments?.getInt("round") ?: 0,
                    gameUuid = backStackEntry.arguments?.getString("gameUuid") ?: "",
                    viewModel = viewModel
                )
            }
            composable(
                route = WAITING_ROUTE,
                arguments = listOf(
                    navArgument("gameUuid") { type = NavType.StringType },
                    navArgument("round") { type = NavType.IntType },
                    navArgument("score") { type = NavType.IntType },
                    navArgument("isController") { type = NavType.BoolType }
                )
            ) { backStackEntry ->
                val gameUuid = backStackEntry.arguments?.getString("gameUuid") ?: ""
                val round = backStackEntry.arguments?.getInt("round") ?: 0
                val score = backStackEntry.arguments?.getInt("score") ?: 0
                setState(
                    UserState.InWaitingScreen(
                        score,
                        round,
                        gameUuid,
                        backStackEntry.arguments?.getBoolean("isController") ?: false
                    )
                )
                WaitingScreen(
                    onNavigateToEndGame = { navController.navigate(END_GAME_ROUTE) },
                    getState = { _userState },
                    setState = { setState(it) },
                    getEndTime = { 0 },
                    gameUuid = gameUuid,
                    viewModel = viewModel
                )
            }
        }
    }

    private fun setState(state: UserState) {
        _userState.value = state
    }

    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                permissionRequestCode
            )
        }
    }
}