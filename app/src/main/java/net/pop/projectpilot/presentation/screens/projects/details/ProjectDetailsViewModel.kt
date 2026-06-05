package net.pop.projectpilot.presentation.screens.projects.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.pop.projectpilot.data.firestore.Project
import net.pop.projectpilot.data.firestore.Task
import javax.inject.Inject

@HiltViewModel
class ProjectDetailsViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProjectDetailsState>(ProjectDetailsState.Loading)
    val uiState: StateFlow<ProjectDetailsState> = _uiState.asStateFlow()

    private val _inviteMessage = MutableStateFlow<String?>(null)
    val inviteMessage: StateFlow<String?> = _inviteMessage.asStateFlow()

    private val _projectDeleted = MutableStateFlow(false)
    val projectDeleted: StateFlow<Boolean> = _projectDeleted.asStateFlow()

    private var currentProject: Project? = null

    fun loadProjectDetails(project: Project) {
        currentProject = project
        fetchMembers(project)
    }

    private fun fetchMembers(project: Project) {
        viewModelScope.launch {
            try {
                _uiState.value = ProjectDetailsState.Loading

                val membersList = mutableListOf<MemberUiModel>()
                if (project.members.isNotEmpty()) {
                    val chunks = project.members.chunked(10)
                    for (chunk in chunks) {
                        val usersSnapshot = firestore.collection("users")
                            .whereIn(FieldPath.documentId(), chunk)
                            .get()
                            .await()

                        for (doc in usersSnapshot.documents) {
                            val id = doc.id
                            val name = doc.getString("name") ?: "Unknown User"
                            val avatarUrl =
                                "https://tyzbupxlnwpddpgzwfju.supabase.co/storage/v1/object/public/profile_pictures/$id.jpg"
                            membersList.add(MemberUiModel(id, name, avatarUrl))
                        }
                    }
                }

                val tasksSnapshot = firestore.collection("tasks")
                    .whereEqualTo("projectId", project.id)
                    .get()
                    .await()

                val tasksList = tasksSnapshot.toObjects(Task::class.java)

                _uiState.value =
                    ProjectDetailsState.Success(
                        project,
                        membersList,
                        tasks = tasksList,
                        auth.currentUser?.uid
                    )

            } catch (e: Exception) {
                _uiState.value = ProjectDetailsState.Error(e.message ?: "Failed to load members")
            }
        }
    }

    fun addMemberToProject(memberEmail: String) {
        val project = currentProject ?: return

        if (memberEmail.isBlank()) {
            _inviteMessage.value = "Please enter an email."
            return
        }

        viewModelScope.launch {
            try {
                val userQuery = firestore.collection("users")
                    .whereEqualTo("email", memberEmail.trim())
                    .get()
                    .await()

                if (userQuery.isEmpty) {
                    _inviteMessage.value = "No user found with this email."
                    return@launch
                }

                val newMemberId = userQuery.documents.first().id

                if (project.members.contains(newMemberId)) {
                    _inviteMessage.value = "User is already a member of this project."
                    return@launch
                }

                firestore.collection("projects").document(project.id)
                    .update("members", FieldValue.arrayUnion(newMemberId))
                    .await()

                val updatedMembers = project.members + newMemberId
                val updatedProject = project.copy(members = updatedMembers)
                currentProject = updatedProject

                fetchMembers(updatedProject)

                _inviteMessage.value = "User successfully added!"

            } catch (e: Exception) {
                _inviteMessage.value = e.message ?: "Failed to add member."
            }
        }
    }

    fun removeMemberFromProject(memberId: String) {
        val project = currentProject ?: return
        viewModelScope.launch {
            try {
                firestore.collection("projects").document(project.id)
                    .update("members", FieldValue.arrayRemove(memberId))
                    .await()

                val updatedProject = project.copy(members = project.members - memberId)
                currentProject = updatedProject
                fetchMembers(updatedProject)

                _inviteMessage.value = "Member removed."
            } catch (e: Exception) {
                _inviteMessage.value = e.message ?: "Failed to remove member."
            }
        }
    }

    fun deleteProject() {
        val project = currentProject ?: return
        viewModelScope.launch {
            try {
                _uiState.value = ProjectDetailsState.Loading
                firestore.collection("projects").document(project.id).delete().await()
                _projectDeleted.value = true
            } catch (e: Exception) {
                _uiState.value = ProjectDetailsState.Error(e.message ?: "Failed to delete project")
            }
        }
    }

    fun clearInviteMessage() {
        _inviteMessage.value = null
    }
}