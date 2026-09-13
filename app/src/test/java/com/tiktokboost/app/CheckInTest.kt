package com.tiktokboost.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.tiktokboost.app.data.EconomyResult
import com.tiktokboost.app.data.FailureReason
import com.tiktokboost.app.data.QuestStatus
import com.tiktokboost.app.data.Quests
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.ui.AppState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.robolectric.annotation.Config
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * version-1.0.1 — Daily Check-in regressions.
 * Root cause fixed: a fresh day computed quest progress 0/1 -> AVAILABLE, so
 * claiming returned INVALID_STATE ("Not available right now"). Eligibility
 * (canCheckIn) is now the READY_TO_CLAIM condition.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class CheckInTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val def get() = Quests.byId("q_checkin")!!

    @Before
    fun seed() {
        context.getSharedPreferences("tiktokboost_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        Session.init(context)
        Session.isLoggedIn = true
        Session.isOnboarded = true
        Session.displayName = "John Doe"
        Session.tiktokUsername = "john.grams"
        AppState.refresh()
    }

    @Test
    fun `eligible user can claim - reward, streak, daily progress, persistence, UI state`() {
        // THE FIX: an eligible user is READY_TO_CLAIM (was AVAILABLE -> broken)
        assertEquals(QuestStatus.READY_TO_CLAIM, Quests.stateOf(def).status)

        val before = Session.coins
        val res = Quests.claim(def)
        assertTrue("claim must succeed: $res", res is EconomyResult.Success)

        // +1 coin through the central economy
        assertEquals(before + 1, Session.coins)
        // streak started
        assertEquals(1, Session.streakDays)
        // today's earning progress updated
        assertEquals(1, AppState.dailyEarnedCoins)
        // UI state flips to completed
        assertEquals(QuestStatus.COMPLETED, Quests.stateOf(def).status)
        // transaction recorded
        assertTrue(Session.transactions().any { it.note.contains("Daily check-in") })

        // persists across restart
        Session.init(context)
        AppState.refresh()
        assertEquals(1, Session.streakDays)
        assertEquals(QuestStatus.COMPLETED, Quests.stateOf(def).status)
    }

    @Test
    fun `same-day duplicate claim is prevented`() {
        Quests.claim(def)
        val coins = Session.coins
        val second = Quests.claim(def)
        assertTrue(second is EconomyResult.Failure)
        assertEquals(FailureReason.DUPLICATE, (second as EconomyResult.Failure).reason)
        assertEquals(coins, Session.coins)
        assertEquals(QuestStatus.COMPLETED, Quests.stateOf(def).status)
    }

    @Test
    fun `next day the check-in is claimable again and the streak continues`() {
        Quests.claim(def)   // day 1: streak = 1
        rewindCheckIn(daysAgo = 1, streakDays = 1)

        assertEquals(QuestStatus.READY_TO_CLAIM, Quests.stateOf(def).status)
        val res = Quests.claim(def)
        assertTrue(res is EconomyResult.Success)
        assertEquals("consecutive day must continue the streak", 2, Session.streakDays)
    }

    @Test
    fun `missed day resets the streak per existing rules`() {
        Quests.claim(def)   // day 1
        rewindCheckIn(daysAgo = 3, streakDays = 2)  // last check-in 3 days ago, streak was 2

        val res = Quests.claim(def)
        assertTrue(res is EconomyResult.Success)
        assertEquals("a missed day must reset the streak to 1", 1, Session.streakDays)
    }

    /** Simulates time passing: last check-in N days ago with the given streak. */
    private fun rewindCheckIn(daysAgo: Int, streakDays: Int) {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val past = fmt.format(Date(System.currentTimeMillis() - daysAgo * 86_400_000L))
        context.getSharedPreferences("tiktokboost_prefs", Context.MODE_PRIVATE).edit()
            .putString("last_checkin", past)
            .apply()
        Session.streakDays = streakDays
        Session.streakLastDay = past
        // quest record also ages so the state machine sees a new day
        val old = System.currentTimeMillis() - daysAgo * 86_400_000L
        Session.saveQuestStatesJson(
            """{"q_checkin":{"progress":1,"status":"COMPLETED","completed":$old,"claimed":$old}}"""
        )
        AppState.refresh()
    }
}
