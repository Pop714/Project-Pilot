package net.pop.projectpilot.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_accounts")
data class SavedAccount(
    @PrimaryKey
    val email: String,
    val name: String,
    val encryptedPass: String,
    val profileImageUrl: String? = null
)
