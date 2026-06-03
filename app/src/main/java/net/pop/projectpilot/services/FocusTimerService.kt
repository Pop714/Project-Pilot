package net.pop.projectpilot.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.pop.projectpilot.R
import net.pop.projectpilot.domain.manager.FocusManager
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class FocusTimerService : Service() {

    @Inject
    lateinit var focusManager: FocusManager

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null
    private lateinit var notificationManager: NotificationManager

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "focus_timer_channel"
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTimer()
            ACTION_PAUSE -> pauseTimer()
            ACTION_STOP -> stopTimer()
        }
        return START_STICKY
    }

    private fun startTimer() {
        focusManager.setRunning(true)
        startForeground(NOTIFICATION_ID, buildNotification(focusManager.timeRemaining.value))
        if (focusManager.isDndEnabled.value && notificationManager.isNotificationPolicyAccessGranted) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
        }

        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (focusManager.timeRemaining.value > 0 && focusManager.isRunning.value) {
                delay(1000L.milliseconds)
                val newTime = focusManager.timeRemaining.value - 1
                focusManager.updateTime(newTime)

                notificationManager.notify(NOTIFICATION_ID, buildNotification(newTime))
            }

            if (focusManager.timeRemaining.value <= 0L) {
                stopTimer()
            }
        }
    }

    private fun pauseTimer() {
        focusManager.setRunning(false)
        timerJob?.cancel()
        stopForeground(STOP_FOREGROUND_DETACH)
    }

    private fun stopTimer() {
        focusManager.setRunning(false)
        focusManager.updateTime(focusManager.totalDuration.value)
        timerJob?.cancel()
        if (notificationManager.isNotificationPolicyAccessGranted) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
        stopSelf()
    }

    @SuppressLint("DefaultLocale")
    private fun buildNotification(secondsRemaining: Long): Notification {
        val mins = secondsRemaining / 60
        val secs = secondsRemaining % 60
        val timeString = String.format("%02d:%02d", mins, secs)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Deep Focus Session")
            .setContentText("Time remaining: $timeString")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Focus Timer",
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

}