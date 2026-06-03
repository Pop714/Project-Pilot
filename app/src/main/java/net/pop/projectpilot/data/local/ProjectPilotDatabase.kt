package net.pop.projectpilot.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import net.pop.projectpilot.data.model.SavedAccount

@Database(entities = [SavedAccount::class], version = 1, exportSchema = false)
abstract class ProjectPilotDatabase : RoomDatabase() {
    abstract fun savedAccountDao(): SavedAccountDao
}