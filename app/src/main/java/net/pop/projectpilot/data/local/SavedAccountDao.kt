package net.pop.projectpilot.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.pop.projectpilot.data.model.SavedAccount

@Dao
interface SavedAccountDao {

    @Query("SELECT * FROM saved_accounts")
    fun getAllAccounts(): Flow<List<SavedAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: SavedAccount)

    @Query("DELETE FROM saved_accounts WHERE email = :email")
    suspend fun deleteAccount(email: String)

}