package com.kgjr.alarmmanagerexpriment.ui

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import java.util.concurrent.TimeUnit

class AlarmForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "location_notification_channel"
        const val NOTIFICATION_ID = 1001
    }
    private lateinit var scheduler: AndroidAlarmScheduler
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val handler = Handler(Looper.getMainLooper())
    private var locationUpdatesCount = 0

    override fun onCreate() {
        super.onCreate()
        scheduler = AndroidAlarmScheduler(applicationContext)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("AlarmManager", "Starting AlarmForegroundService...")
        startForeground(NOTIFICATION_ID, buildNotification("Waiting for location..."))

        // Check for location permissions
        if (hasLocationPermissions()) {
            startLocationUpdates()
        } else {
            Log.e("AlarmManager", "Location permissions missing")
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
        }

        // Stop the service after 10 seconds, but keep notification
        handler.postDelayed({
            stopLocationUpdates()
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
            Log.d("AlarmManager", "Service stopped, notification persists")
        }, TimeUnit.SECONDS.toMillis(11))

        return START_NOT_STICKY
    }

    private fun hasLocationPermissions(): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val foregroundServiceLocationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.FOREGROUND_SERVICE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required below Android 14
        }

        return (fineLocationGranted || coarseLocationGranted) && foregroundServiceLocationGranted
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(1)
            fastestInterval = TimeUnit.SECONDS.toMillis(1)
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationUpdatesCount++
                val location = locationResult.lastLocation
                val locationText = if (location != null) {
                    "Lat: ${location.latitude}, Lon: ${location.longitude}"
                } else {
                    "Location unavailable"
                }

                // Update notification
                val notification = buildNotification(locationText)
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(NOTIFICATION_ID, notification)
                Log.d("AlarmManager", "Location update: $locationText")
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e("AlarmManager", "SecurityException: ${e.message}")
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
        }
    }

    private fun stopLocationUpdates() {
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun buildNotification(locationText: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Service")
            .setContentText(locationText)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOnlyAlertOnce(true)
            .setOngoing(true) // Make notification non-dismissible
            .setAutoCancel(false) // Prevent automatic dismissal
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Notification Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        Log.d("AlarmManager", "Service destroyed")
        scheduler.scheduleAlarm(timeInSec = 5)
    }
}