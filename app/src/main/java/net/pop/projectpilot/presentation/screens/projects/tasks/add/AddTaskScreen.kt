package net.pop.projectpilot.presentation.screens.projects.tasks.add

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.delay
import net.pop.projectpilot.domain.recorder.AudioRecorderHelper
import java.io.File
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    projectId: String,
    viewModel: AddTaskViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("In Progress") }
    val statuses = listOf("In Progress", "Completed")

    val audioRecorder = remember { AudioRecorderHelper(context) }
    var isRecording by remember { mutableStateOf(false) }
    var recordedFile by remember { mutableStateOf<File?>(null) }

    val mediaPlayer = remember { MediaPlayer() }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackProgress by remember { mutableFloatStateOf(0f) }
    var currentTimeStr by remember { mutableStateOf("00:00") }
    var totalTimeStr by remember { mutableStateOf("00:00") }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    fun formatTime(ms: Int): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / 1000) / 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
            if (isRecording) audioRecorder.stopRecording()
        }
    }

    LaunchedEffect(isRecording, recordedFile) {
        if (!isRecording && recordedFile != null && recordedFile!!.exists()) {
            try {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(recordedFile!!.absolutePath)
                mediaPlayer.prepare()
                totalTimeStr = formatTime(mediaPlayer.duration)
                currentTimeStr = "00:00"
                playbackProgress = 0f

                mediaPlayer.setOnCompletionListener {
                    isPlaying = false
                    playbackProgress = 0f
                    currentTimeStr = "00:00"
                }
            } catch (e: Exception) {
                Toast.makeText(context, "MediaPlayer prepare failed", Toast.LENGTH_SHORT).show()
            }
        } else if (recordedFile == null) {
            if (isPlaying) {
                mediaPlayer.stop()
                isPlaying = false
            }
            playbackProgress = 0f
            currentTimeStr = "00:00"
            totalTimeStr = "00:00"
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            if (mediaPlayer.duration > 0) {
                playbackProgress = mediaPlayer.currentPosition.toFloat() / mediaPlayer.duration.toFloat()
                currentTimeStr = formatTime(mediaPlayer.currentPosition)
            }
            delay(50.milliseconds)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            recordedFile = audioRecorder.startRecording()
            isRecording = true
        } else {
            Toast.makeText(context, "Microphone permission is required.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(state) {
        when (state) {
            is AddTaskState.Success -> {
                Toast.makeText(context, "Task Added!", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                onNavigateBack()
            }
            is AddTaskState.Error -> {
                Log.e("Abbas", (state as AddTaskState.Error).message)
                Toast.makeText(context, (state as AddTaskState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Button(
                onClick = {
                    viewModel.saveTask(
                        projectId = projectId,
                        title = title,
                        status = selectedStatus,
                        audioFile = recordedFile
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(72.dp)
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(16.dp),
                enabled = state !is AddTaskState.Loading
            ) {
                if (state is AddTaskState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text("Create Task", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("New Task", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                placeholder = { Text("e.g. Design Login Screen") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                maxLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text("Initial Status", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                statuses.forEach { status ->
                    val isSelected = selectedStatus == status
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStatus = status },
                        label = { Text(status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (status == "Completed") Color(0xFFE8F5E9) else MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = if (status == "Completed") Color(0xFF2E7D32) else MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Voice Note (Optional)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 88.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (isRecording || recordedFile == null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier.size(56.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .scale(pulseScale)
                                        .clip(CircleShape)
                                        .background(
                                            if (isRecording) MaterialTheme.colorScheme.error.copy(
                                                alpha = 0.3f
                                            ) else Color.Transparent
                                        )
                                )
                                IconButton(
                                    onClick = {
                                        if (isRecording) {
                                            audioRecorder.stopRecording()
                                            isRecording = false
                                        } else {
                                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                                recordedFile = audioRecorder.startRecording()
                                                isRecording = true
                                            } else {
                                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(
                                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                        contentDescription = "Record",
                                        tint = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = if (isRecording) "Recording in progress..." else "Tap the mic to record instructions",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isRecording) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = {
                                    if (isPlaying) {
                                        mediaPlayer.pause()
                                        isPlaying = false
                                    } else {
                                        mediaPlayer.start()
                                        isPlaying = true
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(currentTimeStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    Text(totalTimeStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { playbackProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primaryContainer,
                                    strokeCap = StrokeCap.Round,
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            IconButton(
                                onClick = {
                                    if (isPlaying) {
                                        mediaPlayer.stop()
                                        isPlaying = false
                                    }
                                    recordedFile?.delete()
                                    recordedFile = null
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}