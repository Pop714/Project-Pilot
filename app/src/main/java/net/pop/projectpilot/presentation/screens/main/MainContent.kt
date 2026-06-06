package net.pop.projectpilot.presentation.screens.main

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import net.pop.projectpilot.data.firestore.Project
import net.pop.projectpilot.presentation.screens.focus.FocusScreen
import net.pop.projectpilot.presentation.screens.home.HomeScreen
import net.pop.projectpilot.presentation.screens.navigation.main.BottomNavItem
import net.pop.projectpilot.presentation.screens.navigation.main.FloatingBottomNavigationBar
import net.pop.projectpilot.presentation.screens.profile.ProfileScreen
import net.pop.projectpilot.presentation.screens.projects.display.ProjectsScreen
import com.google.gson.Gson
import net.pop.projectpilot.data.firestore.Task
import net.pop.projectpilot.presentation.screens.projects.addEdit.AddEditProjectScreen
import net.pop.projectpilot.presentation.screens.projects.details.ProjectDetailsScreen
import net.pop.projectpilot.presentation.screens.projects.tasks.AddTaskScreen
import net.pop.projectpilot.presentation.screens.projects.tasks.details.TaskDetailsScreen
import kotlin.jvm.java

@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    profileImageUrl: String? = null,
    onNavigateToLogin: () -> Unit
) {

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTopLevelScreen = currentRoute == BottomNavItem.Home.route ||
            currentRoute == BottomNavItem.Projects.route ||
            currentRoute == BottomNavItem.Focus.route ||
            currentRoute == BottomNavItem.Profile.route

    var isVisibleByScroll by remember { mutableStateOf(true) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -10) {
                    isVisibleByScroll = false
                }
                if (available.y > 10) {
                    isVisibleByScroll = true
                }
                return Offset.Zero
            }
        }
    }

    val isBottomBarVisible = isTopLevelScreen && isVisibleByScroll

    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
            enterTransition = { fadeIn(animationSpec = tween(700)) },
            exitTransition = { fadeOut(animationSpec = tween(700)) },
            popEnterTransition = { fadeIn(animationSpec = tween(700)) },
            popExitTransition = { fadeOut(animationSpec = tween(700)) }
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    onNavigateToAllProjects = {
                        navController.navigate(BottomNavItem.Projects.route) {
                            navController.graph.startDestinationRoute?.let { route ->
                                popUpTo(route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    onNavigateToProjectDetails = { project ->
                        val projectJson = Uri.encode(Gson().toJson(project))
                        navController.navigate("${BottomNavItem.ProjectDetails.route}/$projectJson")
                    }
                )
            }
            composable(BottomNavItem.Projects.route) {
                ProjectsScreen(
                    onNavigateBack = { navController.navigateUp() },
                    onNavigateToAddProject = { navController.navigate(BottomNavItem.AddProject.route) },
                    onNavigateToProjectDetails = { project ->
                        val projectJson = Uri.encode(Gson().toJson(project))
                        navController.navigate("${BottomNavItem.ProjectDetails.route}/$projectJson")
                    }
                )
            }
            composable(
                route = "${BottomNavItem.ProjectDetails.route}/{projectJson}",
                arguments = listOf(
                    navArgument("projectJson") {
                        type = NavType.StringType
                    }
                )) { backStackEntry ->
                val projectJson = backStackEntry.arguments?.getString("projectJson")
                val project = Gson().fromJson(projectJson, Project::class.java)
                if (project != null) {
                    ProjectDetailsScreen(
                        project = project,
                        onNavigateBack = { navController.navigateUp() },
                        onNavigateToEdit = { projectToEdit ->
                            val editJson = Uri.encode(Gson().toJson(projectToEdit))
                            navController.navigate("${BottomNavItem.AddProject.route}?projectJson=$editJson")
                        },
                        onNavigateToAddTask = {
                            navController.navigate("${BottomNavItem.AddTask.route}/${project.id}")
                        },
                        onNavigateToTask = { task ->
                            val taskJsonString = Uri.encode(Gson().toJson(task))
                            navController.navigate("${BottomNavItem.TaskDetails.route}?taskJson=$taskJsonString")
                        },
                    )
                }
            }
            composable(
                route = "${BottomNavItem.AddProject.route}?projectJson={projectJson}",
                arguments = listOf(
                    navArgument("projectJson") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val projectJson = backStackEntry.arguments?.getString("projectJson")
                val projectToEdit = if (!projectJson.isNullOrEmpty()) {
                    Gson().fromJson(projectJson, Project::class.java)
                } else {
                    null
                }
                AddEditProjectScreen (
                    projectToEdit = projectToEdit,
                    onNavigateBack = { navController.navigateUp() }
                )
            }
            composable(
                route = "${BottomNavItem.AddTask.route}/{projectId}",
                arguments = listOf(
                    navArgument("projectId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getString("projectId")
                if (projectId != null) {
                    AddTaskScreen(
                        projectId = projectId,
                        onNavigateBack = { navController.navigateUp() }
                    )
                }
            }
            composable(
                route = "${BottomNavItem.TaskDetails.route}?taskJson={taskJson}",
                arguments = listOf(
                    navArgument("taskJson") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val taskJson = backStackEntry.arguments?.getString("taskJson")
                val task = if (!taskJson.isNullOrEmpty()) {
                    Gson().fromJson(taskJson, Task::class.java)
                } else {
                    null
                }
                if (task != null) {
                    TaskDetailsScreen(
                        task = task,
                        onNavigateBack = { navController.navigateUp() }
                    )
                }
            }
            composable(BottomNavItem.Focus.route) {
                FocusScreen()
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen {
                    onNavigateToLogin()
                }
            }
        }

        AnimatedVisibility(
            visible = isBottomBarVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            FloatingBottomNavigationBar(
                navController = navController,
                isVisible = true,
                profileImageUrl = profileImageUrl
            )
        }
    }

}