package net.pop.projectpilot.presentation.screens.projects.tasks.details

import net.pop.projectpilot.data.firestore.Attachment
import net.pop.projectpilot.data.firestore.Task

sealed class TaskDetailsState {
    object Loading : TaskDetailsState()
    data class Success(val task: Task, val attachments: List<Attachment>) : TaskDetailsState()
    data class Error(val message: String) : TaskDetailsState()
}