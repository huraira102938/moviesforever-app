package com.moviesforever.app.data.repository

data class RedemptionResult(
    val success: Boolean,
    val message: String,
    val username: String? = null
)

interface RedemptionRepository {
    /**
     * Validates ID + username pair and atomically burns the code via the Cloudflare Worker.
     * The username returned on success is the assigned referral identity.
     */
    suspend fun redeem(id: String, username: String): RedemptionResult
}
