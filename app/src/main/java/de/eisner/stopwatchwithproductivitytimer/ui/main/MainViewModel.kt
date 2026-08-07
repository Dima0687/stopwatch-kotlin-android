package de.eisner.stopwatchwithproductivitytimer.ui.main

import android.app.Application
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import de.eisner.stopwatchwithproductivitytimer.data.repository.TaskRepository
import de.eisner.stopwatchwithproductivitytimer.data.repository.TimerRepository
import de.eisner.stopwatchwithproductivitytimer.domain.model.Task
import de.eisner.stopwatchwithproductivitytimer.domain.model.TimerState
import de.eisner.stopwatchwithproductivitytimer.service.TimerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val timerState: StateFlow<TimerState> = TimerRepository.timerState
    val tasks: StateFlow<List<Task>> = TaskRepository.tasks

    private val _showAddTaskDialog = MutableStateFlow(false)
    val showAddTaskDialog: StateFlow<Boolean> = _showAddTaskDialog.asStateFlow()

    fun startTimer() {
        val context = getApplication<Application>()
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START
        }
        context.startForegroundService(intent)
    }

    fun pauseTimer() {
        val context = getApplication<Application>()
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_PAUSE
        }
        context.startService(intent)
    }

    fun resetTimer() {
        val context = getApplication<Application>()
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_RESET
        }
        context.startService(intent)
    }

    fun onTaskClicked(taskId: String) {
        val currentState = timerState.value

        if (currentState.isRunning || currentState.elapsedSeconds > 0) {
            Toast.makeText(
                getApplication<Application>(),
                "Please finish or reset your current task first",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        TaskRepository.selectTask(taskId)
    }

    fun onTaskToggleCompleted(taskId: String) {
        TaskRepository.toggleTaskCompletion(taskId)
    }

    fun onDeleteTask(taskId: String) {
        val currentState = timerState.value

        if (taskId == currentState.activeTaskId && currentState.isRunning ||
            currentState.elapsedSeconds > 0) {
            Toast.makeText(
                getApplication<Application>(),
                "You cannot delete a running task. Please reset the timer first.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        TaskRepository.deleteTask(taskId)
    }

    fun openAddTaskDialog() {
        _showAddTaskDialog.value = true
    }

    fun closeAddTaskDialog() {
        _showAddTaskDialog.value = false
    }

    fun addNewTask(title: String, durationMinutes: Int) {
        TaskRepository.addTask(title, durationMinutes)
        closeAddTaskDialog()
    }
}