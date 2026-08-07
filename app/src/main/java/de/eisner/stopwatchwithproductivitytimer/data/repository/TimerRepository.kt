package de.eisner.stopwatchwithproductivitytimer.data.repository

import android.os.SystemClock
import de.eisner.stopwatchwithproductivitytimer.domain.model.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Singleton-Repository as single source of truth for timer state
 */
object TimerRepository {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var timerJob: Job? = null

    private var startTimeMillis: Long = 0L
    private var accumulatedSeconds: Long = 0L

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _alarmEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val alarmEvent: SharedFlow<Unit> = _alarmEvent.asSharedFlow()

    fun startTimer() {
        if (_timerState.value.isRunning) return

        startTimeMillis = SystemClock.elapsedRealtime()
        _timerState.update { it.copy(isRunning = true, isTargetReached = false) }

        timerJob?.cancel()
        timerJob = scope.launch {
            while (_timerState.value.isRunning) {
                val now = SystemClock.elapsedRealtime()
                val currentRunSeconds = (now - startTimeMillis) / 1000
                val totalSeconds = accumulatedSeconds + currentRunSeconds

                val currentTarget = _timerState.value.targetSeconds
                val targetReached = currentTarget in 1..totalSeconds
                        && !_timerState.value.isTargetReached

                if (targetReached) {
                    pauseTimer()
                    _alarmEvent.tryEmit(Unit)
                }

                _timerState.update { currentState ->
                    currentState.copy(
                        elapsedSeconds = totalSeconds,
                        isTargetReached = currentState.isTargetReached || targetReached
                    )
                }

                delay(500.milliseconds)
            }
        }
    }

    fun pauseTimer() {
        if (!_timerState.value.isRunning) return

        val now = SystemClock.elapsedRealtime()
        accumulatedSeconds += (now - startTimeMillis) / 1000

        _timerState.update { it.copy(isRunning = false, elapsedSeconds = accumulatedSeconds) }
        timerJob?.cancel()
        timerJob = null
    }

    fun resetTimer() {
        timerJob?.cancel()
        timerJob = null
        accumulatedSeconds = 0L
        startTimeMillis = 0L
        _timerState.update {
            it.copy(
                elapsedSeconds = 0L,
                isRunning = false,
                isTargetReached = false
            )
        }
    }

    fun setTargetTask(taskId: String?, title: String?, targetSeconds: Long) {
        _timerState.update {
            it.copy(
                activeTaskId = taskId,
                activeTaskTitle = title,
                targetSeconds = targetSeconds
            )
        }
    }
}