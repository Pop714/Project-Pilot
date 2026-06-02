package net.pop.projectpilot.presentation.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import net.pop.projectpilot.permissions.RequestNotificationPermission
import net.pop.projectpilot.presentation.screens.main.MainScreen
import net.pop.projectpilot.presentation.ui.theme.ProjectPilotTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjectPilotTheme {
                RequestNotificationPermission()
            }
            MainScreen()
        }
    }

}


