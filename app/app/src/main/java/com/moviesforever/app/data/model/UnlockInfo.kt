package com.moviesforever.app.data.model

data class UnlockInfo(
    val id: String = "",
    val username: String = "",
    val unlockedAt: Long = 0L,
    val celebrationShown: Boolean = false
)
