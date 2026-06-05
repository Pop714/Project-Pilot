package net.pop.projectpilot.presentation.screens.projects.details

import net.pop.projectpilot.data.firestore.Project
import net.pop.projectpilot.data.firestore.Task

sealed class ProjectDetailsState {
    object Loading : ProjectDetailsState()
    data class Success(
        val project: Project,
        val members: List<MemberUiModel>,
        val tasks: List<Task>,
        val currentUserId: String?
    ) : ProjectDetailsState()

    data class Error(val message: String) : ProjectDetailsState()
}