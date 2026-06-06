package net.pop.projectpilot.presentation.screens.projects.tasks.details

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.pop.projectpilot.data.firestore.Task
import net.pop.projectpilot.presentation.components.AddAttachmentDialog
import net.pop.projectpilot.presentation.components.AttachmentCard

@Composable
fun TaskDetailsScreen(
    task: Task,
    viewModel: TaskDetailsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showAddAttachmentDialog by remember { mutableStateOf(false) }

    LaunchedEffect(task.id) {
        viewModel.loadTaskDetails(task)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(
                    Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                "Task Details",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        when (val currentState = state) {
            is TaskDetailsState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is TaskDetailsState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(currentState.message, color = MaterialTheme.colorScheme.error)
                }
            }

            is TaskDetailsState.Success -> {
                val currentTask = currentState.task
                val attachments = currentState.attachments

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    item {
                        Text(
                            text = currentTask.title,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val statusColor =
                                if (currentTask.status.lowercase() == "completed") Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = statusColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = currentTask.status,
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = statusColor
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                currentTask.createdAt,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (currentTask.voicePath.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.clickable {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        currentTask.voicePath.toUri()
                                    )
                                    context.startActivity(intent)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Mic,
                                        contentDescription = "Voice Note",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Play Voice Note",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.5f
                            )
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Attachments (${attachments.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            TextButton(onClick = { showAddAttachmentDialog = true }) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add")
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (attachments.isEmpty()) {
                        item {
                            Text(
                                "No attachments yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(attachments) { attachment ->
                            AttachmentCard(
                                attachment = attachment,
                                onClick = {
                                    val urlToOpen =
                                        if (attachment.type == "url") attachment.url else attachment.file
                                    if (urlToOpen.isNotBlank()) {
                                        val intent = Intent(Intent.ACTION_VIEW, urlToOpen.toUri())
                                        context.startActivity(intent)
                                    }
                                },
                                onDelete = { viewModel.deleteAttachment(attachment.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddAttachmentDialog) {
        AddAttachmentDialog(
            onDismiss = { showAddAttachmentDialog = false },
            onConfirm = { title, type, fileUri, url ->
                viewModel.addAttachment(task.id, title, type, fileUri, url)
                showAddAttachmentDialog = false
            }
        )
    }
}