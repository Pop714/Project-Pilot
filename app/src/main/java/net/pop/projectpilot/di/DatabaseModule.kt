package net.pop.projectpilot.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import net.pop.projectpilot.data.local.ProjectPilotDatabase
import net.pop.projectpilot.data.local.SavedAccountDao

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ProjectPilotDatabase {
        return Room.databaseBuilder(
            context,
            ProjectPilotDatabase::class.java,
            "project_pilot_db"
        ).build()
    }

    @Provides
    fun provideSavedAccountDao(database: ProjectPilotDatabase): SavedAccountDao {
        return database.savedAccountDao()
    }
}