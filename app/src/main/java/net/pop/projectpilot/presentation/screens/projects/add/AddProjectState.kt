package net.pop.projectpilot.presentation.screens.projects.add

sealed class AddProjectState {
    object Idle : AddProjectState()
    object Loading : AddProjectState()
    object Success : AddProjectState()
    data class Error(val message: String) : AddProjectState()
}