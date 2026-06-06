package net.pop.projectpilot.presentation.screens.projects.tasks.details

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.pop.projectpilot.data.firestore.Attachment
import net.pop.projectpilot.data.firestore.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TaskDetailsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val supabaseClient: SupabaseClient,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow<TaskDetailsState>(TaskDetailsState.Loading)
    val uiState: StateFlow<TaskDetailsState> = _uiState.asStateFlow()

    private var currentTask: Task? = null

    fun loadTaskDetails(task: Task) {
        currentTask = task
        fetchAttachments(task.id)
    }

    private fun fetchAttachments(taskId: String) {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("attachments")
                    .whereEqualTo("taskId", taskId)
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()

                val attachmentsList = snapshot.toObjects(Attachment::class.java)
                currentTask?.let {
                    _uiState.value = TaskDetailsState.Success(it, attachmentsList)
                }
            } catch (e: Exception) {
                _uiState.value = TaskDetailsState.Error(e.message ?: "Failed to load attachments")
            }
        }
    }

    fun addAttachment(taskId: String, title: String, type: String, fileUri: Uri?, url: String) {
        if (title.isBlank()) return

        viewModelScope.launch {
            _uiState.value = TaskDetailsState.Loading

            try {
                var uploadedFilePath = ""

                if (type == "file" && fileUri != null) {
                    val bytes = context.contentResolver.openInputStream(fileUri)?.readBytes()
                    if (bytes != null) {
                        val mimeType = context.contentResolver.getType(fileUri)
                        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"

                        val fileName = "${UUID.randomUUID()}.$extension"

                        val bucket = supabaseClient.storage["task_attachments"]
                        bucket.upload(fileName, bytes)
                        uploadedFilePath = bucket.publicUrl(fileName)
                    }
                }

                val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
                val currentTime = dateFormat.format(Date())
                val docRef = firestore.collection("attachments").document()

                val newAttachment = Attachment(
                    id = docRef.id,
                    taskId = taskId,
                    title = title.trim(),
                    type = type,
                    file = uploadedFilePath,
                    url = if (type == "url") url.trim() else "",
                    createdAt = currentTime
                )

                docRef.set(newAttachment).await()

                fetchAttachments(taskId)

            } catch (e: Exception) {
                Log.e("Abbas", e.message.toString())
                _uiState.value = TaskDetailsState.Error(e.message ?: "Failed to add attachment")
            }
        }
    }

    fun deleteAttachment(attachmentId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("attachments").document(attachmentId).delete().await()
                currentTask?.let { fetchAttachments(it.id) }
            } catch (e: Exception) {
                _uiState.value = TaskDetailsState.Error(e.message ?: "Failed to delete attachment")
            }
        }
    }
}