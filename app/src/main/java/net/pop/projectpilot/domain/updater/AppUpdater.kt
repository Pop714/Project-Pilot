package net.pop.projectpilot.domain.updater

import android.content.Context
import android.content.pm.PackageManager
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.tasks.await

class AppUpdater(private val context: Context) {

    private val remoteConfig = Firebase.remoteConfig

    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    suspend fun checkForUpdates(): UpdateStatus {
        return try {
            remoteConfig.fetchAndActivate().await()
            val latestVersion = remoteConfig.getLong("latest_pp_version").toInt()
            val forceUpdate = remoteConfig.getBoolean("pp_force_update_required")
            val currentVersion = getCurrentVersionCode()
            when {
                currentVersion < latestVersion && forceUpdate -> UpdateStatus.ForceUpdate
                currentVersion < latestVersion && !forceUpdate -> UpdateStatus.RecommendedUpdate
                else -> UpdateStatus.UpToDate
            }
        } catch (_: Exception) {
            UpdateStatus.UpToDate
        }
    }

    private fun getCurrentVersionCode(): Int {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.longVersionCode.toInt()
        } catch (_: PackageManager.NameNotFoundException) {
            0
        }
    }

}