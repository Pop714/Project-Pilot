package net.pop.projectpilot.presentation.screens.projects.addEdit

sealed class AddEditProjectState {
    object Idle : AddEditProjectState()
    object Loading : AddEditProjectState()
    object Success : AddEditProjectState()
    data class Error(val message: String) : AddEditProjectState()
}