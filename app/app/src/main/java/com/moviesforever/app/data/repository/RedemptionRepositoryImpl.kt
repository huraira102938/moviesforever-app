package com.moviesforever.app.data.repository

import com.moviesforever.app.data.remote.WorkerApiClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RedemptionRepositoryImpl @Inject constructor(
    private val workerApiClient: WorkerApiClient
) : RedemptionRepository {

    override suspend fun redeem(id: String, username: String): RedemptionResult {
        if (id.isBlank() || username.isBlank()) {
            return RedemptionResult(false, "Please enter both your Code ID and Username.")
        }
        return try {
            val response = workerApiClient.redeem(id.trim(), username.trim())
            if (response.success) {
                RedemptionResult(
                    success = true,
                    message = response.message,
                    username = response.username ?: username.trim()
                )
            } else {
                RedemptionResult(false, response.message ?: "This code is invalid or already used.")
            }
        } catch (e: Exception) {
            RedemptionResult(false, "Network error. Please check your connection and try again.")
        }
    }
}
