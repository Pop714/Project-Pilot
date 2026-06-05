package net.pop.projectpilot.presentation.screens.projects.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AddProjectViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow<AddProjectState>(AddProjectState.Idle)
    val state: StateFlow<AddProjectState> = _state.asStateFlow()

    private val _inviteState = MutableStateFlow<String?>(null)
    val inviteState: StateFlow<String?> = _inviteState.asStateFlow()

    fun addProject(title: String, description: String, priority: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _state.value = AddProjectState.Error("User not logged in.")
            return
        }

        if (title.isBlank() || description.isBlank()) {
            _state.value = AddProjectState.Error("Please fill in all fields.")
            return
        }

        _state.value = AddProjectState.Loading

        viewModelScope.launch {
            try {
                val docRef = firestore.collection("projects").document()

                val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
                val currentTime = dateFormat.format(Date())

                val newProject = hashMapOf(
                    "id" to docRef.id,
                    "userId" to userId,
                    "title" to title.trim(),
                    "description" to description.trim(),
                    "priority" to priority,
                    "status" to "Active",
                    "createdAt" to currentTime,
                    "lastUpdated" to currentTime,
                    "members" to listOf(userId)
                )

                docRef.set(newProject).await()
                _state.value = AddProjectState.Success

            } catch (e: Exception) {
                _state.value = AddProjectState.Error(e.message ?: "Failed to create project")
            }
        }
    }

    fun resetState() {
        _state.value = AddProjectState.Idle
    }

    fun addMemberToProject(projectId: String, memberEmail: String) {
        if (memberEmail.isBlank()) {
            _inviteState.value = "Please enter an email."
            return
        }

        viewModelScope.launch {
            try {
                val userQuery = firestore.collection("users")
                    .whereEqualTo("email", memberEmail.trim())
                    .get()
                    .await()

                if (userQuery.isEmpty) {
                    _inviteState.value = "No user found with this email."
                    return@launch
                }

                val newMemberId = userQuery.documents.first().id

                if (newMemberId == auth.currentUser?.uid) {
                    _inviteState.value = "You are already the owner of this project."
                    return@launch
                }

                firestore.collection("projects").document(projectId)
                    .update("members", FieldValue.arrayUnion(newMemberId))
                    .await()

                _inviteState.value = "User successfully added to project!"

            } catch (e: Exception) {
                _inviteState.value = e.message ?: "Failed to add member."
            }
        }
    }

    fun clearInviteState() {
        _inviteState.value = null
    }

}