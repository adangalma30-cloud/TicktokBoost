package com.tiktokboost.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.tiktokboost.app.data.BoostTaskStatus
import com.tiktokboost.app.data.EconomyResult
import com.tiktokboost.app.data.FailureReason
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.ui.AppState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.robolectric.annotation.Config

/** v1.0.1: boost task lifecycle, staking, expiry, leaderboard. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class V101Test {

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
        AppState.refresh()   // seeds the community board on first read
    }

    @Test
    fun `task lifecycle - start complete reward exactly once persist`() {
        val task = AppState.boostTasks.first { it.id == "bt_engage" }
        assertEquals(BoostTaskStatus.AVAILABLE, task.status)
        // reward visible before starting
        assertTrue(task.reward >= 2)

        assertTrue(AppState.startTask(task.id) is EconomyResult.Success)
        assertEquals(BoostTaskStatus.IN_PROGRESS, AppState.taskById(task.id)!!.status)

        val before = Session.coins
        val res = AppState.completeTask(task.id)
        assertTrue(res is EconomyResult.Success)
        assertEquals(before + task.reward, Session.coins)
        assertEquals(BoostTaskStatus.COMPLETED, AppState.taskById(task.id)!!.status)
        assertEquals("bttx_${task.id}", AppState.taskById(task.id)!!.rewardTransactionId)

        // double completion rejected, balance unchanged
        val dup = AppState.completeTask(task.id)
        assertTrue(dup is EconomyResult.Failure)
        assertEquals(FailureReason.DUPLICATE, (dup as EconomyResult.Failure).reason)
        assertEquals(before + task.reward, Session.coins)

        // restart: state persists
        Session.init(context)
        AppState.refresh()
        assertEquals(BoostTaskStatus.COMPLETED, AppState.taskById(task.id)!!.status)
    }

    @Test
    fun `starting a non-available task is rejected`() {
        AppState.startTask("bt_engage")
        val again = AppState.startTask("bt_engage")
        assertTrue(again is EconomyResult.Failure)
        assertEquals(FailureReason.INVALID_STATE, (again as EconomyResult.Failure).reason)
    }

    @Test
    fun `expired in-progress tasks sweep to EXPIRED without reward`() {
        AppState.startTask("bt_engage")
        // time-travel the deadline into the past
        val t = AppState.taskById("bt_engage")!!
        Session.updateBoostTask(t.copy(expiresAt = System.currentTimeMillis() - 1000))
        val before = Session.coins
        AppState.refresh()   // sweep runs
        assertEquals(BoostTaskStatus.EXPIRED, AppState.taskById("bt_engage")!!.status)
        // completing an expired task is refused
        val res = AppState.completeTask("bt_engage")
        assertTrue(res is EconomyResult.Failure)
        assertEquals(before, Session.coins)
    }

    @Test
    fun `creating a task stakes the reward up front and publishes it`() {
        Session.addCoins(50)   // funding
        val before = Session.coins
        val res = AppState.createTask("Check out my new skit", "Watch and drop a comment", 5)
        assertTrue(res is EconomyResult.Success)
        assertEquals("stake must be deducted immediately", before - 5, Session.coins)
        val mine = AppState.boostTasks.first { it.createdByMe }
        assertEquals("Check out my new skit", mine.title)
        assertEquals(BoostTaskStatus.AVAILABLE, mine.status)
        // invalid rewards rejected
        assertTrue(AppState.createTask("Bad", "", 99) is EconomyResult.Failure)
    }

    @Test
    fun `leaderboard ranks honestly and includes the user`() {
        val rows = AppState.leaderboard()
        assertTrue("community + user", rows.size > 10)
        assertTrue("exactly one row is the user", rows.count { it.third } == 1)
        // descending order
        assertEquals(rows.map { it.second }.sortedDescending(), rows.map { it.second })
        // user's points are their REAL lifetime earned
        assertEquals(Session.lifetimeEarned, rows.first { it.third }.second)
        assertTrue("rank is 1-based", AppState.myRank() in 1..rows.size)
    }
}
