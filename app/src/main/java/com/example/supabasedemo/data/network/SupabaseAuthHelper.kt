package com.example.supabasedemo.data.network

import android.content.Context
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.data.network.SupabaseClient.client
import com.example.supabasedemo.utils.SharedPreferenceHelper
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Helper class to manage Supabase authentication operations including:
 * - User signup/login/logout
 * - Session management
 * - Token storage
 * - Login state tracking
 *
 * Requires:
 * - Supabase client configuration
 * - CoroutineScope for async operations
 * - Context for shared preferences access
 */

class SupabaseAuthHelper(
    private val scope: CoroutineScope,
    val setState: (UserState) -> Unit,
    private val context: Context
) {
    // --- Authentication Operations ---

    /**
     * Creates a new user account with email/password
     * @param userEmail User's email address
     * @param userPassword User's password
     * @param username Display name for the user
     * Updates state with loading/success/error
     */
    fun signUp(userEmail: String, userPassword: String, username: String) {
        scope.launch {
            try {
                setState(UserState.LoginOrSignupLoading)
                client.auth.signUpWith(Email) {
                    email = userEmail
                    password = userPassword
                    data = buildJsonObject {
                        put("email", userEmail)
                        put("username", username)
                    }
                }
                saveToken()
                setState(UserState.LoginOrSignupSucceeded("Registered successfully!"))
            } catch (e: Exception) {
                setState(UserState.LoginOrSignupFailed(e.message ?: ""))
            }
        }
    }

    // --- Token Management ---

    /**
     * Stores the current access token in SharedPreferences
     */
    private fun saveToken() {
        scope.launch {
            val accessToken = client.auth.currentAccessTokenOrNull()
            val sharedPref = SharedPreferenceHelper(context)
            sharedPref.saveStringData("accessToken", accessToken)
        }
    }

    /**
     * Retrieves stored access token
     * @return Stored token or null if not found
     */
    private fun getToken(): String? {
        val sharedPref = SharedPreferenceHelper(context)
        return sharedPref.getStringData("accessToken")
    }

    /**
     * Authenticates existing user with email/password
     * @param userEmail User's email address
     * @param userPassword User's password
     * Updates state with loading/success/error
     */
    fun login(userEmail: String, userPassword: String) {
        scope.launch {
            try {
                setState(UserState.LoginOrSignupLoading)
                client.auth.signInWith(Email) {
                    email = userEmail
                    password = userPassword
                }
                saveToken()
                setState(UserState.LoginOrSignupSucceeded("Logged in successfully!"))
            } catch (e: Exception) {
                setState(UserState.LoginOrSignupFailed(e.message ?: ""))
            }
        }
    }

    /**
     * Signs out current user and clears stored credentials
     * Updates state with loading/success/error
     */
    fun logout() {
        val sharedPref = SharedPreferenceHelper(context)
        scope.launch {
            try {
                setState(UserState.LogoutLoading)
                client.auth.signOut()
                sharedPref.clearPreferences()
                setState(UserState.LogoutSucceeded("Logged out successfully!"))
            } catch (e: Exception) {
                setState(UserState.LogoutFailed(e.message ?: ""))
            }
        }
    }

    // --- Session Status ---

    /**
     * Checks if user has valid session
     * Attempts token refresh if session exists
     * Updates state with current login status
     */
    fun isUserLoggedIn() {
        scope.launch {
            try {
                setState(UserState.CheckingLoginStatus)
                val token = getToken()
                if (token.isNullOrEmpty()) {
                    setState(UserState.CheckedLoginStatusSucceeded("User not logged in!"))
                } else {
                    client.auth.retrieveUser(token)
                    client.auth.refreshCurrentSession()
                    saveToken()
                    setState(UserState.CheckedLoginStatusSucceeded("User already logged in!"))
                }
            } catch (e: RestException) {
                setState(UserState.CheckedLoginStatusFailed(e.error))
            }
        }
    }

    /**
     * Gets current user's metadata if logged in
     * @return User metadata as JsonObject or null if not logged in
     */
    fun getCurrentUser(): JsonObject? {
        val user = client.auth.currentUserOrNull()
        val metadata = user?.userMetadata
        return metadata
    }

    /**
     * Gets complete user info for current session
     * @return UserInfo object containing profile data
     * @throws Exception if no active session exists
     */
    fun getCurrentUserInfo(): UserInfo {
        return runBlocking {
            return@runBlocking client.auth.retrieveUserForCurrentSession(false)
        }
    }
}