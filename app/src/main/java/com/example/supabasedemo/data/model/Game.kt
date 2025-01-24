package com.example.supabasedemo.data.model

import kotlinx.serialization.Serializable

/**
 * Represents a game session in the application.
 * This data class is serializable for database storage and network transmission.
 * It tracks the state and participants of a multiplayer game session.
 *
 * @property id Database primary key (nullable for new games)
 * @property uuid Unique identifier for the game session
 * @property start_time ISO-8601 timestamp when the game started
 * @property end_time ISO-8601 timestamp when the game ended
 * @property round_no Current round number (-1 indicates not started)
 * @property user1 ID of the first player (game creator)
 * @property user2 ID of the second player (can be null if not joined)
 * @property won Game outcome (null if game in progress)
 * @property controller_address UWB address of the controlling device
 * @property controller_preamble UWB preamble code for the controller
 * @property controlee_address UWB address of the controlled device
 *
 * Example usage:
 * ```
 * val newGame = Game(
 *     uuid = UUID.randomUUID().toString(),
 *     user1 = currentUserId,
 *     round_no = 0
 * )
 * ```
 */
@Serializable
data class Game(
    val id: Long? = null,
    val uuid: String,
    val start_time: String? = null,
    val end_time: String? = null,
    val round_no: Int = -1,
    val user1: String?,
    val user2: String? = null,
    val won: Boolean? = null,
    val controller_address: String? = null,
    val controller_preamble: String? = null,
    val controlee_address: String? = null,
)