package net.pop.projectpilot.presentation.screens.navigation.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector = Icons.Default.Home, val label: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Home")
    object Projects : BottomNavItem("projects", Icons.Default.Favorite, "Projects")
    object Focus : BottomNavItem("focus", Icons.Default.ShoppingCart, "Focus")
    object Profile : BottomNavItem("profile", Icons.Default.ShoppingCart, "Profile")
}