package net.pop.projectpilot.presentation.screens.home

import net.pop.projectpilot.data.firestore.Project

sealed class HomeState {
    object Loading : HomeState()
    data class Success(
        val userName: String,
        val activeProjectsCount: Int,
        val pendingTasksCount: Int,
        val recentProjects: List<Project>
    ) : HomeState()
    data class Error(val message: String) : HomeState()
}