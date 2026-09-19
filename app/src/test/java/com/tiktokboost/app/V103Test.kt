package com.tiktokboost.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.tiktokboost.app.data.auth.AuthService
import com.tiktokboost.app.data.db.AppDatabase
import com.tiktokboost.app.data.db.DatabaseMirror
import com.tiktokboost.app.data.db.PointTransactionEntity
import com.tiktokboost.app.data.ContentItem
import com.tiktokboost.app.data.MediaType
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.data.TxType
import com.tiktokboost.app.ui.AppState
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.robolectric.annotation.Config

/**
 * v1.0.3 — backend & database foundation:
 * auth (hashed credentials, wrong-password rejection), ledger write-through,
 * content ownership, check-in rows, profile persistence in Room.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class V103Test {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var db: AppDatabase

    @Before
    fun seed() {
        context.getSharedPreferences("tiktokboost_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        // in-memory Room injected BEFORE Session.init wires DatabaseMirror
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        AppDatabase.setTestInstance(db)
        Session.init(context)
        Session.isLoggedIn = true
        Session.isOnboarded = true
        Session.displayName = "John Doe"
        Session.tiktokUsername = "john.grams"
        Session.bio = "Skits"
        Session.category = "Comedy"
        AppState.refresh()
        DatabaseMirror.awaitWrites()
    }

    @After
    fun tearDown() {
        DatabaseMirror.awaitWrites()
        db.close()
        AppDatabase.setTestInstance(null)
        DatabaseMirror.resetForTest()
    }

    @Test
    fun `register stores hashed credentials - never plaintext - and login verifies`() {
        val reg = AuthService.register(Session.userDbId, "john@example.com", "hunter22")
        assertTrue(reg.ok)
        DatabaseMirror.awaitWrites()

        val user = db.userDao().byEmail("john@example.com")
        assertNotNull("user row exists", user)
        assertTrue("hash stored", user!!.passwordHash!!.isNotEmpty())
        assertNotEquals("password NOT stored in plaintext", "hunter22", user.passwordHash)
        assertEquals("salt stored separately", 32, user.passwordSalt!!.length)

        // correct password verifies
        val ok = AuthService.login(Session.userDbId, "john@example.com", "hunter22")
        assertTrue(ok.ok)
        // wrong password is rejected
        val bad = AuthService.login(Session.userDbId, "john@example.com", "wrongpass")
        assertTrue("wrong password must fail", !bad.ok)
    }

    @Test
    fun `every economy transaction lands in the database ledger`() {
        val before = db.pointTransactionDao().byUser(Session.userDbId).size
        AppState.checkIn()   // +1 through the central economy service
        DatabaseMirror.awaitWrites()

        val rows = db.pointTransactionDao().byUser(Session.userDbId)
        assertTrue("ledger grew", rows.size > before)
        val checkinRow = rows.firstOrNull { it.reason.contains("Daily check-in") }
        assertNotNull("check-in reward recorded", checkinRow)
        assertEquals(1, checkinRow!!.amount)
        assertEquals(TxType.BONUS.name, checkinRow.type)
        assertNotNull("reference id present for idempotency", checkinRow.referenceId)
    }

    @Test
    fun `derived balance equals the sum of ledger rows`() {
        val earned = db.pointTransactionDao().byUser(Session.userDbId).sumOf { it.amount }
        val derived = db.pointTransactionDao().balance(Session.userDbId)
        assertEquals("balances are derived from the ledger, never stored raw", earned, derived)
    }

    @Test
    fun `content is owned and delete is ownership-scoped in SQL`() {
        AppState.addContent(ContentItem("c_own1", MediaType.PHOTO, "/x/p.jpg", "mine", 10L, 10L))
        DatabaseMirror.awaitWrites()
        assertEquals(1, db.contentDao().byUser(Session.userDbId).size)

        // another user's delete attempt (different userId) removes nothing
        val removed = db.contentDao().deleteOwned("c_own1", "someone_else")
        assertEquals("foreign delete must affect 0 rows", 0, removed)
        assertEquals(1, db.contentDao().byUser(Session.userDbId).size)

        // the owner can delete
        AppState.deleteContent("c_own1")
        DatabaseMirror.awaitWrites()
        assertEquals(0, db.contentDao().byUser(Session.userDbId).size)
    }

    @Test
    fun `check-in state persists to the daily_checkins table`() {
        AppState.checkIn()
        DatabaseMirror.awaitWrites()
        val row = db.dailyCheckInDao().byUser(Session.userDbId)
        assertNotNull("check-in row exists", row)
        assertEquals(1, row!!.currentStreak)
        assertTrue(row.lastCheckIn > 0)
    }

    @Test
    fun `profile edits persist through the repository to Room`() {
        DatabaseMirror.profile("Jane Grower", "jane.g", "New bio", null, "Music")
        DatabaseMirror.awaitWrites()
        val row = db.profileDao().byUser(Session.userDbId)
        assertNotNull(row)
        assertEquals("Jane Grower", row!!.displayName)
        assertEquals("Music", row.category)
        assertEquals("jane.g", row.tiktokUsername)
    }

    @Test
    fun `notifications are mirrored to the database`() {
        val before = db.notificationDao().byUser(Session.userDbId).size
        Session.addNotification("system", "Mirror test", "Mirrored to DB")
        DatabaseMirror.awaitWrites()
        assertEquals(before + 1, db.notificationDao().byUser(Session.userDbId).size)
    }
}
