package com.example.supabasedemo.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Game(
    val id: Long? = null,
    val uuid: String,
    val startTime: String? = null,
    val endTime: String? = null,
    val round_no: Int = -1,
    val user1: String?,
    val user2: String? = null,
    val won: Boolean? = null,
    val controller_address: String? = null,
    val controller_preamble: String? = null,
    val controlee_address: String? = null,
)