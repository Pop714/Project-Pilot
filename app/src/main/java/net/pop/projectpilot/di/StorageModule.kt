package net.pop.projectpilot.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://tyzbupxlnwpddpgzwfju.supabase.co",
            supabaseKey = "sb_publishable_BY1iv4KgRKc0JPIiXv7BJA_050pBBeh"
        ) {
            install(Storage)
        }
    }

}