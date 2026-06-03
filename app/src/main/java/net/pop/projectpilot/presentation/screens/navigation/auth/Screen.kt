package net.pop.projectpilot.presentation.screens.navigation.auth

sealed class Screen(val route: String) {
    object SavedAccounts : Screen("saved_accounts")
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
}