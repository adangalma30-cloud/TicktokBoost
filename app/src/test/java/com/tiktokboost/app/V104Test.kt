package com.tiktokboost.app

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.data.notify.TickTokNotifications
import com.tiktokboost.app.ui.AppState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * v1.0.4 — notifications & communication system:
 * real system notifications (TickTokBoost icon, channels, deep links),
 * read/unread/delete persistence, badge consistency, toggle gating.
 *
 * sdk 28: channels active (O+), no runtime notification permission needed.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
class V104Test {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val nm get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Before
    fun seed() {
        context.getSharedPreferences("tiktokboost_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        Session.init(context)
        Session.isLoggedIn = true
        Session.isOnboarded = true
        Session.displayName = "John Doe"
        Session.tiktokUsername = "john.grams"
        AppState.refresh()
        nm.cancelAll()
    }

    @Test
    fun `notifications post to the system with the TickTokBoost icon and channel`() {
        val before = shadowOf(nm).activeNotifications.size
        Session.addNotification("coins", "Reward received", "5 points have been added to your balance.")
        val posted = shadowOf(nm).activeNotifications
        assertTrue("a system notification was posted", posted.size == before + 1)
        val latest = posted.last()
        assertEquals("dedicated TickTokBoost notification icon", R.drawable.ic_notification, latest.notification.smallIcon.resId)
        assertEquals("reward channel", TickTokNotifications.CH_REWARDS, latest.notification.channelId)
        assertEquals("content title", "Reward received", latest.notification.extras.getString("android.title"))
    }

    @Test
    fun `channels are created for all notification categories`() {
        TickTokNotifications.ensureChannels(context)
        val ids = nm.notificationChannels.map { it.id }
        assertTrue(TickTokNotifications.CH_REWARDS in ids)
        assertTrue(TickTokNotifications.CH_BOOSTS in ids)
        assertTrue(TickTokNotifications.CH_REMINDERS in ids)
        assertTrue(TickTokNotifications.CH_SYSTEM in ids)
    }

    @Test
    fun `deep link routes map notification kinds to the right screens`() {
        assertEquals("main_earn", TickTokNotifications.routeForKind("limit"))
        assertEquals("main_boost", TickTokNotifications.routeForKind("boost"))
        assertEquals("history", TickTokNotifications.routeForKind("coins"))
        assertEquals("history", TickTokNotifications.routeForKind("exchange"))
        assertEquals("main_profile", TickTokNotifications.routeForKind("trust"))
        assertEquals("notifications", TickTokNotifications.routeForKind("system"))
    }

    @Test
    fun `in-app records carry a tap-through target`() {
        Session.addNotification("boost", "Boost completed tap", "You earned 50 points.")
        val n = Session.notifications().first { it.title == "Boost completed tap" }
        assertNotNull("target stored for tap-through", n.target)
        assertEquals("main_boost", n.target)
    }

    @Test
    fun `read unread delete persist and the badge stays consistent`() {
        // baseline includes the migration's welcome notification
        val baseline = AppState.unreadCount
        Session.addNotification("coins", "One", "1")
        Session.addNotification("boost", "Two", "2")
        AppState.refresh()
        assertEquals(baseline + 2, AppState.unreadCount)

        val first = AppState.notifications.first()
        AppState.markNotificationRead(first.id)
        assertEquals("badge drops when read", baseline + 1, AppState.unreadCount)

        AppState.markNotificationUnread(first.id)
        assertEquals("badge rises when marked unread", baseline + 2, AppState.unreadCount)

        AppState.deleteNotification(first.id)
        assertEquals("badge reflects deletion", baseline + 1, AppState.unreadCount)
        assertEquals(baseline + 1, AppState.notifications.size)

        // persists across restart
        Session.init(context)
        AppState.refresh()
        assertEquals(baseline + 1, Session.notifications().size)
        assertEquals(baseline + 1, AppState.unreadCount)
    }

    @Test
    fun `disabling notifications suppresses both in-app records and system posts`() {
        Session.addNotification("system", "Baseline", "counted")
        val baseInApp = Session.notifications().size
        val basePosted = shadowOf(nm).activeNotifications.size

        AppState.setNotifications(false)
        Session.addNotification("coins", "Suppressed", "should not appear")

        assertEquals("no new in-app record", baseInApp, Session.notifications().size)
        assertEquals("no new system notification", basePosted, shadowOf(nm).activeNotifications.size)
    }
}
