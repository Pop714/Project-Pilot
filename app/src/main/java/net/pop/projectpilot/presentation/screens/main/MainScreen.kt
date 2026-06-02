package net.pop.projectpilot.presentation.screens.main

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.pop.projectpilot.domain.updater.UpdateStatus
import net.pop.projectpilot.presentation.screens.update.ForceUpdateScreen
import net.pop.projectpilot.presentation.ui.theme.BackgroundLight

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) { innerPadding ->

        when (updateState) {
            null -> {
//                LoadingState()
            }

            is UpdateStatus.ForceUpdate -> {
                ForceUpdateScreen()
            }

            else -> {
                if (updateState is UpdateStatus.RecommendedUpdate) {
                    LaunchedEffect(Unit) {
                        Toast.makeText(
                            context,
                            "A new update is available, You can download it now!",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                }

                // Main Content
            }
        }
    }
}