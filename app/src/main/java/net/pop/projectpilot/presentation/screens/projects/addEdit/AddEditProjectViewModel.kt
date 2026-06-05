package net.pop.projectpilot.presentation.screens.projects.addEdit

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
class AddEditProjectViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow<AddEditProjectState>(AddEditProjectState.Idle)
    val state: StateFlow<AddEditProjectState> = _state.asStateFlow()

    fun saveProject(projectId: String?, title: String, description: String, priority: String, status: String = "Active") {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _state.value = AddEditProjectState.Error("User not logged in.")
            return
        }

        if (title.isBlank() || description.isBlank()) {
            _state.value = AddEditProjectState.Error("Please fill in all fields.")
            return
        }

        _state.value = AddEditProjectState.Loading

        viewModelScope.launch {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
                val currentTime = dateFormat.format(Date())

                val isEditing = projectId != null

                if (isEditing) {
                    val updates = mapOf(
                        "title" to title.trim(),
                        "description" to description.trim(),
                        "priority" to priority,
                        "status" to status,
                        "lastUpdated" to currentTime
                    )
                    firestore.collection("projects").document(projectId!!).update(updates).await()
                } else {
                    val docRef = firestore.collection("projects").document()
                    val newProject = hashMapOf(
                        "id" to docRef.id,
                        "userId" to userId,
                        "title" to title.trim(),
                        "description" to description.trim(),
                        "priority" to priority,
                        "status" to status,
                        "createdAt" to currentTime,
                        "lastUpdated" to currentTime,
                        "members" to listOf(userId)
                    )
                    docRef.set(newProject).await()
                }

                _state.value = AddEditProjectState.Success

            } catch (e: Exception) {
                _state.value = AddEditProjectState.Error(e.message ?: "Failed to save project")
            }
        }
    }

    fun resetState() {
        _state.value = AddEditProjectState.Idle
    }

}