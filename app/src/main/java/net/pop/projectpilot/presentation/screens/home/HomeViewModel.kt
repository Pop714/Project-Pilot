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

    init {
        fetchHomeData()
    }

    fun fetchHomeData() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _state.value = HomeState.Error("User not logged in")
            return
        }
        _state.value = HomeState.Loading
        viewModelScope.launch {
            try {
                val userDeferred = async { firestore.collection("users").document(userId).get().await() }

                val allProjectsDeferred = async {
                    firestore.collection("projects")
                        .whereArrayContains("members", userId)
                        .get().await()
                }

                val recentProjectsDeferred = async {
                    firestore.collection("projects")
                        .whereArrayContains("members", userId)
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                        .limit(3)
                        .get().await()
                }

                val tasksDeferred = async {
                    firestore.collection("tasks")
                        .whereEqualTo("userId", userId)
                        .whereEqualTo("status", "pending")
                        .get().await()
                }

                val userDoc = userDeferred.await()
                val allProjectsSnapshot = allProjectsDeferred.await()
                val recentProjectsSnapshot = recentProjectsDeferred.await()
                val tasksSnapshot = tasksDeferred.await()

                val userName = userDoc.getString("name")?.split(" ")?.firstOrNull() ?: "User"
                val activeProjectsCount = allProjectsSnapshot.size()
                val recentProjects = recentProjectsSnapshot.toObjects(Project::class.java)
                val pendingTasksCount = tasksSnapshot.size()

                _state.value = HomeState.Success(
                    userName = userName,
                    activeProjectsCount = activeProjectsCount,
                    pendingTasksCount = pendingTasksCount,
                    recentProjects = recentProjects
                )

            } catch (e: Exception) {
                _state.value = HomeState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

}