package net.pop.projectpilot.presentation.screens.auth

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.pop.projectpilot.data.firestore.User
import net.pop.projectpilot.data.local.SavedAccountDao
import net.pop.projectpilot.data.model.SavedAccount
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val supabaseClient: SupabaseClient,
    @ApplicationContext private val context: Context,
    private val savedAccountDao: SavedAccountDao
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val savedAccounts: StateFlow<List<SavedAccount>> = savedAccountDao.getAllAccounts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun login(email: String, pass: String, saveAccount: Boolean = false) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Fields cannot be empty")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, pass).await()
                if (result.user?.isEmailVerified == true) {
                    if (saveAccount) saveAccountLocally(email, pass)
                    _authState.value = AuthState.Success
                } else {
                    try {
                        result.user?.sendEmailVerification()?.await()
                    } catch (_: Exception) {
                        _authState.value = AuthState.Error("Failed to verify your email.")
                    }
                    auth.signOut()
                    _authState.value =
                        AuthState.Error("Please verify your email address before logging in. Don't forget to check your spam or junk folder!")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun register(email: String, pass: String, name: String, imageUri: Uri?) {
        if (email.isBlank() || pass.isBlank() || name.isBlank()) {
            _authState.value = AuthState.Error("Please fill out all required fields")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
                val user = authResult.user
                val userId = authResult.user?.uid

                if (userId != null && user != null) {
                    try {
                        user.sendEmailVerification().await()
                        var profilePicUrl = ""
                        if (imageUri != null) {
                            val imageBytes = context.contentResolver.openInputStream(imageUri)
                                ?.use { it.readBytes() }

                            if (imageBytes != null) {
                                val bucket = supabaseClient.storage.from("profile_pictures")
                                val fileName = "$userId.jpg"

                                bucket.upload(fileName, imageBytes) {
                                    upsert = true
                                }
                                profilePicUrl = bucket.publicUrl(fileName)
                            }
                        }

                        val userMap = User(
                            id = userId,
                            name = name,
                            email = email,
                            profileImageUrl = profilePicUrl
                        )

                        firestore.collection("users").document(userId).set(userMap).await()
                        auth.signOut()
                        _authState.value = AuthState.Success
                    } catch (innerException: Exception) {
                        try {
                            user.delete().await()
                            if (imageUri != null) {
                                supabaseClient.storage.from("profile_pictures")
                                    .delete(listOf("$userId.jpg"))
                            }
                        } catch (_: Exception) {
                            Toast.makeText(context, "Failed to cleanup!", Toast.LENGTH_SHORT).show()
                        }

                        throw innerException
                    }
                } else {
                    _authState.value = AuthState.Error("Failed to retrieve user ID")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    private suspend fun saveAccountLocally(email: String, pass: String) {
        try {
            val userId = auth.currentUser?.uid ?: return
            val doc = firestore.collection("users").document(userId).get().await()
            val name = doc.getString("name") ?: "User"
            val profileImageUrl = doc.getString("profileImageUrl")
            val newAccount = SavedAccount(
                email = email,
                name = name,
                encryptedPass = pass,
                profileImageUrl = profileImageUrl
            )
            savedAccountDao.insertAccount(newAccount)
        } catch (_: Exception) {
            Toast.makeText(context, "Failed to save account locally!", Toast.LENGTH_SHORT).show()
        }
    }

    fun removeSavedAccount(email: String) {
        viewModelScope.launch {
            savedAccountDao.deleteAccount(email)
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

}