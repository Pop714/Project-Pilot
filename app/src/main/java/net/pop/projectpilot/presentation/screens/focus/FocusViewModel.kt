package net.pop.projectpilot.presentation.screens.focus

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import net.pop.projectpilot.domain.manager.FocusManager
import net.pop.projectpilot.services.FocusTimerService
import javax.inject.Inject

@HiltViewModel
class FocusViewModel @Inject constructor(
    val focusManager: FocusManager
) : ViewModel() {

    fun setDuration(minutes: Long) {
        if (focusManager.isRunning.value) return
        focusManager.setTotalDuration(minutes * 60)
    }

    fun toggleTimer(context: Context) {
        val action = if (focusManager.isRunning.value) {
            FocusTimerService.ACTION_PAUSE
        } else {
            FocusTimerService.ACTION_START
        }
        sendCommandToService(context, action)
    }

    fun resetTimer(context: Context) {
        sendCommandToService(context, FocusTimerService.ACTION_STOP)
    }

    fun toggleDnd() {
        focusManager.setDndEnabled(!focusManager.isDndEnabled.value)
    }

    private fun sendCommandToService(context: Context, action: String) {
        val intent = Intent(context, FocusTimerService::class.java).apply {
            this.action = action
        }
        context.startService(intent)
    }
}