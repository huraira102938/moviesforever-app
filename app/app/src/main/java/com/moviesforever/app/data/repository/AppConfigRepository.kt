package com.moviesforever.app.data.repository

import com.moviesforever.app.data.model.AppConfig
import kotlinx.coroutines.flow.Flow

interface AppConfigRepository {
    fun observeAppConfig(): Flow<AppConfig>
}
