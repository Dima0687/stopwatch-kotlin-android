package de.eisner.stopwatchwithproductivitytimer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import de.eisner.stopwatchwithproductivitytimer.MainActivity
import de.eisner.stopwatchwithproductivitytimer.R
import de.eisner.stopwatchwithproductivitytimer.data.repository.TaskRepository
import de.eisner.stopwatchwithproductivitytimer.data.repository.TimerRepository
import de.eisner.stopwatchwithproductivitytimer.domain.model.TimerState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class TimerService : LifecycleService() {

    companion object {
        private const val TAG = "TimerService"
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESET = "ACTION_RESET"

        private const val CHANNEL_ID = "timer_service_channel"
        private const val NOTIFICATION_ID = 1001
    }

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        observeTimerState()
        observeAlarmEvents()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START -> {
                startForegroundServiceWithNotification()
                TimerRepository.startTimer()
            }

            ACTION_PAUSE -> {
                TimerRepository.pauseTimer()
            }

            ACTION_RESET -> {
                TimerRepository.resetTimer()
                stopForegroundAndService()
            }
        }

        return START_NOT_STICKY
    }

    private fun observeTimerState() {
        TimerRepository.timerState
            .onEach { state ->
                if (state.isRunning || state.elapsedSeconds > 0) {
                    updateNotification(state)
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun observeAlarmEvents() {
        TimerRepository.alarmEvent
            .onEach {
                startAlarmSound()
            }
            .launchIn(lifecycleScope)
    }

    private fun startAlarmSound() {
        if (mediaPlayer?.isPlaying == true) return

        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@TimerService, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting alarm sound", e)
        }
    }

    private fun stopAlarmSound() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
    }

    private fun startForegroundServiceWithNotification() {
        val notification = buildNotification(TimerRepository.timerState.value)

        val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }

        try {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                serviceType
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground service", e)
        }
    }

    private fun updateNotification(state: TimerState) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(state))
    }

    private fun buildNotification(state: TimerState): Notification {
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingContentIntent = PendingIntent.getActivity(
            this,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        fun createServicePendingIntent(actionStr: String, requestCode: Int): PendingIntent {
            val intent = Intent(this, TimerService::class.java).apply {
                action = actionStr
            }
            return PendingIntent.getService(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val startPendingIntent = createServicePendingIntent(ACTION_START, 1)
        val pausePendingIntent = createServicePendingIntent(ACTION_PAUSE, 2)
        val resetPendingIntent = createServicePendingIntent(ACTION_RESET, 3)

        val taskText = state.activeTaskTitle?.let { "Task: $it" } ?: "Focus Session"

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(taskText)
            .setContentText("Elapsed Time: ${state.formattedTime}")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(state.isRunning)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingContentIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        val startOrResumeText = if (state.elapsedSeconds > 0) "Resume" else "Start"

        if (state.isRunning) {
            builder.addAction(R.mipmap.ic_launcher, "Pause", pausePendingIntent)
        } else {
            builder.addAction(R.mipmap.ic_launcher, startOrResumeText, startPendingIntent)
        }
        builder.addAction(R.mipmap.ic_launcher, "Reset", resetPendingIntent)

        return builder.build()
    }

    private fun stopForegroundAndService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Timer Notification",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows the running timer in notifications with controls"
        }

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarmSound()
    }
}