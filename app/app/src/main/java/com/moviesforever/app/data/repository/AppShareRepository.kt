package com.moviesforever.app.data.repository

import com.moviesforever.app.data.model.AppShareLink
import kotlinx.coroutines.flow.Flow

interface AppShareRepository {
    /** The most recently added link from the `app-sharing` collection, or null if none exist yet. */
    fun observeCurrentShareLink(): Flow<AppShareLink?>
}
