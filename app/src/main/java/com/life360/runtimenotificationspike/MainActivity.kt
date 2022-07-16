package com.life360.runtimenotificationspike

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.text.Spanned
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.life360.runtimenotificationspike.databinding.ActivityMainBinding


private const val CHANNEL_ID: String = "channel_id"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.postNotification.setOnClickListener {
            postNotification()
            if (!isGrantedPermission() && !isNotificationsEnabled() && isAndroid13()) {
                Toast.makeText(applicationContext, "Can\'t post notification without POST_NOTIFICATIONS granted AND Notifications Enabled", Toast.LENGTH_LONG).show()
            } else if (!isGrantedPermission() && isAndroid13()) {
                Toast.makeText(applicationContext, "Can\'t post notification without POST_NOTIFICATIONS granted", Toast.LENGTH_LONG).show()
            } else if (!isNotificationsEnabled()) {
                Toast.makeText(applicationContext, "Can\'t post notification without Notifications Enabled", Toast.LENGTH_LONG).show()
            }
        }

        binding.requestPermission.setOnClickListener {
            requestNotificationPermission()
        }

        binding.openPermissionSettings.setOnClickListener {
            val intent = Intent()
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("app_package", packageName)
            intent.putExtra("app_uid", applicationInfo.uid)
            intent.putExtra("android.provider.extra.APP_PACKAGE", packageName)

            startActivity(intent)
        }

        binding.openAppSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

    private fun isAndroid13(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    override fun onResume() {
        super.onResume()
        binding.info.text = getInfo()
        updateSettingsState()
        createNotificationChannel()
    }

    private fun updateSettingsState() {
        binding.permissionState.text = getPermissionStateMessage()
        binding.permissionState.isEnabled = isAndroid13()
        binding.notificationsState.text = getNotificationsEnabledStateMessage()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            updateSettingsState()
        }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Test Channel"
            val descriptionText = "channel description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getInfo(): String {
        return "device API version: API-${Build.VERSION.SDK_INT} (Android-${Build.VERSION.RELEASE_OR_CODENAME})" +
                "\nApplication targetSDKVersion: API-${applicationContext.applicationInfo.targetSdkVersion}"
    }

    private fun getPermissionStateMessage(): Spanned {
        val html = if(isGrantedPermission()) {
            "POST_NOTIFICATIONS Granted (for Android-13 devices only)? - <b><font color='green'>TRUE</font></b>"
        } else {
            "POST_NOTIFICATIONS Granted (for Android-13 devices only)? - <b><font color='red'>FALSE</font></b>"
        }
        return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
    }

    private fun getNotificationsEnabledStateMessage(): Spanned {
        val html = if (isNotificationsEnabled()) {
            "Notifications Enabled for the app? - <b><font color='green'>TRUE</font></b>"
        } else {
            "Notifications Enabled for the app? - <b><font color='red'>FALSE</font></b>"
        }
        return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
    }

    private fun isNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(this).areNotificationsEnabled()
    }

    private fun isGrantedPermission(): Boolean {
        return ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    private fun isPermissionDeniedTwice(): Boolean {
        return shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun requestNotificationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun postNotification() {
        val notificationId = 1001
        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(androidx.loader.R.drawable.notification_icon_background)
            .setContentTitle("Test Notification")
            .setContentText("This is notification message")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
    }
}
