package com.tiktokboost.app

import android.content.Context
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTextReplacement
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

/**
 * v1.0.1 Profile UX polish regressions:
 * read-only profile, dedicated edit screen with explicit save, ⋯ content menus,
 * delete confirmation.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34], qualifiers = "w360dp-h640dp")
class ProfileUxTest {

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
        Session.bio = "Original bio"
        Session.category = "Comedy"
        AppState.refresh()
    }

    private fun goProfile() {
        rule.mainClock.autoAdvance = false
        rule.mainClock.advanceTimeBy(2600)
        rule.waitForIdle()
        rule.mainClock.advanceTimeBy(600)
        rule.waitForIdle()
        rule.mainClock.autoAdvance = true
        rule.waitForIdle()
        rule.onNodeWithText("Profile").performClick()
        rule.waitForIdle()
    }

    @Test
    fun `profile is clean and read-only - no permanent edit form`() {
        goProfile()
        // profile info displayed
        rule.onNodeWithText("John Doe").assertExists()
        rule.onNodeWithText("@john.grams").assertExists()
        rule.onNodeWithText("Original bio").assertExists()
        // clear Edit Profile entry point
        rule.onNodeWithText("Edit Profile").performScrollTo().assertExists()
        // NO permanent form: no text fields / save button on the profile itself
        rule.onNodeWithText("Display name").assertDoesNotExist()
        rule.onNodeWithText("Save Changes").assertDoesNotExist()
    }

    @Test
    fun `edit saves only on Save Changes and returns to updated profile`() {
        goProfile()
        rule.onNodeWithText("Edit Profile").performScrollTo().performClick()
        rule.waitForIdle()

        // edit screen: form fields + explicit save
        rule.onNodeWithText("Display name").assertExists()
        rule.onNodeWithText("Save Changes").assertExists()

        // change the name and save
        rule.onNodeWithText("John Doe").performTextReplacement("Jane Grower")
        // the button sits below the fold on small screens — scroll it into view first
        rule.onNodeWithText("Save Changes").performScrollTo().performClick()
        rule.waitForIdle()
        rule.mainClock.advanceTimeBy(1000)   // confirmation delay then auto-back
        rule.waitForIdle()

        // back on the read-only profile with the NEW name; persisted
        rule.onNodeWithText("Jane Grower").assertExists()
        assertEquals("Jane Grower", Session.displayName)
    }

    @Test
    fun `cancel discards unsaved changes`() {
        goProfile()
        rule.onNodeWithText("Edit Profile").performScrollTo().performClick()
        rule.waitForIdle()
        rule.onNodeWithText("John Doe").performTextReplacement("Temp Name")
        // cancel via the top bar back button
        rule.onNodeWithContentDescription("Back").performClick()
        rule.waitForIdle()
        // original name still shown and persisted — nothing was saved
        rule.onNodeWithText("John Doe").assertExists()
        assertEquals("John Doe", Session.displayName)
    }

    @Test
    fun `content cards use dot menu and deletion requires confirmation`() {
        Session.addContentItem(
            ContentItem("c_test", MediaType.PHOTO, null, "sunset vibes", 100L, 100L)
        )
        AppState.refresh()
        goProfile()

        // card renders with caption; no permanently visible Edit/Delete actions
        rule.onNodeWithText("sunset vibes").performScrollTo().assertExists()
        rule.onNodeWithText("Delete").assertDoesNotExist()

        // open the ⋯ menu → Delete → confirmation appears
        rule.onNodeWithContentDescription("More options").performScrollTo().performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Delete").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Delete this content?").assertExists()

        // cancel first — content survives
        rule.onNodeWithText("Cancel").performClick()
        rule.waitForIdle()
        assertTrue(Session.contentItems().any { it.id == "c_test" })

        // delete for real
        rule.onNodeWithContentDescription("More options").performScrollTo().performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Delete").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Delete").performClick()  // confirm button (exact — title also contains 'Delete')
        rule.waitForIdle()
        assertTrue("deleted after confirmation", Session.contentItems().none { it.id == "c_test" })
    }
}
