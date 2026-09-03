package com.moviesforever.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moviesforever.app.data.model.UnlockInfo
import com.moviesforever.app.data.repository.RedemptionRepository
import com.moviesforever.app.data.repository.UnlockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LockUiState(
    val redeeming: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class LockViewModel @Inject constructor(
    private val redemptionRepository: RedemptionRepository,
    private val unlockRepository: UnlockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LockUiState())
    val uiState: StateFlow<LockUiState> = _uiState.asStateFlow()

    fun redeem(id: String, username: String) {
        if (_uiState.value.redeeming) return
        _uiState.value = LockUiState(redeeming = true, error = null)
        viewModelScope.launch {
            val result = redemptionRepository.redeem(id, username)
            if (result.success && result.username != null) {
                unlockRepository.saveUnlock(
                    UnlockInfo(
                        id = id.trim(),
                        username = result.username!!,
                        unlockedAt = System.currentTimeMillis(),
                        celebrationShown = false
                    )
                )
                _uiState.value = LockUiState(success = true)
            } else {
                _uiState.value = LockUiState(
                    redeeming = false,
                    error = result.message
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun reset() {
        _uiState.value = LockUiState()
    }
}
