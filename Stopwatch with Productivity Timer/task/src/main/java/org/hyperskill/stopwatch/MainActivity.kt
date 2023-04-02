package org.hyperskill.stopwatch

import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat

const val CHANNEL_ID = "org.hyperskill"

class MainActivity : AppCompatActivity() {

    private fun secondsToFormattedString(seconds: Int): String {
        val min = seconds / 60
        val sec = seconds - min * 60
        return "${String.format("%02d", min)}:${String.format("%02d", sec)}"
    }

    private fun getRandomColor(currentColor: Int): Int {
        val progressBarColors = arrayOf(Color.RED, Color.BLUE, Color.CYAN, Color.GREEN)
        return progressBarColors.filter { it != currentColor }.random()
    }

    private fun showNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "stopwatch"
            val channelDecription = "stopwatch description"
            val channelImportance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, channelName, channelImportance).apply {
                description = channelDecription
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Stopwatch")
                .setContentText("Time over!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)

            val notification = notificationBuilder.build()
            notification.flags = notification.flags or Notification.FLAG_INSISTENT
            notificationManager.notify(393939, notification)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var seconds: Int = 0
        var limit: Int? = null
        var started: Boolean = false
        val timerText = findViewById<TextView>(R.id.textView)
        val startButton = findViewById<Button>(R.id.startButton)
        val resetButton = findViewById<Button>(R.id.resetButton)
        val settingsButton = findViewById<Button>(R.id.settingsButton)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        progressBar.visibility = View.INVISIBLE
        var progressBarDefaultColor = Color.CYAN

        val handler = Handler(Looper.getMainLooper())

        settingsButton.setOnClickListener {
            val contentView = LayoutInflater.from(this).inflate(R.layout.dialog, null, false)
            AlertDialog.Builder(this)
                .setTitle(R.string.settings)
                .setView(contentView)
                .setPositiveButton(R.string.ok) { _, _ ->
                    val upperLimit = contentView.findViewById<EditText>(R.id.upperLimitEditText)
                    limit = upperLimit.text.toString().toInt()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        resetButton.setOnClickListener {
            started = false
            seconds = 0
            timerText.setTextColor(Color.BLACK)
            timerText.text = secondsToFormattedString(seconds)
            progressBar.visibility = View.INVISIBLE
            settingsButton.isEnabled = true
            settingsButton.isClickable = true
        }

        val timer: Runnable = object: Runnable {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun run() {
                val newColor = getRandomColor(progressBarDefaultColor)
                progressBar.indeterminateTintList = ColorStateList.valueOf(newColor)
                progressBarDefaultColor = newColor
                val lm = limit
                if (lm != null && seconds > lm) {
                    timerText.setTextColor(Color.RED)
                    if (seconds - lm == 1 && lm != 0) showNotification()
                }
                timerText.text = secondsToFormattedString(seconds)
                seconds++
                if (started) handler.postDelayed(this, 1000)
            }
        }

        startButton.setOnClickListener {
            if (!started) {
                progressBar.visibility = View.VISIBLE
                started = true
                settingsButton.isEnabled = false
                settingsButton.isClickable = false
                handler.post(timer)
            }
        }

    }
}