package net.pop.projectpilot.presentation.ui.main

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import net.pop.projectpilot.domain.preferences.ThemePreferences
import net.pop.projectpilot.permissions.RequestNotificationPermission
import net.pop.projectpilot.presentation.screens.navigation.auth.ProjectPilotNavigation
import net.pop.projectpilot.presentation.ui.theme.ProjectPilotTheme
import net.pop.projectpilot.presentation.ui.theme.ThemeMode
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var themePreferences: ThemePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()

        setContent {
            val currentTheme by themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            ProjectPilotTheme(themeMode = currentTheme) {
                RequestNotificationPermission()
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    ProjectPilotNavigation()
                }
            }
        }
    }

}


