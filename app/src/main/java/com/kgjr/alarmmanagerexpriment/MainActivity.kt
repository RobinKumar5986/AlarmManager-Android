package com.kgjr.alarmmanagerexpriment

import android.annotation.SuppressLint
import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.kgjr.alarmmanagerexpriment.ui.AndroidAlarmScheduler
import com.kgjr.alarmmanagerexpriment.ui.theme.AlarmManagerExprimentTheme
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlarmManagerExprimentTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        TimeInputControl()
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        UsageEventList()
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimeInputControl(modifier: Modifier = Modifier) {
    var timeInSeconds by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scheduler = remember { AndroidAlarmScheduler(context) }

    val usageStatsList by remember { mutableStateOf(getUsageStatsList(context)) }
    val notificationManager = remember { NotificationManager(context) }
    LaunchedEffect(Unit) {
        notificationManager.showNotification(context,"Hello! Test Notification!")
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
    ) {
        OutlinedTextField(
            value = timeInSeconds,
            onValueChange = { timeInSeconds = it },
            label = { Text("Time (in seconds)", color = Color.Black) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = Color.Black)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Button(onClick = {
                timeInSeconds.toIntOrNull()?.let {
                    scheduler.scheduleAlarm(it)
                }
            }) {
                Text("Start", color = Color.Black)
            }

            Button(onClick = {
                scheduler.cancelAlarm()
            }) {
                Text("Stop", color = Color.Black)
            }

            Button(onClick = {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                    data = "package:${context.packageName}".toUri()
                }
                context.startActivity(intent)
            }) {
                Text("Open Settings", color = Color.Black)
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            "App Screen Time (Last 24h):",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(usageStatsList) { stat ->
                val appName = getAppName(context, stat.packageName)
                val timeInSeconds = TimeUnit.MILLISECONDS.toSeconds(stat.totalTimeInForeground.toLong())
                Text(
                    text = "$appName: $timeInSeconds seconds",
                    color = Color.Black,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

@Composable
fun UsageEventList() {
    val context = LocalContext.current
    var events by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        events = getAppUsageEvents(context)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text("App Usage Events (last 24h):", color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(events) { event ->
                Text(text = event, color = Color.Black, modifier = Modifier.padding(4.dp))
            }
        }
    }
}

fun getUsageStatsList(context: Context): List<UsageStats> {
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val endTime = System.currentTimeMillis()
    val startTime = endTime - 1000 * 60 * 60 * 24 // last 24 hours

    val stats = usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY,
        startTime,
        endTime
    )
    return stats.filter { it.totalTimeInForeground > 0 }
}

fun getAppUsageEvents(context: Context): List<String> {
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val endTime = System.currentTimeMillis()
    val startTime = endTime - 1000 * 60 * 60 * 24 // last 24 hours

    val events = usageStatsManager.queryEvents(startTime, endTime)
    val usageEvents = mutableListOf<String>()
    val event = UsageEvents.Event()

    while (events.hasNextEvent()) {
        events.getNextEvent(event)
        if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
            val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(event.timeStamp))
            val appName = getAppName(context, event.packageName)
            usageEvents.add("$appName opened at $time")
        }
    }

    return usageEvents
}

fun getAppName(context: Context, packageName: String): String {
    return try {
        val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
        context.packageManager.getApplicationLabel(appInfo).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        packageName
    }
}
