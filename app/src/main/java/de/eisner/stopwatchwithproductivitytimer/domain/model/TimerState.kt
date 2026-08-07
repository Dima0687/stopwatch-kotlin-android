package de.eisner.stopwatchwithproductivitytimer.domain.model

import java.util.Locale

data class TimerState(
    val elapsedSeconds: Long = 0L,
    val targetSeconds: Long = 1500L, // 25 Minutes Focus Session "Default"
    val isRunning: Boolean = false,
    val activeTaskId: String? = null,
    val activeTaskTitle: String? = null,
    val isTargetReached: Boolean = false
) {

    /**
     * Calculates the progress in range 0.0f ... 1.0f for circle progress bar
     */
    val progress: Float
        get() = if (targetSeconds > 0) {
            (elapsedSeconds.toFloat() / targetSeconds.toFloat()).coerceIn(0f, 1f)
        } else 0f

    /**
     * Formats the remaining time to 00:25:00
     */
    val formattedTime: String
        get() {
            val hours = elapsedSeconds / 3600
            val minutes = (elapsedSeconds % 3600) / 60
            val seconds = elapsedSeconds % 60
            return String.format(Locale.getDefault(), "%02d:%02d:%02d",
                hours, minutes, seconds)
        }
}
