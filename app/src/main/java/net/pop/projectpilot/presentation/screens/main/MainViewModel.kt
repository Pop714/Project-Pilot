package net.pop.projectpilot.presentation.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.pop.projectpilot.domain.updater.AppUpdater
import net.pop.projectpilot.domain.updater.UpdateStatus

@HiltViewModel
class MainViewModel @Inject constructor(
    private val appUpdater: AppUpdater
) : ViewModel() {

    private val _updateState = MutableStateFlow<UpdateStatus?>(null)
    val updateState = _updateState.asStateFlow()

    init {
        checkForUpdates()
    }

    private fun checkForUpdates() {
        viewModelScope.launch {
            _updateState.value = appUpdater.checkForUpdates()
        }
    }

}