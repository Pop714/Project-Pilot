package net.pop.projectpilot.presentation.screens.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import net.pop.projectpilot.presentation.components.EditNameDialog
import net.pop.projectpilot.presentation.components.EditPasswordDialog
import net.pop.projectpilot.presentation.components.ProfileOptionCard
import net.pop.projectpilot.presentation.components.SaveAccountPasswordDialog

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var showNameDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showSaveAccountDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateProfileImage(it) }
    }

    LaunchedEffect(state.error, state.successMessage) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
        state.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(state.passwordUpdateSuccess) {
        if (state.passwordUpdateSuccess) {
            showPasswordDialog = false
            viewModel.resetPasswordDialogState()
        }
    }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 32.dp, top = 24.dp)
        )

        Box(
            modifier = Modifier
                .size(122.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (!state.profileImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = state.profileImageUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Picture",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = state.name,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = state.email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        ProfileOptionCard(
            title = "Edit Name",
            icon = Icons.Default.PersonOutline,
            onClick = { showNameDialog = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ProfileOptionCard(
            title = "Change Password",
            icon = Icons.Default.Lock,
            onClick = { showPasswordDialog = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (state.isAccountSaved) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = if (state.isAccountSaved) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Fast Login",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = if (state.isAccountSaved) "Biometrics enabled" else "Save for quick access",
                            color = if (state.isAccountSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (state.isAccountSaved) {
                    TextButton(onClick = { viewModel.toggleSavedAccount() }) {
                        Text("Disable", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    FilledTonalButton(onClick = { showSaveAccountDialog = true }) {
                        Text("Enable")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = { viewModel.logout(onSuccess = onNavigateToLogin) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Default.Output, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Out", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(96.dp))
    }

    if (showNameDialog) {
        EditNameDialog(
            currentName = state.name,
            onDismiss = { showNameDialog = false },
            onConfirm = { newName ->
                viewModel.updateName(newName)
                showNameDialog = false
            }
        )
    }

    if (showPasswordDialog) {
        EditPasswordDialog(
            isUpdating = state.isUpdatingPassword,
            errorMessage = state.passwordUpdateError,
            onDismiss = {
                showPasswordDialog = false
                viewModel.resetPasswordDialogState()
            },
            onConfirm = { current, new ->
                viewModel.updatePassword(current, new)
            }
        )
    }

    if (showSaveAccountDialog) {
        SaveAccountPasswordDialog(
            onDismiss = { showSaveAccountDialog = false },
            onConfirm = { password ->
                viewModel.toggleSavedAccount(password)
                showSaveAccountDialog = false
            }
        )
    }
}