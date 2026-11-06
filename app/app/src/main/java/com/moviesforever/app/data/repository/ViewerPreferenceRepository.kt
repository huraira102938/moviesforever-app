package com.moviesforever.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.moviesforever.app.data.model.ViewerPreference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

private val Context.viewerDataStore: DataStore<Preferences> by preferencesDataStore(name = "viewer_store")
private val PREFERENCE_KEY = stringPreferencesKey("viewer_preference")

/** The first-launch answer, kept on this device. Null until the viewer has answered. */
@Singleton
class ViewerPreferenceRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val preference: StateFlow<ViewerPreference?> = context.viewerDataStore.data
        .map { prefs -> ViewerPreference.entries.find { it.name == prefs[PREFERENCE_KEY] } }
        .stateIn(scope, SharingStarted.Eagerly, null)

    suspend fun save(preference: ViewerPreference) {
        context.viewerDataStore.edit { it[PREFERENCE_KEY] = preference.name }
    }
}
