package com.karateka.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.R
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
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
            var isNotificationEnabled by remember {
                mutableStateOf(NotificationManagerCompat.from(this@MainActivity).areNotificationsEnabled())
            }
            val lifecycleOwner = LocalLifecycleOwner.current
            val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

            LaunchedEffect(lifecycleState) {
                // Do something with your state
                // You may want to use DisposableEffect or other alternatives
                // instead of LaunchedEffect
                when (lifecycleState) {
                    Lifecycle.State.DESTROYED -> {}
                    Lifecycle.State.INITIALIZED -> {}
                    Lifecycle.State.CREATED -> {}
                    Lifecycle.State.STARTED -> {}
                    Lifecycle.State.RESUMED -> {
                        isNotificationEnabled = NotificationManagerCompat.from(this@MainActivity).areNotificationsEnabled()
                    }
                }
            }
            NotificationsTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(modifier = Modifier.weight(1f), text = "Получать уведомления")
                            Switch(checked = isNotificationEnabled, onCheckedChange = { isChecked ->
                                launchNotificationSettings()
                            })
                        }
                        Text("Нажмите `кнопка` чтобы увидеть уведомление!")
                        Button(onClick = {
                            when {
                                ContextCompat.checkSelfPermission(
                                    this@MainActivity, Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED -> {
                                    createNotification(this@MainActivity)
                                }
                                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) &&
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

fun Context.launchNotificationSettings() {
    val intent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent().apply {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }
        } else {
            val uri: Uri = Uri.fromParts("package", packageName, null)
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                data = uri
            }
        }
    startActivity(intent)
}

private fun getPermissionDeniedToast(context: Context) =
    Toast.makeText(context, "Приложению запрещено отправлять уведомления", Toast.LENGTH_SHORT)

private fun getPermissionInfoToast(context: Context) =
    Toast.makeText(context, "Для отправки уведомлений, приложению необходимо разрешение", Toast.LENGTH_SHORT)