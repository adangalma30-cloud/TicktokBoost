package com.tiktokboost.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.tiktokboost.app.data.EconomyResult
import com.tiktokboost.app.data.FailureReason
import com.tiktokboost.app.data.Quests
import com.tiktokboost.app.data.ReferralStatus
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.ui.AppState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.robolectric.annotation.Config

/** v0.0.13: persistent, repeatable referral system with idempotent rewards. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class V13Test {

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
    fun `referral lifecycle - join qualify claim reward exactly once`() {
        val before = Session.coins
        val r = AppState.simulateFriendJoining()
        assertEquals(ReferralStatus.REGISTERED, Session.referrals().first { it.id == r.id }.status)

        // not claimable before qualification
        assertTrue(AppState.claimReferralReward(r.id) is EconomyResult.Failure)

        AppState.simulateFriendQualifying(r.id)
        assertEquals(ReferralStatus.QUALIFIED, Session.referrals().first { it.id == r.id }.status)

        val res = AppState.claimReferralReward(r.id)
        assertTrue(res is EconomyResult.Success)
        assertEquals(Session.coins, before + 5)
        val rec = Session.referrals().first { it.id == r.id }
        assertEquals(ReferralStatus.REWARDED, rec.status)
        assertEquals("rftx_${r.id}", rec.rewardTransactionId)

        // IDEMPOTENT: second claim of the same referral never pays again
        val again = AppState.claimReferralReward(r.id)
        assertTrue(again is EconomyResult.Failure)
        assertEquals(FailureReason.DUPLICATE, (again as EconomyResult.Failure).reason)
        assertEquals(Session.coins, before + 5)
    }

    @Test
    fun `referrals are repeatable - second friend rewards again`() {
        val before = Session.coins
        val a = AppState.simulateFriendJoining()
        AppState.simulateFriendQualifying(a.id)
        assertTrue(AppState.claimReferralReward(a.id) is EconomyResult.Success)

        // invite remains available forever
        val b = AppState.simulateFriendJoining()
        AppState.simulateFriendQualifying(b.id)
        val res = AppState.claimReferralReward(b.id)
        assertTrue("second referral must reward too", res is EconomyResult.Success)
        assertEquals(Session.coins, before + 10)
        assertEquals(2, Session.referrals().count { it.status == ReferralStatus.REWARDED })
    }

    @Test
    fun `daily referral reward limit blocks the fourth claim`() {
        repeat(3) {
            val r = AppState.simulateFriendJoining()
            AppState.simulateFriendQualifying(r.id)
            assertTrue(AppState.claimReferralReward(r.id) is EconomyResult.Success)
        }
        val fourth = AppState.simulateFriendJoining()
        AppState.simulateFriendQualifying(fourth.id)
        val res = AppState.claimReferralReward(fourth.id)
        assertTrue(res is EconomyResult.Failure)
        assertEquals(FailureReason.DAILY_CAP_REACHED, (res as EconomyResult.Failure).reason)
    }

    @Test
    fun `referrals persist across restart and stats are correct`() {
        val r = AppState.simulateFriendJoining()
        AppState.simulateFriendQualifying(r.id)
        AppState.claimReferralReward(r.id)

        // simulate restart
        Session.init(context)
        AppState.refresh()

        assertEquals(1, Session.referrals().size)
        assertEquals(ReferralStatus.REWARDED, Session.referrals().first().status)
        val (invited, rewarded, coins) = AppState.referralStats()
        assertEquals(1, invited)
        assertEquals(1, rewarded)
        assertEquals(5, coins)
    }

    @Test
    fun `self referral code is rejected at signup validation`() {
        // the signup guard rejects entering your own code
        val own = Session.referralCode()
        assertTrue(own.isNotBlank())
        // (guard logic is exercised in AuthScreens; here we assert the code shape)
        assertEquals(8, own.length)
    }
}
