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
            return RedemptionResult(false, MSG_ENTER_BOTH)
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
                RedemptionResult(false, toUserMessage(response.message))
            }
        } catch (e: retrofit2.HttpException) {
            // The server answered with an error status (for example 500).
            RedemptionResult(false, MSG_SERVER)
        } catch (e: Exception) {
            RedemptionResult(false, MSG_NETWORK)
        }
    }

    /** The server sends English text; the person always sees simple Roman Urdu. */
    private fun toUserMessage(serverMessage: String?): String {
        val m = serverMessage.orEmpty()
        return when {
            m.contains("already used", ignoreCase = true) -> MSG_USED
            m.contains("does not match", ignoreCase = true) -> MSG_USERNAME_MISMATCH
            m.contains("enter both", ignoreCase = true) -> MSG_ENTER_BOTH
            m.contains("server error", ignoreCase = true) -> MSG_SERVER
            m.contains("invalid", ignoreCase = true) -> MSG_INVALID
            else -> MSG_GENERIC
        }
    }

    private companion object {
        const val MSG_ENTER_BOTH = "Please enter both your code and username."
        const val MSG_INVALID = "This code is not valid. Please check it and try again."
        const val MSG_USED = "This code has already been used."
        const val MSG_USERNAME_MISMATCH = "This username does not match the code. Please check it and try again."
        const val MSG_SERVER = "Something went wrong on our side. Please try again later."
        const val MSG_NETWORK = "Connection problem. Check your internet and try again."
        const val MSG_GENERIC = "The code or username is not correct. Please check and try again."
    }
}
