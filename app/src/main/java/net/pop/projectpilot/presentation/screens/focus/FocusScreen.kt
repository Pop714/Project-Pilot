package net.pop.projectpilot.presentation.screens.focus

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.pop.projectpilot.presentation.components.PresetChip
import kotlin.math.atan2

@SuppressLint("DefaultLocale")
@Composable
fun FocusScreen(
    viewModel: FocusViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val notificationManager = remember {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    val timeRemaining by viewModel.focusManager.timeRemaining.collectAsState()
    val totalDuration by viewModel.focusManager.totalDuration.collectAsState()
    val isRunning by viewModel.focusManager.isRunning.collectAsState()
    val isDndEnabled by viewModel.focusManager.isDndEnabled.collectAsState()

    var showCustomTimeDialog by remember { mutableStateOf(false) }
    var customMinutesInput by remember { mutableStateOf("") }

    var circleCenter by remember { mutableStateOf(Offset.Zero) }
    val maxMinutes = 240f

    val dndSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (notificationManager.isNotificationPolicyAccessGranted) {
            viewModel.toggleDnd()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            viewModel.toggleTimer(context)
        }
    }

    val mins = timeRemaining / 60
    val secs = timeRemaining % 60
    val formattedTime = String.format("%02d:%02d", mins, secs)

    val targetProgress = if (isRunning) {
        if (totalDuration > 0) timeRemaining.toFloat() / totalDuration.toFloat() else 0f
    } else {
        (timeRemaining / 60f) / maxMinutes
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        label = "timer_progress"
    )

    fun updateTimeFromPointer(offset: Offset) {
        if (circleCenter == Offset.Zero) return

        val dx = offset.x - circleCenter.x
        val dy = offset.y - circleCenter.y

        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()

        angle += 90f
        if (angle < 0f) angle += 360f

        var minutes = ((angle / 360f) * maxMinutes).toLong()
        if (minutes < 1L) minutes = 1L
        if (minutes > 240L) minutes = 240L

        viewModel.setDuration(minutes)
    }

    if (showCustomTimeDialog) {
        AlertDialog(
            onDismissRequest = { showCustomTimeDialog = false },
            title = { Text("Set Custom Timer", style = MaterialTheme.typography.headlineMedium) },
            text = {
                OutlinedTextField(
                    value = customMinutesInput,
                    onValueChange = {
                        if (it.length <= 3 && it.all { char -> char.isDigit() }) customMinutesInput =
                            it
                    },
                    label = { Text("Minutes (Max 240)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        var parsedMinutes = customMinutesInput.toLongOrNull()
                        if (parsedMinutes != null && parsedMinutes > 0) {
                            if (parsedMinutes > 240) parsedMinutes = 240
                            viewModel.setDuration(parsedMinutes)
                        }
                        showCustomTimeDialog = false
                        customMinutesInput = ""
                    },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Set")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTimeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "DEEP FOCUS",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(280.dp)
                .shadow(
                    elevation = if (isRunning) 32.dp else 0.dp,
                    shape = CircleShape,
                    spotColor = MaterialTheme.colorScheme.primary,
                    ambientColor = MaterialTheme.colorScheme.primary
                )
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .onGloballyPositioned { coordinates ->
                    circleCenter = Offset(coordinates.size.width / 2f, coordinates.size.height / 2f)
                }
                .pointerInput(isRunning) {
                    if (!isRunning) {
                        detectTapGestures(
                            onTap = { offset -> updateTimeFromPointer(offset) }
                        )
                    }
                }
                .pointerInput(isRunning) {
                    if (!isRunning) {
                        detectDragGestures(
                            onDragStart = { offset -> updateTimeFromPointer(offset) },
                            onDrag = { change, _ -> updateTimeFromPointer(change.position) }
                        )
                    }
                }
        ) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
                strokeWidth = 12.dp,
            )
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 12.dp,
                strokeCap = StrokeCap.Round,
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formattedTime,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                if (!isRunning) {
                    Text(
                        text = "Drag to adjust",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (!isRunning) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                PresetChip(text = "15m", onClick = { viewModel.setDuration(15) })
                PresetChip(text = "25m", onClick = { viewModel.setDuration(25) })
                PresetChip(text = "60m", onClick = { viewModel.setDuration(60) })
                PresetChip(text = "Custom", onClick = { showCustomTimeDialog = true })
            }
        } else {
            Spacer(modifier = Modifier.height(40.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DoNotDisturb,
                        contentDescription = "DND",
                        tint = if (isDndEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Do Not Disturb",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Block notifications during focus",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Switch(
                    checked = isDndEnabled,
                    onCheckedChange = {
                        if (notificationManager.isNotificationPolicyAccessGranted) {
                            viewModel.toggleDnd()
                        } else {
                            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                            dndSettingsLauncher.launch(intent)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.resetTimer(context) },
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isRunning) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.toggleTimer(context)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(96.dp))
    }
}