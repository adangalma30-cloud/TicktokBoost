package com.tiktokboost.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.tiktokboost.app.data.EconomyResult
import com.tiktokboost.app.data.FailureReason
import com.tiktokboost.app.data.Quests
import com.tiktokboost.app.data.QuestStatus
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.ui.AppState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.robolectric.annotation.Config

/**
 * Quest reward security: rewards require REAL qualifying actions.
 * Opening, tapping, restarting must never grant coins.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class QuestTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun seedFreshUser() {
        context.getSharedPreferences("tiktokboost_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        Session.init(context)
        Session.isLoggedIn = true
        Session.isOnboarded = true
        Session.displayName = "John Doe"
        Session.tiktokUsername = "john.grams"
        Session.category = "Comedy"
        Session.bio = "Skits & fun"
        AppState.refresh()
    }

    @Test
    fun `claiming an unearned quest is rejected`() {
        val shareQuest = Quests.byId("q_share")!!
        // nothing qualified yet
        assertEquals(QuestStatus.AVAILABLE, Quests.stateOf(shareQuest).status)
        val res = Quests.claim(shareQuest)
        assertTrue(res is EconomyResult.Failure)
        assertEquals(FailureReason.INVALID_STATE, (res as EconomyResult.Failure).reason)
        // no coins, no transactions
        assertEquals(0, Session.coins)
        assertEquals(0, Session.transactions().count { it.type == com.tiktokboost.app.data.TxType.ACHIEVEMENT })
    }

    @Test
    fun `share quest qualifies only after the share sheet event and pays exactly once`() {
        val shareQuest = Quests.byId("q_share")!!
        Quests.onAppShared()                       // real qualifying event
        assertEquals(QuestStatus.READY_TO_CLAIM, Quests.stateOf(shareQuest).status)

        val first = Quests.claim(shareQuest)
        assertTrue(first is EconomyResult.Success)
        assertEquals(3, Session.coins)

        val second = Quests.claim(shareQuest)
        assertTrue(second is EconomyResult.Failure)   // never twice
        assertEquals(3, Session.coins)                 // balance unchanged
        assertEquals(QuestStatus.COMPLETED, Quests.stateOf(shareQuest).status)
    }

    @Test
    fun `invite is a permanent system not a one-time quest`() {
        // the one-time invite quest is gone; referrals are handled by the
        // persistent referral system (see V13Test)
        assertEquals(null, Quests.byId("q_invite"))
    }

    @Test
    fun `profile quest tracks completion and follow quest tracks real exchanges`() {
        val profileQuest = Quests.byId("q_profile")!!
        // bio + category + name + handle => 100% => ready
        assertEquals(QuestStatus.READY_TO_CLAIM, Quests.stateOf(profileQuest).status)

        val followQuest = Quests.byId("q_follow3")!!
        assertEquals(QuestStatus.AVAILABLE, Quests.stateOf(followQuest).status)
        // simulate one qualifying completed exchange
        Session.setFollowStatus("u13", "followed")
        assertEquals(1, Quests.stateOf(followQuest).progress)
        assertEquals(QuestStatus.IN_PROGRESS, Quests.stateOf(followQuest).status)
        assertTrue(Quests.claim(followQuest) is EconomyResult.Failure)   // 1/3 not enough
    }
}
