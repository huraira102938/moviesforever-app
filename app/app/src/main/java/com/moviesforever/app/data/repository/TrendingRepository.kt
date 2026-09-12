package com.moviesforever.app.data.repository

import com.moviesforever.app.data.model.TrendingItem
import kotlinx.coroutines.flow.Flow

interface TrendingRepository {
    /** Emits the admin-curated trending list, ordered by `order` ascending. */
    fun observeTrendingItems(): Flow<List<TrendingItem>>
}
