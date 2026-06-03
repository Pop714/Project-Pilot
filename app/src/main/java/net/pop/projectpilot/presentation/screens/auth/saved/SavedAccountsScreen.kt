package net.pop.projectpilot.presentation.screens.auth.saved

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import net.pop.projectpilot.domain.auth.promptBiometricAuth
import net.pop.projectpilot.presentation.screens.auth.AuthState
import net.pop.projectpilot.presentation.screens.auth.AuthViewModel

@Composable
fun SavedAccountsScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToStandardLogin: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    val savedAccounts by viewModel.savedAccounts.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Choose an Account",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(savedAccounts, key = { it.email }) { account ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val activity = context as FragmentActivity
                            promptBiometricAuth(
                                activity = activity,
                                onSuccess = {
                                    viewModel.login(account.email, account.encryptedPass, false)
                                },
                                onError = { errorMessage ->
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!account.profileImageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = account.profileImageUrl,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize().size(45.dp),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Text Details (weight(1f) ensures it takes up available space)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = account.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Remove Account Button
                        IconButton(
                            onClick = { viewModel.removeSavedAccount(account.email) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Remove account",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onNavigateToStandardLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log in to another account", style = MaterialTheme.typography.labelLarge)
        }

        if (authState is AuthState.Loading) {
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else if (authState is AuthState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}