package net.pop.projectpilot.presentation.screens.update

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import net.pop.projectpilot.presentation.ui.theme.AppTypography
import net.pop.projectpilot.presentation.ui.theme.BackgroundLight
import net.pop.projectpilot.presentation.ui.theme.OnPrimaryContainer
import net.pop.projectpilot.presentation.ui.theme.TertiaryAmber

@Composable
fun ForceUpdateScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Time to Update! 🚀",
            style = AppTypography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = OnPrimaryContainer
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "We've added new features and squashed some bugs. Please update to the latest version to continue.",
            style = AppTypography.bodyLarge,
            color = OnPrimaryContainer.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val appPackageName = context.packageName
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW,
                        "https://github.com/Pop714/Project-Pilot/releases/".toUri()))
                } catch (_: android.content.ActivityNotFoundException) {
                    context.startActivity(Intent(Intent.ACTION_VIEW,
                        "https://github.com/Pop714/Project-Pilot/releases/".toUri()))
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = TertiaryAmber),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Update Now", style = AppTypography.labelLarge)
        }
    }
}