package net.pop.projectpilot.presentation.screens.home

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
class HomeViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow<HomeState>(HomeState.Loading)
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        fetchHomeData()
    }

    fun fetchHomeData(isRefresh: Boolean = false) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _state.value = HomeState.Error("User not logged in")
            return
        }

        if (isRefresh) {
            _isRefreshing.value = true
        } else {
            _state.value = HomeState.Loading
        }

        viewModelScope.launch {
            try {
                val userDeferred = async { firestore.collection("users").document(userId).get().await() }

                val allProjectsSnapshot = firestore.collection("projects")
                    .whereArrayContains("members", userId)
                    .get().await()

                val projectIds = allProjectsSnapshot.documents.map { it.id }
                val activeProjectsCount = projectIds.size

                val recentProjectsDeferred = async {
                    firestore.collection("projects")
                        .whereArrayContains("members", userId)
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                        .limit(3)
                        .get().await()
                }

                var pendingTasksCount = 0
                if (projectIds.isNotEmpty()) {
                    val chunks = projectIds.chunked(10)
                    for (chunk in chunks) {
                        val tasksSnapshot = firestore.collection("tasks")
                            .whereIn("projectId", chunk)
                            .get().await()

                        pendingTasksCount += tasksSnapshot.documents.count { doc ->
                            val status = doc.getString("status")?.lowercase() ?: ""
                            status == "pending" || status == "in progress"
                        }
                    }
                }

                val userDoc = userDeferred.await()
                val recentProjectsSnapshot = recentProjectsDeferred.await()

                val userName = userDoc.getString("name")?.split(" ")?.firstOrNull() ?: "User"
                val recentProjects = recentProjectsSnapshot.toObjects(Project::class.java)

                _state.value = HomeState.Success(
                    userName = userName,
                    activeProjectsCount = activeProjectsCount,
                    pendingTasksCount = pendingTasksCount,
                    recentProjects = recentProjects
                )

            } catch (e: Exception) {
                _state.value = HomeState.Error(e.message ?: "An unexpected error occurred")
            } finally {
                if (isRefresh) {
                    _isRefreshing.value = false
                }
            }
        }
    }
}