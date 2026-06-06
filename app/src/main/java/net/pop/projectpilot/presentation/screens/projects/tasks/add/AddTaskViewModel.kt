package net.pop.projectpilot.presentation.screens.projects.tasks.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddTaskViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val supabaseClient: SupabaseClient
) : ViewModel() {
    private val _state = MutableStateFlow<AddTaskState>(AddTaskState.Idle)
    val state: StateFlow<AddTaskState> = _state.asStateFlow()

    fun saveTask(projectId: String, title: String, status: String, audioFile: File?) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _state.value = AddTaskState.Error("User not logged in.")
            return
        }

        if (title.isBlank()) {
            _state.value = AddTaskState.Error("Task title cannot be empty.")
            return
        }

        _state.value = AddTaskState.Loading

        viewModelScope.launch {
            try {
                var voicePath = ""
                if (audioFile != null && audioFile.exists()) {
                    val fileName = "${UUID.randomUUID()}.mp4"
                    val bucket = supabaseClient.storage["task_voices"]

                    val fileBytes = audioFile.readBytes()
                    bucket.upload(fileName, fileBytes)

                    voicePath = bucket.publicUrl(fileName)
                }

                val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
                val currentTime = dateFormat.format(Date())

                val docRef = firestore.collection("tasks").document()

                val newTask = hashMapOf(
                    "id" to docRef.id,
                    "projectId" to projectId,
                    "creatorId" to userId,
                    "title" to title.trim(),
                    "status" to status,
                    "createdAt" to currentTime,
                    "voicePath" to voicePath
                )

                docRef.set(newTask).await()
                _state.value = AddTaskState.Success

            } catch (e: Exception) {
                _state.value = AddTaskState.Error(e.message ?: "Failed to save task")
            }
        }
    }

    fun resetState() {
        _state.value = AddTaskState.Idle
    }
}