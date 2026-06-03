package net.pop.projectpilot.presentation.screens.main

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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import net.pop.projectpilot.presentation.screens.focus.FocusScreen
import net.pop.projectpilot.presentation.screens.navigation.main.BottomNavItem
import net.pop.projectpilot.presentation.screens.navigation.main.FloatingBottomNavigationBar

@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    profileImageUrl: String? = null
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
            composable(BottomNavItem.Home.route) {}
            composable(BottomNavItem.Projects.route) {}
            composable(BottomNavItem.Focus.route) {
                FocusScreen()
            }
            composable(BottomNavItem.Profile.route) {}
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