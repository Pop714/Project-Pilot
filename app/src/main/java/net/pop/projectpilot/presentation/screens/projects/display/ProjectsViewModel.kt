package net.pop.projectpilot.presentation.screens.projects.display

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.pop.projectpilot.data.firestore.Project
import javax.inject.Inject

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProjectsUiState>(ProjectsUiState.Loading)
    val uiState: StateFlow<ProjectsUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        fetchUserProjects()
    }

    fun fetchUserProjects(isRefresh: Boolean = false) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.value = ProjectsUiState.Error("User not logged in")
            return
        }

        if (isRefresh) {
            _isRefreshing.value = true
        } else {
            _uiState.value = ProjectsUiState.Loading
        }

        viewModelScope.launch {
            try {
                val allProjectsDeferred = async {
                    firestore.collection("projects")
                        .whereArrayContains("members", userId)
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                        .get().await()
                }

                val projectsSnapshot = allProjectsDeferred.await()
                val projectsList = projectsSnapshot.toObjects(Project::class.java)

                _uiState.value = ProjectsUiState.Success(projectsList)

            } catch (e: Exception) {
                _uiState.value = ProjectsUiState.Error(e.message ?: "Failed to load projects")
            } finally {
                if (isRefresh) {
                    _isRefreshing.value = false
                }
            }
        }
    }

}