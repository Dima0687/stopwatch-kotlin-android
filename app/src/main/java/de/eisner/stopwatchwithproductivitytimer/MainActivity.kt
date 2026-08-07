package de.eisner.stopwatchwithproductivitytimer

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private var seconds = 0
    private var stopWatchIsRunning = false
    private var upperLimit: Int? = null
    private var defaultTextColor: Int = Color.BLACK

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private val requestPermissionsLauncher =registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startStopwatch()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()

        val startBtn = findViewById<Button>(R.id.startButton)
        val resetBtn = findViewById<Button>(R.id.resetButton)
        val settingsBtn = findViewById<Button>(R.id.settingsButton)
        val textView = findViewById<TextView>(R.id.textView)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        defaultTextColor = textView.currentTextColor

        runnable = object : Runnable {
            override fun run() {
                seconds++
                val minutes = seconds / 60
                val secs = seconds % 60
                textView.text = String.format(
                    Locale.ENGLISH,
                    "%02d:%02d",
                    minutes, secs
                )

                if (upperLimit != null && upperLimit!! > 0) {
                    if (seconds > upperLimit!!) {
                        textView.setTextColor(Color.RED)
                    }
                    if (seconds == upperLimit) {
                        showNotification()
                    }
                }

                val randomColor = Color.rgb(
                    Random.nextInt(256),
                    Random.nextInt(256),
                    Random.nextInt(256)
                )

                progressBar.indeterminateTintList = ColorStateList.valueOf(randomColor)

                handler.postDelayed(this, 1000)
            }
        }

        startBtn.setOnClickListener {
            if (!stopWatchIsRunning) {
                checkPermissionAndStart()
            }
        }

        resetBtn.setOnClickListener {
            stopWatchIsRunning = false
            handler.removeCallbacks(runnable)
            seconds = 0
            textView.text = "00:00"
            textView.setTextColor(defaultTextColor)
            progressBar.visibility = View.INVISIBLE
            settingsBtn.isEnabled = true
        }

        settingsBtn.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_settings, null)
            val editText = dialogView.findViewById<EditText>(R.id.upperLimitEditText)

            upperLimit?.let { editText.setText(it.toString()) }

            AlertDialog.Builder(this)
                .setTitle("Set Limit")
                .setView(dialogView)
                .setPositiveButton("OK") { _, _ ->
                    val inpuText = editText.text.toString()
                    upperLimit = inpuText.toIntOrNull()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun checkPermissionAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                startStopwatch()
            } else {
                requestPermissionsLauncher.launch(permission)
            }
        } else {
            startStopwatch()
        }
    }

    private fun startStopwatch() {
        stopWatchIsRunning = true
        findViewById<Button>(R.id.settingsButton).isEnabled = false
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
        handler.postDelayed(runnable, 1000)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Stopwatch Channel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("org.hyperskill", name, importance)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification() {
        val builder = NotificationCompat.Builder(this, "org.hyperskill")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time Limit Reached")
            .setContentText("Your stopwatch limit has been reached!")
            .setOnlyAlertOnce(true)

        val notification = builder.build()
        notification.flags = notification.flags or Notification.FLAG_INSISTENT

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(393939, notification)
    }
}