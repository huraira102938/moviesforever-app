package com.moviesforever.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.moviesforever.app.data.model.UnlockInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "unlock_store")

@Singleton
class UnlockRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UnlockRepository {

    private object Keys {
        val ID = stringPreferencesKey("unlock_id")
        val USERNAME = stringPreferencesKey("unlock_username")
        val UNLOCKED_AT = longPreferencesKey("unlock_at")
        val CELEBRATION_SHOWN = booleanPreferencesKey("celebration_shown")
    }

    override suspend fun isUnlocked(): Boolean {
        return context.dataStore.data.first().let {
            it[Keys.ID] != null && it[Keys.USERNAME] != null
        }
    }

    override fun observeUnlockInfo(): Flow<UnlockInfo?> {
        return context.dataStore.data.map { prefs ->
            val id = prefs[Keys.ID] ?: return@map null
            val username = prefs[Keys.USERNAME] ?: return@map null
            UnlockInfo(
                id = id,
                username = username,
                unlockedAt = prefs[Keys.UNLOCKED_AT] ?: 0L,
                celebrationShown = prefs[Keys.CELEBRATION_SHOWN] ?: false
            )
        }
    }

    override suspend fun saveUnlock(info: UnlockInfo) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ID] = info.id
            prefs[Keys.USERNAME] = info.username
            prefs[Keys.UNLOCKED_AT] = info.unlockedAt
            prefs[Keys.CELEBRATION_SHOWN] = info.celebrationShown
        }
    }

    override suspend fun markCelebrationShown() {
        context.dataStore.edit { prefs ->
            prefs[Keys.CELEBRATION_SHOWN] = true
        }
    }

    override suspend fun resetUnlock() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.ID)
            prefs.remove(Keys.USERNAME)
            prefs.remove(Keys.UNLOCKED_AT)
            prefs.remove(Keys.CELEBRATION_SHOWN)
        }
    }
}
