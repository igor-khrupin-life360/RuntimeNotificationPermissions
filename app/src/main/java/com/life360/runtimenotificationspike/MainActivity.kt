package com.life360.runtimenotificationspike

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
            if (!isGrantedPermission()) {
                Toast.makeText(applicationContext, "Can\'t post notification without POST_NOTIFICATIONS granted", Toast.LENGTH_LONG).show()
            }
        }

        binding.requestPermission.setOnClickListener {
            requestNotificationPermission()
        }

        binding.openPermissionSettings.setOnClickListener {
            val intent = Intent()
            intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

//for Android 5-7

//for Android 5-7
            intent.putExtra("app_package", packageName)
            intent.putExtra("app_uid", applicationInfo.uid)

// for Android 8 and above

// for Android 8 and above
            intent.putExtra("android.provider.extra.APP_PACKAGE", packageName)

            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.info.text = getInfo()
        updatePermissionState()
        createNotificationChannel()
    }

    private fun updatePermissionState() {
        binding.permissionState.text = getPermissionState()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            updatePermissionState()
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

    private fun getPermissionState(): Spanned {
        val html = if(isGrantedPermission()) {
            "POST_NOTIFICATIONS Granted? - <b><font color='green'>TRUE</font></b>"
        } else {
            "POST_NOTIFICATIONS Granted? - <b><font color='red'>FALSE</font></b>"
        }
        return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
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
