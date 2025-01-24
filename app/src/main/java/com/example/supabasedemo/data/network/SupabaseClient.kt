package com.example.supabasedemo.data.network

import com.example.supabasedemo.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

/**
 * Singleton object that provides a centralized Supabase client configuration.
 * This client handles authentication, database operations, and realtime subscriptions.
 */
object SupabaseClient {
    /**
     * Main Supabase client instance configured with:
     * - Authentication URL and API key from BuildConfig
     * - Auth module for user authentication
     * - Postgrest module for database operations
     * - Realtime module for live updates/subscriptions
     */
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.supabaseUrl,
        supabaseKey = BuildConfig.supabaseKey
    ) {
        install(Auth)        // Enables authentication features
        install(Postgrest)   // Enables database operations
        install(Realtime)    // Enables realtime subscriptions
    }
}