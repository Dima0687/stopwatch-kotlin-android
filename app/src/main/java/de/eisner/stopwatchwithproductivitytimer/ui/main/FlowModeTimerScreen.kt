package de.eisner.stopwatchwithproductivitytimer.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.eisner.stopwatchwithproductivitytimer.domain.model.Task
import de.eisner.stopwatchwithproductivitytimer.domain.model.TimerState

private val DarkBackground = Color(0xFF0F2529)
private val CardBackground = Color(0xFF142E33)
private val NeonCyan = Color(0xFF00F5D4)
private val DarkCircle = Color(0xFF1C3A3E)
private val StartGreen = Color(0xFF4CAF50)
private val PauseGrey = Color(0xFF757575)
private val ResetRed = Color(0xFFE53935)
private val TaskSelectedGreen = Color(0xFF0F5241)

@Composable
fun FlowModeTimerScreen(
    timerState: TimerState,
    taskList: List<Task>,
    showAddTaskDialog: Boolean,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResetClick: () -> Unit,
    onTaskClick: (String) -> Unit,
    onTaskToggleCompleted: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    onOpenAddTaskDialog: () -> Unit,
    onCloseAddTaskDialog: () -> Unit,
    onAddNewTask: (String, Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "FlowMode Timer",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
            )

            // Timer Display + Circular Progress Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(280.dp)
                    .padding(16.dp)
            ) {
                CircularProgressRing(progress = timerState.progress)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timerState.formattedTime,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = timerState.activeTaskTitle ?: "Focus Session",
                        fontSize = 16.sp,
                        color = Color.LightGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Control Buttons (Start, Pause, Reset)
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {

                if (timerState.isRunning) {
                    ControlButton(
                        color = PauseGrey,
                        label = "Pause",
                        onClick = onPauseClick
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = Color.White
                        )
                    }
                } else {
                    ControlButton(
                        color = StartGreen,
                        label = "Start",
                        onClick = onStartClick
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start",
                            tint = Color.Black
                        )
                    }
                }

                ControlButton(
                    color = ResetRed,
                    label = "Reset",
                    onClick = onResetClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            TaskSection(
                tasks = taskList,
                onTaskClick = onTaskClick,
                onTaskToggleCompleted = onTaskToggleCompleted,
                onDeleteTask = onDeleteTask,
                onOpenAddTaskDialog = onOpenAddTaskDialog
            )

            Spacer(modifier = Modifier.weight(1f))

            // Footer
            Text(
                text = "Powered by Kotlin + Android",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (showAddTaskDialog) {
            CreateTaskDialog(
                onDismiss = onCloseAddTaskDialog,
                onCreate = onAddNewTask
            )
        }
    }
}

@Composable
private fun CircularProgressRing(progress: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 16.dp.toPx()

        drawCircle(
            color = DarkCircle,
            style = Stroke(width = strokeWidth)
        )

        drawArc(
            color = NeonCyan,
            startAngle = -90f,
            sweepAngle = progress * 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}


@Composable
private fun ControlButton(
    color: Color,
    label: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(color)
        ) {
            icon()
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskSection(
    tasks: List<Task>,
    onTaskClick: (String) -> Unit,
    onTaskToggleCompleted: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    onOpenAddTaskDialog: () -> Unit
) {
    Surface(
        color = CardBackground,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tasks",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = onOpenAddTaskDialog) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Task",
                        tint = NeonCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tasks.forEach { task ->
                    TaskChip(
                        task = task,
                        onClick = { onTaskClick(task.id) },
                        onToggleCompleted = { onTaskToggleCompleted(task.id) },
                        onDelete = { onDeleteTask(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskChip(
    task: Task,
    onClick: () -> Unit,
    onToggleCompleted: () -> Unit,
    onDelete: () -> Unit
) {
    val backgroundColor = if (task.isSelected) TaskSelectedGreen else Color(0xFF1E3A40)
    val borderColor = if (task.isSelected) NeonCyan else Color.Transparent

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (task.isCompleted) NeonCyan else Color.Gray)
                    .clickable { onToggleCompleted() },
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${task.title} (${task.durationMinutes}m)",
                color = if (task.isSelected || task.isCompleted) Color.White else Color.LightGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete",
                tint = Color.Gray,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onDelete() }
            )
        }
    }
}

@Composable
private fun CreateTaskDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var durationText by remember { mutableStateOf("25") }


    val dialogBackground = Color(0xFF132A2F)
    val accentGreen = Color(0xFF4CAF50)
    val primaryButtonBg = Color(0xFF2E6B5E)
    val textColor = Color.White
    val textMuted = Color(0xFFB0BEC5)


    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBackground,
        title = {
            Text(
                text = "Create new Task",
                color = textColor
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentGreen,
                        unfocusedBorderColor = textMuted,
                        focusedLabelColor = accentGreen,
                        unfocusedLabelColor = textMuted,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it },
                    label = { Text("Duration (in minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentGreen,
                        unfocusedBorderColor = textMuted,
                        focusedLabelColor = accentGreen,
                        unfocusedLabelColor = textMuted,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val duration = durationText.toIntOrNull() ?: 25
                    if (title.isNotBlank()) {
                        onCreate(title, duration)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryButtonBg,
                    contentColor = textColor
                )
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = textMuted)
            }
        }
    )
}