package com.tiktokboost.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.data.notify.ReminderWorker
import com.tiktokboost.app.data.notify.TickTokNotifications
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.TikTokBoostApp
import com.tiktokboost.app.ui.theme.TikTokBoostTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Session.init(applicationContext)
        AppState.refresh()
        TickTokNotifications.ensureChannels(this)
        requestNotificationPermissionIfNeeded()
        ReminderWorker.schedule(applicationContext)
        handleDeepLink(intent)
        setContent {
            TikTokBoostTheme {
                TikTokBoostApp()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)   // notification tapped while the app is running (singleTop)
    }

    private fun handleDeepLink(intent: Intent?) {
        intent?.getStringExtra("deep_link")?.let { route ->
            TickTokNotifications.pendingRoute = route
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001
            )
        }
    }
}
