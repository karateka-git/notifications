package com.karateka.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.R
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.karateka.notifications.ui.theme.NotificationsTheme

private const val CHANNEL_ID = "TestChannel"
private const val NOTIFICATION_ID = 1

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    createNotification(this@MainActivity)
                } else {
                    getPermissionDeniedToast(this@MainActivity).show()
                }
            }

            NotificationsTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Нажмите `кнопка` чтобы увидеть уведомление!")
                        Button(onClick = {
                            when {
                                ContextCompat.checkSelfPermission(
                                    this@MainActivity, Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED -> {
                                    createNotification(this@MainActivity)
                                }
                                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) &&
                                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                                    getPermissionInfoToast(this@MainActivity).show()
                                }
                                else -> {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        getPermissionDeniedToast(this@MainActivity).show()
                                    }
                                }
                            }
                        }) {
                            Text("кнопка")
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
fun createNotification(context: Context) {
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_call_answer_low)
        .setContentTitle("Title text")
        .setContentText("Content text")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    with(NotificationManagerCompat.from(context)) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Уведомления"
            val descriptionText = "Тестирую уведомления"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system.
            createNotificationChannel(channel)
        }
        notify(NOTIFICATION_ID, builder.build())
    }
}

private fun getPermissionDeniedToast(context: Context) =
    Toast.makeText(context, "Приложению запрещено отправлять уведомления", Toast.LENGTH_LONG)

private fun getPermissionInfoToast(context: Context) =
    Toast.makeText(context, "Для отправки уведомлений, приложению необходимо разрешение", Toast.LENGTH_LONG)