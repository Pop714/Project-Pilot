package net.pop.projectpilot.presentation.screens.projects.display

import net.pop.projectpilot.data.firestore.Project

sealed interface ProjectsUiState {
    object Loading : ProjectsUiState
    data class Success(val projects: List<Project>) : ProjectsUiState
    data class Error(val message: String) : ProjectsUiState
}