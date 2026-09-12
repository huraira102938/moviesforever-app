package com.moviesforever.app.data.repository

import com.moviesforever.app.data.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationsRepository {
    /** All notifications the admin has sent, newest first. Filtering by
     * target group (free/paid/paused) happens in [com.moviesforever.app.ui.viewmodel.AppUiState],
     * not here, since that depends on the current user's unlock/pause state. */
    fun observeNotifications(): Flow<List<AppNotification>>
}
