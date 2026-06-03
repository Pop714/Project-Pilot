package net.pop.projectpilot.presentation.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.pop.projectpilot.domain.updater.AppUpdater
import net.pop.projectpilot.domain.updater.UpdateStatus

@HiltViewModel
class MainViewModel @Inject constructor(
    private val appUpdater: AppUpdater,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _updateState = MutableStateFlow<UpdateStatus?>(null)
    val updateState = _updateState.asStateFlow()

    private val _profileImageUrl = MutableStateFlow<String?>(null)
    val profileImageUrl: StateFlow<String?> = _profileImageUrl.asStateFlow()

    init {
        checkForUpdates()
        fetchUserProfilePicture()
    }

    private fun checkForUpdates() {
        viewModelScope.launch {
            _updateState.value = appUpdater.checkForUpdates()
        }
    }

    private fun fetchUserProfilePicture() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                _profileImageUrl.value = document.getString("profileImageUrl")
            }
            .addOnFailureListener {
                _profileImageUrl.value = null
            }
    }

}