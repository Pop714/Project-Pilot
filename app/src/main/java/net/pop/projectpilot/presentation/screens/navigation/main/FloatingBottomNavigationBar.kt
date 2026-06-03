package net.pop.projectpilot.presentation.screens.navigation.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun FloatingBottomNavigationBar(
    navController: NavController,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Projects,
        BottomNavItem.Focus,
        BottomNavItem.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(surfaceColor)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                val contentColor = if (isSelected) activeColor else inactiveColor

                val interactionSource = remember { MutableInteractionSource() }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            if (!isSelected) {
                                navController.navigate(item.route) {
                                    navController.graph.startDestinationRoute?.let { route ->
                                        popUpTo(route) { saveState = true }
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = contentColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.label,
                        color = contentColor,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}