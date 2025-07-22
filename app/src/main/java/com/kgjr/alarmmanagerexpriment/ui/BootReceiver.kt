package com.kgjr.alarmmanagerexpriment.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

class BootReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device booted. Attempting to reschedule alarm.")

            val scheduler = AndroidAlarmScheduler(context)
            scheduler.scheduleAlarm(timeInSec = 60)
            Log.d("BootReceiver", "Alarm rescheduled after boot.")
        }
    }
}