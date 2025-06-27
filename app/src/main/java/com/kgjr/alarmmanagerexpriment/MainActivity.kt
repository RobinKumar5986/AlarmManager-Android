package com.kgjr.alarmmanagerexpriment

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kgjr.alarmmanagerexpriment.ui.AndroidAlarmScheduler
import com.kgjr.alarmmanagerexpriment.ui.theme.AlarmManagerExprimentTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlarmManagerExprimentTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TimeInputControl()
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

    Column(
        modifier = modifier.fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
    ) {
        OutlinedTextField(
            value = timeInSeconds,
            onValueChange = { timeInSeconds = it },
            label = { Text("Time (in seconds)") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = {
                timeInSeconds.toIntOrNull()?.let {
                    scheduler.scheduleAlarm(it)
                }
            }) {
                Text("Start")
            }

            Button(onClick = {
                scheduler.cancelAlarm()
            }) {
                Text("Stop")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun MainScreenPreView() {
    TimeInputControl()
}