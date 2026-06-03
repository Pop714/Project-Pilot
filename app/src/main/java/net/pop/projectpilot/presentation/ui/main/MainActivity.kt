package net.pop.projectpilot.presentation.ui.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import net.pop.projectpilot.permissions.RequestNotificationPermission
import net.pop.projectpilot.presentation.screens.navigation.auth.ProjectPilotNavigation
import net.pop.projectpilot.presentation.ui.theme.ProjectPilotTheme

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            ProjectPilotTheme {
                RequestNotificationPermission()
            }
            ProjectPilotNavigation()
        }
    }

}


