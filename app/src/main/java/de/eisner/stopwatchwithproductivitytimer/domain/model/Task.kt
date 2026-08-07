package de.eisner.stopwatchwithproductivitytimer.domain.model

data class Task(
    val id: String,
    val title: String,
    val durationMinutes: Int = 25,
    val isCompleted: Boolean = false,
    val isSelected: Boolean = false
)
