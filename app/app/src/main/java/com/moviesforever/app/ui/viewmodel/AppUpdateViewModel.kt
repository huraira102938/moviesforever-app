package com.moviesforever.app.ui.viewmodel

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moviesforever.app.data.model.AppUpdateConfig
import com.moviesforever.app.data.repository.AppUpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** What the app should do right now: wait, run normally, or show the force-update screen. */
data class UpdateGate(
    val checked: Boolean = false,
    val blocked: Boolean = false,
    val config: AppUpdateConfig = AppUpdateConfig(),
    val currentVersionCode: Long = 0L
)

@HiltViewModel
class AppUpdateViewModel @Inject constructor(
    private val repository: AppUpdateRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val versionCode: Long = readVersionCode()

    private val _gate = MutableStateFlow(UpdateGate(currentVersionCode = versionCode))
    val gate: StateFlow<UpdateGate> = _gate.asStateFlow()

    init {
        // If the saved value says blocked, block from the very first frame, even offline.
        // Otherwise wait for the server answer (max 3 seconds) before showing the app.
        val saved = repository.readCache()
        if (saved.blocks(versionCode)) publish(saved)

        viewModelScope.launch {
            repository.observe().collect { publish(it) }
        }
        // Never keep the person on a blank screen: if the server is slow, go with what we have.
        viewModelScope.launch {
            delay(3_000)
            if (!_gate.value.checked) _gate.value = _gate.value.copy(checked = true)
        }
    }

    private fun publish(config: AppUpdateConfig) {
        _gate.value = UpdateGate(
            checked = true,
            blocked = config.blocks(versionCode),
            config = config,
            currentVersionCode = versionCode
        )
    }

    @Suppress("DEPRECATION")
    private fun readVersionCode(): Long = try {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode else info.versionCode.toLong()
    } catch (_: Exception) {
        Long.MAX_VALUE // cannot read our own version: never block by mistake
    }
}
