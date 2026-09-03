package com.moviesforever.app.data.repository

import com.moviesforever.app.data.model.UnlockInfo

interface UnlockRepository {
    suspend fun isUnlocked(): Boolean
    fun observeUnlockInfo(): kotlinx.coroutines.flow.Flow<UnlockInfo?>
    suspend fun saveUnlock(info: UnlockInfo)
    suspend fun markCelebrationShown()
    suspend fun resetUnlock()
}
