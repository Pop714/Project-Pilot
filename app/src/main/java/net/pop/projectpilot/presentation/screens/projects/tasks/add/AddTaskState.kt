package net.pop.projectpilot.presentation.screens.projects.tasks.add

sealed class AddTaskState {
    object Idle : AddTaskState()
    object Loading : AddTaskState()
    object Success : AddTaskState()
    data class Error(val message: String) : AddTaskState()
}