package com.example.supabasedemo.utils

import android.content.Context

/**
 * Helper class to manage Android SharedPreferences operations.
 * Provides simple methods to save, retrieve and clear string data persistently.
 *
 * Usage example:
 * ```
 * val helper = SharedPreferenceHelper(context)
 * helper.saveStringData("user_id", "12345")
 * val userId = helper.getStringData("user_id")
 * ```
 */
class SharedPreferenceHelper(private val context: Context) {

    companion object {
        /**
         * Key used to identify this app's SharedPreferences file
         */
        private const val MY_PREF_KEY = "MY_PREF"
    }

    /**
     * Saves a string value to SharedPreferences.
     * @param key The identifier for the data
     * @param data The string data to save, can be null
     */
    fun saveStringData(key: String, data: String?) {
        val sharedPreferences = context.getSharedPreferences(MY_PREF_KEY, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(key, data).apply()
    }

    /**
     * Retrieves a previously saved string value from SharedPreferences.
     * @param key The identifier of the data to retrieve
     * @return The stored string value, or null if not found
     */
    fun getStringData(key: String): String? {
        val sharedPreferences = context.getSharedPreferences(MY_PREF_KEY, Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, null)
    }

    /**
     * Removes all data stored in this app's SharedPreferences.
     * Use with caution as this cannot be undone.
     */
    fun clearPreferences() {
        val sharedPreferences = context.getSharedPreferences(MY_PREF_KEY, Context.MODE_PRIVATE)
        return sharedPreferences.edit().clear().apply()
    }
}