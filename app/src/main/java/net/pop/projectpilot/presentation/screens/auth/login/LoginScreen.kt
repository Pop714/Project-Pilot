package net.pop.projectpilot.presentation.screens.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import net.pop.projectpilot.presentation.screens.auth.AuthState
import net.pop.projectpilot.presentation.screens.auth.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    // Input States
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var saveAccount by remember { mutableStateOf(false) }

    // Live Error States
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Password Visibility State
    var passwordVisible by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()

    // Regex for basic email validation
    val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()

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
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Sign in to continue to Project Pilot",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = if (!it.matches(emailPattern)) "Enter a valid email address" else null
            },
            label = { Text("Email address", style = MaterialTheme.typography.bodyMedium) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = emailError != null,
            supportingText = {
                if (emailError != null) {
                    Text(text = emailError!!, style = MaterialTheme.typography.labelSmall)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password Field with Visibility Toggle
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = if (it.isBlank()) "Password cannot be empty" else null
            },
            label = { Text("Password", style = MaterialTheme.typography.bodyMedium) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            },
            isError = passwordError != null,
            supportingText = {
                if (passwordError != null) {
                    Text(text = passwordError!!, style = MaterialTheme.typography.labelSmall)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = saveAccount,
                    onCheckedChange = { saveAccount = it }
                )
                Text(text = "Save this account", style = MaterialTheme.typography.bodyMedium)
            }

            TextButton(onClick = { /* Handle Forgot Password */ }) {
                Text(
                    text = "Forgot Password?",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Firebase Error Display
        if (authState is AuthState.Error) {
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Submit Button
        val isFormValid = email.isNotBlank() && password.isNotBlank() && emailError == null

        Button(
            onClick = { viewModel.login(email, password, saveAccount) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = MaterialTheme.shapes.medium,
            enabled = authState !is AuthState.Loading && isFormValid
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Sign In", style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Don't have an account? ", style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onNavigateToRegister, contentPadding = PaddingValues(0.dp)) {
                Text(
                    text = "Register",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}