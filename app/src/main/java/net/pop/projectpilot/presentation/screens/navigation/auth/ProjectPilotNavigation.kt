package net.pop.projectpilot.presentation.screens.navigation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import net.pop.projectpilot.presentation.screens.auth.AuthViewModel
import net.pop.projectpilot.presentation.screens.auth.login.LoginScreen
import net.pop.projectpilot.presentation.screens.auth.register.RegisterScreen
import net.pop.projectpilot.presentation.screens.auth.saved.SavedAccountsScreen
import net.pop.projectpilot.presentation.screens.main.MainScreen

@Composable
fun ProjectPilotNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val isLoggedIn = remember { authViewModel.isUserLoggedIn() }
    val savedAccounts by authViewModel.savedAccounts.collectAsState()

    val startDestination = when {
        savedAccounts.isNotEmpty() -> Screen.SavedAccounts.route
        isLoggedIn -> Screen.Dashboard.route
        else -> Screen.Login.route
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.SavedAccounts.route) {
            SavedAccountsScreen(
                viewModel = authViewModel,
                onNavigateToStandardLogin = { navController.navigate(Screen.Login.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.Dashboard.route) {
            MainScreen()
        }
    }
}