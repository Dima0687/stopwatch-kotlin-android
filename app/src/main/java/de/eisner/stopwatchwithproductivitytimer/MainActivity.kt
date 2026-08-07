package de.eisner.stopwatchwithproductivitytimer

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.eisner.stopwatchwithproductivitytimer.ui.main.FlowModeTimerScreen
import de.eisner.stopwatchwithproductivitytimer.ui.main.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startTimer()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val timerState = viewModel.timerState.collectAsStateWithLifecycle().value
            val taskList = viewModel.tasks.collectAsStateWithLifecycle().value
            val showAddTaskDialog = viewModel.showAddTaskDialog.collectAsStateWithLifecycle().value

            FlowModeTimerScreen(
                timerState = timerState,
                taskList = taskList,
                showAddTaskDialog = showAddTaskDialog,
                onStartClick = { checkPermissionAndStart() },
                onPauseClick = { viewModel.pauseTimer() },
                onResetClick = { viewModel.resetTimer() },
                onTaskClick = { taskId -> viewModel.onTaskClicked(taskId) },
                onTaskToggleCompleted = { taskId -> viewModel.onTaskToggleCompleted(taskId) },
                onDeleteTask = { taskId -> viewModel.onDeleteTask(taskId) },
                onOpenAddTaskDialog = { viewModel.openAddTaskDialog() },
                onCloseAddTaskDialog = { viewModel.closeAddTaskDialog() },
                onAddNewTask = { title, duration -> viewModel.addNewTask(title, duration) }
            )
        }
    }

    private fun checkPermissionAndStart() {
        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.startTimer()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }
}