package com.tsu.sudokugame.controler

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import android.os.Build
import android.app.NotificationChannel
import android.app.NotificationManager

class SudokuApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // channels
            val channel =
                NotificationChannel(CHANNEL_ID, "Default", NotificationManager.IMPORTANCE_LOW)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "sudoku.0"
    }
}