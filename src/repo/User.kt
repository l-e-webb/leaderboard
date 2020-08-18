package com.tangledwebgames.repo

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long,
    val name: String,
    val highScore: Long
)