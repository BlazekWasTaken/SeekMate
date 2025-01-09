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
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.serialization.Serializable

// The NavControllerProvider is singleton object that acts as a global holder for the NavController instance.
object NavControllerProvider {
    @SuppressLint("StaticFieldLeak")
    lateinit var navController: NavController
}

class MainActivity : ComponentActivity() {
    companion object {
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

        viewModel = MainViewModel(this, ::setState)

        setContent {
            AppTheme(
                getThemeChoice = {
                    return@AppTheme _theme.value
                }
            ) {
                Surface {
                    Navigation()
                }
            }
        }
    }

    @Composable
    private fun Navigation() {
        val navController = rememberNavController()
        NavControllerProvider.navController = navController
        NavHost(
            navController = navController,
            startDestination = LoginProcess
        ) {
            composable(
                route = MINIGAME_ROUTE,
                arguments = listOf(
                    navArgument("round") { type = NavType.IntType },
                    navArgument("gameUuid") { type = NavType.StringType },
                    navArgument("isController") { type = NavType.BoolType }
                )
            ) { backStackEntry ->
                MinigameScreen(
                    onNavigateToMainMenu = {
                        navController.popBackStack()
                    },
                    getState = { _userState },
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

                setState(UserState.InWaitingScreen(score, round, gameUuid,
                    backStackEntry.arguments?.getBoolean("isController") ?: false,))

                WaitingScreen(
                    getState = { _userState },
                    setState = { setState(it) },
                    viewModel = viewModel
                )
            }

            navigation<LoginProcess>(startDestination = LoginChoice) {
                composable<LoginChoice> {
                    ChoiceScreen(
                        onNavigateToLogIn = {
                            navController.navigate(route = Login)
                        },
                        onNavigateToSignUp = {
                            navController.navigate(route = Signup)
                        },
                        onNavigateToMainMenu = {
                            navController.navigate(route = MainMenu) {
                                popUpTo(LoginChoice) {
                                    inclusive = true
                                }
                            }
                        },
                        getState = {
                            return@ChoiceScreen _userState
                        },
                        setState = {
                            setState(it)
                        }
                    )
                }
                composable<Login> {
                    LoginScreen(
                        onNavigateToMainMenu = {
                            navController.navigate(route = MainMenu) {
                                popUpTo(MainMenu) {
                                    inclusive = true
                                }
                            }
                        },
                        getState = {
                            return@LoginScreen _userState
                        },
                        setState = {
                            setState(it)
                        }
                    )
                }
                composable<Signup> {
                    SignupScreen(
                        onNavigateToLoginChoice = {
                            navController.popBackStack()
                        },
                        getState = {
                            return@SignupScreen _userState
                        },
                        setState = {
                            setState(it)
                        }
                    )
                }
            }
            navigation<MainMenu>(startDestination = Menu) {
                composable<Menu> {
                    MainMenuScreen(
                        onNavigateToLoginChoice = {
                            navController.navigate(route = LoginChoice) {
                                popUpTo(Menu) {
                                    inclusive = true
                                }
                            }
                        },
                        onNavigateToGame = {
                            navController.navigate(route = Game) {
                                popUpTo(Menu) {
                                    inclusive = true
                                }
                            }
                        },
                        onNavigateToTutorial = {
                            navController.navigate(route = Tutorial)
                        },
                        onNavigateToSettings = {
                            navController.navigate(route = Settings)
                        },
                        onNavigateToStats = {
                            navController.navigate(route = Stats)
                        },
                        onNavigateToMiniGame = {
                            navController.navigate(route = MiniGame)
                        },
                        getState = {
                            return@MainMenuScreen _userState
                        },
                        setState = {
                            setState(it)
                        }
                    )
                }
                composable<Stats> {
                    StatsScreen(
                        onNavigateToMainMenu = {
                            navController.popBackStack()
                        },
                        getState = {
                            return@StatsScreen _userState
                        },
                        setState = {
                            setState(it)
                        }
                    )
                }
                composable<Tutorial> {
                    TutorialScreen(
                        onNavigateToMainMenu = {
                            navController.popBackStack()
                        },
                        getState = {
                            return@TutorialScreen _userState
                        },
                        setState = {
                            setState(it)
                        }
                    )
                }
                composable<MiniGame> {
                    MinigameScreen(
                        onNavigateToMainMenu = {
                            navController.popBackStack()
                        },
                        getState = {
                            return@MinigameScreen _userState
                        },
                        setState = {
                            setState(it)
                        },
                        round = 0,
                        gameUuid = "test-uuid",
                        viewModel = viewModel)
                }
                composable("minigame") {
                    MinigameScreen(
                        onNavigateToMainMenu = {
                            navController.popBackStack()
                        },
                        getState = { _userState },
                        setState = { setState(it) },
                        round = -1,
                        gameUuid = "uuid",
                        viewModel = viewModel
                    )
                }
            }
            navigation<Settings>(startDestination = SettingsMenu) {
                composable<SettingsMenu> {
                    SettingsScreen(
                        onNavigateToMainMenu = {
                            navController.popBackStack()
                        },
                        onNavigateToAccountInfo = {
                            navController.navigate(AccountInfo)
                        },
                        onNavigateToThemeChoice = {
                            navController.navigate(Theme)
                        },
                        onNavigateToDemo = {
                            navController.navigate(Demo)
                        },
                        getState = {
                            return@SettingsScreen _userState
                        },
                        setState = {
                            setState(it)
                        }
                    )
                }
                composable<AccountInfo> {
                    AccountInfoScreen(
                        onNavigateToSettings = {
                            navController.popBackStack()
                        },
                        getState = {
                            return@AccountInfoScreen _userState
                        },
                        setState = {
                            setState(it)
                        }
                    )
                }
                composable<Theme> {
                    ThemeScreen(
                        onNavigateToSettings = {
                            navController.popBackStack()
                        },
                        setTheme = {
                            _theme.value = it
                        },
                        getState = {
                            return@ThemeScreen _userState
                        },
                        setState = {
                            setState(it)
                        }
                    )
                }
                composable<Demo> {
                    UwbScreen(
                        onNavigateToMainMenu = {
                            navController.popBackStack()
                        },
                        getState = {
                            return@UwbScreen _userState
                        },
                        setState = {
                            setState(it)
                        },
                    )
                }
            }
            navigation<Game>(startDestination = GameStart) {
                composable<GameStart> {
                    CreateGameScreen(
                        getState = {
                            return@CreateGameScreen _userState
                        },
                        setState = {
                            setState(it)
                        }
                    )
                }
            }
        }
    }

    // region objects
    @Serializable
    object LoginProcess

    @Serializable
    object LoginChoice

    @Serializable
    object Login

    @Serializable
    object Signup

    @Serializable
    object MainMenu

    @Serializable
    object Menu

    @Serializable
    object Stats

    @Serializable
    object Tutorial

    @Serializable
    object MiniGame

    @Serializable
    object Settings

    @Serializable
    object SettingsMenu

    @Serializable
    object AccountInfo

    @Serializable
    object Theme

    @Serializable
    object Demo

    @Serializable
    object Game

    @Serializable
    object GameStart
    // endregion

    private fun setState(state: UserState) {
        _userState.value = state
//        Toast.makeText(this, _userState.value.toString(), Toast.LENGTH_SHORT).show()
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