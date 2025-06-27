package com.kgjr.alarmmanagerexpriment.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmManager", "AlarmReceiver triggered")
        val serviceIntent = Intent(context, AlarmForegroundService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}