package com.moviesforever.app.data.repository

import com.moviesforever.app.data.model.UserAccount
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    /**
     * Live view of the `users/{userId}` doc. Emits null while the doc
     * doesn't exist / hasn't loaded yet, or on error.
     */
    fun observeAccount(userId: String): Flow<UserAccount?>
}
