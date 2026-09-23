package com.tiktokboost.app.data.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tiktokboost.app.MainActivity
import com.tiktokboost.app.R

/**
 * ═══════════════════════════════════════════════════════════════════════════
 *  TickTokBoost notification & communication system (v1.0.4).
 *
 *  Real Android system notifications (status bar, channels, deep links) with
 *  the dedicated monochrome Creator Rise notification icon. Works while the
 *  app is open AND in the background; ReminderWorker adds closed-app
 *  capability for time-based events (daily check-in, expiring boosts).
 *
 *  A remote push provider (FCM etc.) plugs in behind post(): when a backend
 *  account exists, server events call the same path — no UI changes needed.
 * ═══════════════════════════════════════════════════════════════════════════
 */
object TickTokNotifications {

    // channels
    const val CH_REWARDS = "rewards"
    const val CH_BOOSTS = "boosts"
    const val CH_REMINDERS = "reminders"
    const val CH_SYSTEM = "system"

    /** Deep-link bus: MainActivity writes the tapped notification's route;
     *  TikTokBoostApp consumes it after the splash hands over. */
    var pendingRoute by mutableStateOf<String?>(null)

    /** In-app route targets per notification kind (Notification Center taps). */
    fun routeForKind(kind: String): String = when (kind) {
        "exchange", "coins", "dispute" -> "history"
        "limit", "checkin" -> "main_earn"
        "boost" -> "main_boost"
        "trust", "achievement", "streak" -> "main_profile"
        "premium" -> "premium"
        else -> "notifications"   // generic/system → Notification Center
    }

    private fun channelFor(kind: String): String = when (kind) {
        "coins", "limit" -> CH_REWARDS
        "boost" -> CH_BOOSTS
        "exchange", "trust", "achievement", "streak" -> CH_REWARDS
        "premium", "system", "dispute", "abuse" -> CH_SYSTEM
        else -> CH_SYSTEM
    }

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        fun ch(id: String, name: String, importance: Int) {
            nm.createNotificationChannel(NotificationChannel(id, name, importance))
        }
        ch(CH_REWARDS, "Rewards & coins", NotificationManager.IMPORTANCE_DEFAULT)
        ch(CH_BOOSTS, "Boosts & tasks", NotificationManager.IMPORTANCE_DEFAULT)
        ch(CH_REMINDERS, "Reminders", NotificationManager.IMPORTANCE_DEFAULT)
        ch(CH_SYSTEM, "Account & system", NotificationManager.IMPORTANCE_DEFAULT)
    }

    fun canPost(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < 33) return true
        return context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    /** Post a system notification. Tapping opens the deep-linked screen. */
    fun post(context: Context, id: Long, kind: String, title: String, message: String, route: String? = null) {
        val target = route ?: routeForKind(kind)
        ensureChannels(context)
        if (!canPost(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("deep_link", target)
        }
        val pending = PendingIntent.getActivity(
            context,
            (id % Int.MAX_VALUE).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelFor(kind))
            .setSmallIcon(R.drawable.ic_notification)     // dedicated TickTokBoost mark
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_SOCIAL)
            .build()

        try {
            NotificationManagerCompat.from(context).notify((id % Int.MAX_VALUE).toInt(), notification)
        } catch (se: SecurityException) {
            // permission revoked between check and post — never crash for a notification
        }
    }
}
