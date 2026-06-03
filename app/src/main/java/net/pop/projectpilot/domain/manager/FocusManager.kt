package net.pop.projectpilot.domain.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusManager @Inject constructor() {
    private val _timeRemaining = MutableStateFlow(25 * 60L)
    val timeRemaining: StateFlow<Long> = _timeRemaining.asStateFlow()

    private val _totalDuration = MutableStateFlow(25 * 60L)
    val totalDuration: StateFlow<Long> = _totalDuration.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _isDndEnabled = MutableStateFlow(false)
    val isDndEnabled: StateFlow<Boolean> = _isDndEnabled.asStateFlow()

    fun updateTime(seconds: Long) {
        _timeRemaining.update { seconds }
    }

    fun setTotalDuration(seconds: Long) {
        _totalDuration.update { seconds }
        _timeRemaining.update { seconds }
    }

    fun setRunning(running: Boolean) {
        _isRunning.update { running }
    }

    fun setDndEnabled(enabled: Boolean) {
        _isDndEnabled.update { enabled }
    }
}