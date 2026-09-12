package com.tiktokboost.app

import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.tiktokboost.app.data.ContentItem
import com.tiktokboost.app.data.MediaType
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.ui.AppState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.robolectric.annotation.Config

/** v0.0.12: settings (theme persistence, notification gate) + content CRUD + creator profile nav. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34], qualifiers = "w360dp-h640dp")
class V12Test {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun seed() {
        context.getSharedPreferences("tiktokboost_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        Session.init(context)
        Session.isLoggedIn = true
        Session.isOnboarded = true
        Session.displayName = "John Doe"
        Session.tiktokUsername = "john.grams"
        Session.bio = "Skits"
        Session.category = "Comedy"
        AppState.refresh()
    }

    @Test
    fun `theme mode persists across session restarts`() {
        AppState.setTheme("dark")
        assertEquals("dark", Session.themeMode)
        // simulate app restart: fresh Session over the same storage
        Session.init(context)
        AppState.refresh()
        assertEquals("dark", AppState.themeMode)
        AppState.setTheme("light")
        Session.init(context)
        assertEquals("light", Session.themeMode)
        AppState.setTheme("system")
        assertEquals("system", Session.themeMode)
    }

    @Test
    fun `notification toggle actually suppresses notifications`() {
        // migration seeds one welcome notification — use it as the baseline
        val baseline = Session.notifications().size
        Session.addNotification("system", "before", "works when enabled")
        assertEquals(baseline + 1, Session.notifications().size)
        AppState.setNotifications(false)
        Session.addNotification("system", "after", "suppressed when disabled")
        assertEquals("no new notifications while toggled off", baseline + 1, Session.notifications().size)
        AppState.setNotifications(true)
        Session.addNotification("system", "again", "back on")
        assertEquals(baseline + 2, Session.notifications().size)
    }

    @Test
    fun `content CRUD works and orders newest first`() {
        assertEquals(0, Session.contentItems().size)
        val a = ContentItem("c1", MediaType.PHOTO, "/x/a.jpg", "first", 100L, 100L)
        val b = ContentItem("c2", MediaType.VIDEO, "/x/b.mp4", "second", 200L, 200L)
        AppState.addContent(a)
        AppState.addContent(b)
        assertEquals(listOf("c2", "c1"), Session.contentItems().map { it.id })
        AppState.updateContent(b.copy(caption = "edited", updatedAt = 300L))
        assertEquals("edited", Session.contentItems().first { it.id == "c2" }.caption)
        AppState.deleteContent("c1")
        assertEquals(listOf("c2"), Session.contentItems().map { it.id })
        // persists
        Session.init(context)
        assertEquals(1, Session.contentItems().size)
    }

    @Test
    fun `creator profile opens from discover with content and actions`() {
        rule.mainClock.autoAdvance = false
        rule.mainClock.advanceTimeBy(2600)
        rule.waitForIdle()
        rule.mainClock.advanceTimeBy(600)
        rule.waitForIdle()

        rule.onNodeWithText("Discover").performClick()
        rule.waitForIdle()
        rule.mainClock.advanceTimeBy(900)
        rule.waitForIdle()

        // tap the top-ranked For You creator card (Comedy user -> Zainab ranks first)
        rule.onNodeWithText("Zainab").performClick()
        rule.waitForIdle()
        rule.mainClock.advanceTimeBy(400)
        rule.waitForIdle()

        // profile sections render
        rule.onNodeWithText("Content").assertExists()
        rule.onNodeWithText("Achievements").assertExists()
        rule.onNodeWithText("Open TikTok Profile").assertExists()
        // demo content exists for every mock creator
        assertTrue(
            com.tiktokboost.app.data.MockData.contentFor(
                com.tiktokboost.app.data.MockData.users.first { it.username == "sarah.creates" }
            ).isNotEmpty()
        )
    }
}
