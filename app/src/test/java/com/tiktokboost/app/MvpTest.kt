package com.tiktokboost.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.tiktokboost.app.data.MockData
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.data.TxStatus
import com.tiktokboost.app.data.TxType
import com.tiktokboost.app.ui.AppState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.robolectric.annotation.Config

/** v0.0.11 MVP completion regressions: expiration, blocking, matching, risk. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class MvpTest {

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
    fun `stale pending exchanges expire without punishment and free the pairing`() {
        val sarah = MockData.users.first { it.username == "sarah.creates" }
        val res = AppState.completeFollow(sarah)
        assertTrue(res.ok)
        val tx = AppState.transactionFor(sarah.id)!!
        assertEquals(TxStatus.PENDING, tx.status)

        // time-travel: pending older than EXCHANGE_EXPIRATION_HOURS expires on refresh
        Session.updateTransaction(tx.copy(createdAt = System.currentTimeMillis() - 25 * 3_600_000L))
        AppState.refresh()

        val expired = AppState.transactions.first { it.id == tx.id }
        assertEquals(TxStatus.EXPIRED, expired.status)
        // no punishment: status cleared so a fresh attempt is possible
        assertEquals("expired", Session.followStatus(sarah.id))
        // no coins moved — expiry never grants or punishes
        assertEquals(0, Session.coins)
    }

    @Test
    fun `blocked creators vanish from discovery and cannot be exchanged with`() {
        val nyash = MockData.users.first { it.username == "beats.by.nyash" }
        assertTrue(AppState.rankedCreators("", "Recommended", null).any { it.id == nyash.id })
        val nowBlocked = AppState.toggleBlock(nyash.id)
        assertTrue(nowBlocked)
        assertFalse(AppState.rankedCreators("", "Recommended", null).any { it.id == nyash.id })
        assertFalse(AppState.rankedCreators("", "For You", null).any { it.id == nyash.id })
        // exchange gate refuses
        val reason = AppState.exchangeBlockReason(nyash)
        assertEquals(com.tiktokboost.app.data.FailureReason.ALREADY_EXCHANGED, reason)
        // history preserved: unblock restores discovery
        AppState.toggleBlock(nyash.id)
        assertTrue(AppState.rankedCreators("", "Recommended", null).any { it.id == nyash.id })
    }

    @Test
    fun `match score prefers same-category creators and stays in range`() {
        val sameCategory = MockData.users.first { it.category == "Comedy" && it.id != "u14" }
        val otherCategory = MockData.users.first { it.category == "Gaming" }
        val mine = AppState.matchScore(sameCategory)
        val other = AppState.matchScore(otherCategory)
        assertTrue("same-category should outscore other (got $mine vs $other)", mine > other)
        MockData.users.forEach { u ->
            val s = AppState.matchScore(u)
            assertTrue("score in 5..99 for ${u.username}: $s", s in 5..99)
        }
    }

    @Test
    fun `reports reach the admin queue and resolve`() {
        Session.addReport("zainab.skits", "Spam")
        Session.addReport("maya.gaming", "Fake profile")
        assertEquals(2, Session.reports().count { !it.resolved })
        Session.resolveReport(Session.reports().first().id)
        assertEquals(1, Session.reports().count { !it.resolved })
    }
}
