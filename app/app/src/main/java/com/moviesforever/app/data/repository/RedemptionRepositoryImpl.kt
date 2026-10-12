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
                RedemptionResult(false, toUrdu(response.message))
            }
        } catch (e: retrofit2.HttpException) {
            // The server answered with an error status (for example 500).
            RedemptionResult(false, MSG_SERVER)
        } catch (e: Exception) {
            RedemptionResult(false, MSG_NETWORK)
        }
    }

    /** The server sends English text; the person always sees simple Urdu. */
    private fun toUrdu(serverMessage: String?): String {
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
        const val MSG_ENTER_BOTH = "براہ کرم کوڈ اور یوزرنیم دونوں لکھیں۔"
        const val MSG_INVALID = "یہ کوڈ غلط ہے۔ براہ کرم کوڈ دوبارہ چیک کریں۔"
        const val MSG_USED = "یہ کوڈ پہلے ہی استعمال ہو چکا ہے۔"
        const val MSG_USERNAME_MISMATCH = "یہ یوزرنیم اس کوڈ کے ساتھ میل نہیں کھاتا۔ یوزرنیم دوبارہ چیک کریں۔"
        const val MSG_SERVER = "سرور میں مسئلہ ہے۔ تھوڑی دیر بعد دوبارہ کوشش کریں۔"
        const val MSG_NETWORK = "انٹرنیٹ کا مسئلہ ہے۔ انٹرنیٹ چیک کریں اور دوبارہ کوشش کریں۔"
        const val MSG_GENERIC = "کوڈ یا یوزرنیم درست نہیں ہے۔ دوبارہ چیک کریں۔"
    }
}
