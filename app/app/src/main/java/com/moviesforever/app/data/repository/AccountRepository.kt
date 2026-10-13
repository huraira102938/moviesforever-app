package com.moviesforever.app.data.repository

import com.moviesforever.app.data.model.UserAccount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

/** The server said (not the phone's saved copy) whether `users/{userId}` exists. */
data class AccountPresence(val userId: String, val exists: Boolean)

interface AccountRepository {
    /**
     * Live view of the `users/{userId}` doc. Emits null while the doc
     * doesn't exist / hasn't loaded yet, or on error.
     */
    fun observeAccount(userId: String): Flow<UserAccount?>

    /**
     * Emitted only for answers that really came from the server (never for the saved offline copy),
     * so a missing document here means the admin removed the account.
     */
    val serverPresence: SharedFlow<AccountPresence>
}
