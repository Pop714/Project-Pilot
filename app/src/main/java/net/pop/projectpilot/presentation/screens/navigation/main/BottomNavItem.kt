package net.pop.projectpilot.presentation.screens.navigation.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector = Icons.Default.Home, val label: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Home")
    object Projects : BottomNavItem("projects", Icons.Default.Folder, "Projects")
    object AddProject : BottomNavItem("addProject", Icons.Default.Folder, "AddProject")
    object ProjectDetails : BottomNavItem("projectDetails", Icons.Default.Folder, "ProjectDetails")
    object Focus : BottomNavItem("focus", Icons.Default.Timer, "Focus")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Profile")
}