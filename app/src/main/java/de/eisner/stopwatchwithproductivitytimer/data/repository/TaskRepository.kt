package de.eisner.stopwatchwithproductivitytimer.data.repository

import de.eisner.stopwatchwithproductivitytimer.domain.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Singleton-Repository for task management
 */
object TaskRepository {

    private val initialTasks = listOf(
        Task(
            id = "1",
            title = "Code Refactoring",
            durationMinutes = 25,
            isCompleted = true,
            isSelected = false
        ),
        Task(
            id = "2",
            title = "Meeting Prep",
            durationMinutes = 15,
            isCompleted = false,
            isSelected = true
        ),
        Task(
            id = "3",
            title = "Design Review",
            durationMinutes = 45,
            isCompleted = false,
            isSelected = false
        )
    )

    private val _tasks = MutableStateFlow(initialTasks)
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    init {
        val selected = initialTasks.firstOrNull { it.isSelected }
        if (selected != null) {
            TimerRepository.setTargetTask(
                selected.id,
                selected.title,
                selected.durationMinutes * 60L
            )
        }
    }

    fun selectTask(taskId: String) {
        _tasks.update { list ->
            list.map { task ->
                val isTarget = task.id == taskId
                val newSelected = if (isTarget) !task.isSelected else false
                task.copy(isSelected = newSelected)
            }
        }

        val selectedTask = _tasks.value.firstOrNull { it.isSelected }
        if (selectedTask != null) {
            TimerRepository.setTargetTask(
                selectedTask.id,
                selectedTask.title,
                selectedTask.durationMinutes * 60L
            )
        } else {
            TimerRepository.setTargetTask(null, null, 1500L)
        }
    }

    fun toggleTaskCompletion(taskId: String) {
        _tasks.update { list ->
            list.map { task ->
                if (task.id == taskId) task.copy(isCompleted = !task.isCompleted) else task
            }
        }
    }

    fun addTask(title: String, durationMinutes: Int) {
        if (title.isBlank()) return

        val newTask = Task(
            id = System.currentTimeMillis().toString(),
            title = title.trim(),
            durationMinutes = durationMinutes.coerceAtLeast(1)
        )
        _tasks.update { it + newTask }
    }

    fun deleteTask(taskId: String) {
        _tasks.update { list -> list.filter { it.id != taskId } }
        val currentSelected = _tasks.value.firstOrNull { it.isSelected }
        if (currentSelected == null) {
            TimerRepository.setTargetTask(null, null, 1500L)
        }
    }
}