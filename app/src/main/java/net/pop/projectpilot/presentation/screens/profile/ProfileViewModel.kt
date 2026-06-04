package net.pop.projectpilot.presentation.screens.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.pop.projectpilot.data.local.SavedAccountDao
import net.pop.projectpilot.data.model.SavedAccount
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val supabaseClient: SupabaseClient,
    private val savedAccountDao: SavedAccountDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val user = auth.currentUser ?: return
        val email = user.email ?: return
        viewModelScope.launch {
            try {
                val doc = firestore.collection("users").document(user.uid).get().await()
                val isSaved = savedAccountDao.isAccountSaved(email)
                _state.update {
                    it.copy(
                        name = doc.getString("name") ?: "User",
                        email = user.email ?: "",
                        profileImageUrl = doc.getString("profileImageUrl"),
                        isAccountSaved = isSaved,
                        isLoading = false
                    )
                }
            } catch (_: Exception) {
                _state.update { it.copy(error = "Failed to load profile data", isLoading = false) }
            }
        }
    }

    fun updateName(newName: String) {
        if (newName.isBlank()) return
        val userId = auth.currentUser?.uid ?: return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                firestore.collection("users").document(userId).update("name", newName).await()
                savedAccountDao.updateAccountName(email =  _state.value.email, newName = newName)
                _state.update { it.copy(name = newName, isLoading = false, successMessage = "Name updated successfully") }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Failed to update name", isLoading = false) }
            }
        }
    }

    fun updateProfileImage(imageUri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val imageBytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                if (imageBytes != null) {
                    val bucket = supabaseClient.storage.from("profile_pictures")
                    val fileName = "$userId.jpg"

                    bucket.upload(fileName, imageBytes) { upsert = true }
                    val newUrl = bucket.publicUrl(fileName)

                    firestore.collection("users").document(userId).update("profileImageUrl", newUrl).await()

                    _state.update {
                        it.copy(profileImageUrl = newUrl, isLoading = false, successMessage = "Profile picture updated")
                    }
                }
            } catch (_: Exception) {
                _state.update { it.copy(error = "Failed to upload image", isLoading = false) }
            }
        }
    }

    fun toggleSavedAccount(password: String? = null) {
        val currentState = _state.value
        viewModelScope.launch {
            if (currentState.isAccountSaved) {
                savedAccountDao.deleteAccount(currentState.email)
                _state.update { it.copy(isAccountSaved = false, successMessage = "Account removed from saved devices") }
            } else {
                if (password != null) {
                    val newAccount = SavedAccount(
                        email = currentState.email,
                        name = currentState.name,
                        encryptedPass = password,
                        profileImageUrl = currentState.profileImageUrl
                    )
                    savedAccountDao.insertAccount(newAccount)
                    _state.update { it.copy(isAccountSaved = true, successMessage = "Account saved securely") }
                }
            }
        }
    }

    fun updatePassword(currentPass: String, newPass: String) {
        val user = auth.currentUser ?: return
        val email = user.email ?: return

        _state.update { it.copy(isUpdatingPassword = true, passwordUpdateError = null, passwordUpdateSuccess = false) }

        viewModelScope.launch {
            try {
                val credential = EmailAuthProvider.getCredential(email, currentPass)
                user.reauthenticate(credential).await()
                user.updatePassword(newPass).await()
                if (_state.value.isAccountSaved) {
                    val updatedAccount = SavedAccount(
                        email = email,
                        name = _state.value.name,
                        encryptedPass = newPass,
                        profileImageUrl = _state.value.profileImageUrl
                    )
                    savedAccountDao.insertAccount(updatedAccount)
                }
                _state.update {
                    it.copy(
                        isUpdatingPassword = false,
                        passwordUpdateSuccess = true,
                        successMessage = "Password updated successfully"
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isUpdatingPassword = false,
                        passwordUpdateError = e.message ?: "Failed to update password. Check your current password."
                    )
                }
            }
        }
    }

    fun resetPasswordDialogState() {
        _state.update { it.copy(passwordUpdateError = null, passwordUpdateSuccess = false) }
    }

    fun logout(onSuccess: () -> Unit) {
        auth.signOut()
        onSuccess()
    }

    fun clearMessages() {
        _state.update { it.copy(error = null, successMessage = null) }
    }


}