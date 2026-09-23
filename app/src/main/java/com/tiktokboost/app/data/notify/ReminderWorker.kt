package com.tiktokboost.app.data.notify

import android.content.Context
import androidx.work.Worker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.tiktokboost.app.data.Session
import java.util.concurrent.TimeUnit

/**
 * Background reminders (v1.0.4) — real notifications while the app is CLOSED,
 * via WorkManager:
 *  • Daily check-in available → "Daily reward ready" (deep links to Earn)
 *  • A boost task expiring within ~3h → "Boost ending soon" (deep links to Boost)
 *
 * The user's notifications toggle is respected. A remote push provider can
 * replace/augment this worker later without UI changes.
 */
class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val ctx = applicationContext
        try {
            Session.init(ctx)   // safe re-init in a fresh process
        } catch (e: Exception) {
            return Result.success()   // nothing to remind about
        }
        if (!Session.notificationsEnabled || !TickTokNotifications.canPost(ctx)) {
            return Result.success()
        }

        val now = System.currentTimeMillis()

        // A) daily check-in available
        if (Session.canCheckIn()) {
            TickTokNotifications.post(
                ctx, 9001L, "checkin",
                "Daily reward ready 🎁",
                "Your daily check-in is available.",
                route = "main_earn"
            )
        }

        // D) boost task expiring soon (in progress, deadline within 3 hours)
        Session.boostTasks()
            .firstOrNull {
                it.status == com.tiktokboost.app.data.BoostTaskStatus.IN_PROGRESS &&
                    it.expiresAt != null && it.expiresAt!! - now < 3 * 3_600_000L && it.expiresAt!! > now
            }
            ?.let {
                TickTokNotifications.post(
                    ctx, 9002L, "boost",
                    "Boost ending soon ⏳",
                    "A boost you can complete is expiring soon.",
                    route = "main_boost"
                )
            }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "ticktokboost_reminders"

        /**
         * Registers the periodic reminder check (every ~6 hours). KEEP policy.
         * Best-effort: if WorkManager isn't available (e.g. stripped from a
         * test process), scheduling is skipped — reminders degrade gracefully.
         */
        fun schedule(context: Context) {
            try {
                val request = PeriodicWorkRequestBuilder<ReminderWorker>(6, TimeUnit.HOURS)
                    .build()
                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
            } catch (e: Exception) {
                android.util.Log.w("ReminderWorker", "scheduling skipped: ${e.message}")
            }
        }
    }
}
