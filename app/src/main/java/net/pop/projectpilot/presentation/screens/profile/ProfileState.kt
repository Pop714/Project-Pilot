package net.pop.projectpilot.presentation.screens.profile

data class ProfileState(
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String? = null,
    val isAccountSaved: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val successMessage: String? = null,
    val isUpdatingPassword: Boolean = false,
    val passwordUpdateError: String? = null,
    val passwordUpdateSuccess: Boolean = false
)