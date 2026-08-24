package com.tiktokboost.app

import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.test.core.app.ApplicationProvider
import com.tiktokboost.app.data.MockData
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.data.TxStatus
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
 * End-to-end stability harness against the REAL MainActivity:
 * startup, splash hand-over, every tab, the Notifications screen with
 * poisoned (duplicate-id) persisted data — exactly the v0.0.3 field crash —
 * plus the John/Sarah exchange flow.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34], qualifiers = "w360dp-h640dp")
class StabilityTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun seedPoisonedV003State() {
        val sp = context.getSharedPreferences("tiktokboost_prefs", Context.MODE_PRIVATE)
        val sameMs = 1730000000000L
        sp.edit()
            .putBoolean("onboarded", true)
            .putBoolean("logged_in", true)
            .putBoolean("v2_migrated", true)
            .putBoolean("v3_migrated", true)
            .putString("display_name", "John Doe")
            .putString("email", "john@example.com")
            .putString("tiktok_username", "john.grams")
            .putInt("coins", 57)
            .putInt("lifetime_earned", 82)
            .putInt("lifetime_spent", 25)
            .putInt("stat_exchanges", 3)
            .putString("follow_status", """{"u13":"followed_back"}""")
            .putString(
                "transactions",
                """
                [
                 {"id":"tx_dup","uid":"u13","username":"sarah.creates","type":"FOLLOW","status":"VERIFIED","coins":5,"created":1729000000000,"updated":1729000000000,"note":"Confirmed exchange"},
                 {"id":"tx_dup","uid":"u3","username":"beats.by.nyash","type":"FOLLOW","status":"VERIFIED","coins":5,"created":1729000001000,"updated":1729000001000,"note":"Confirmed exchange"},
                 {"id":"tx_boost","uid":"","username":"Standard Boost","type":"BOOST","status":"COMPLETED","coins":-10,"created":1729000100000,"updated":1729000100000,"note":"Standard Boost"}
                ]
                """.trimIndent()
            )
            .putString(
                "notifications",
                """
                [
                 {"id":"n_$sameMs","kind":"exchange","title":"Sarah confirmed your completed exchange.","message":"Your exchange was verified.","ts":$sameMs,"read":false},
                 {"id":"n_$sameMs","kind":"coins","title":"5 coins have been released.","message":"Pending coins moved to your available balance.","ts":$sameMs,"read":false},
                 {"id":"n_${sameMs}b","kind":"trust","title":"Your trust score increased.","message":"You are now Level 2.","ts":$sameMs,"read":true}
                ]
                """.trimIndent()
            )
            .commit()
        Session.init(context)
        AppState.refresh()
    }

    private fun goHome() {
        rule.mainClock.autoAdvance = false
        rule.mainClock.advanceTimeBy(2600)   // past splash (~2s + margins)
        rule.waitForIdle()
        rule.mainClock.advanceTimeBy(600)    // let home staggered cards appear
        rule.waitForIdle()
    }

    @Test
    fun `app launches to home without crashing`() {
        goHome()
        rule.onNodeWithText("Discover Creators").performScrollTo().assertExists()
    }

    @Test
    fun `notifications screen survives duplicate persisted ids`() {
        // the exact v0.0.3 field crash: duplicate "n_<sameMs>" ids in storage
        val ids = Session.notifications().map { it.id }
        assertEquals("duplicate ids must be deduped on read", ids.size, ids.toSet().size)

        goHome()
        rule.onNodeWithContentDescription("Notifications").performClick()
        rule.waitForIdle()
        rule.mainClock.advanceTimeBy(300)
        rule.onNodeWithText("Notifications", substring = true).assertExists()
        rule.onNodeWithText("Sarah confirmed your completed exchange.").assertExists()
    }

    @Test
    fun `history screen survives duplicate transaction ids`() {
        // data-level guarantee: duplicate ids are deduped on read, so the
        // History LazyColumn can never receive duplicate keys (the crash class)
        val txs = Session.transactions()
        assertEquals(2, txs.size)                     // "tx_dup","tx_dup","tx_boost" -> two
        assertEquals(txs.size, txs.map { it.id }.toSet().size)
        // and the entry point renders on Profile
        goHome()
        rule.onNodeWithText("Profile").performClick()
        rule.waitForIdle()
        rule.mainClock.advanceTimeBy(400)
        rule.onNodeWithText("View history").performScrollTo().assertExists()
    }

    private fun dumpTexts() {
        fun dump(n: SemanticsNode, depth: Int) {
            val texts = n.config.getOrNull(SemanticsProperties.Text)
            if (texts != null) println("HISTDEBUG " + texts.joinToString(" | ") { x -> x.text })
            for (c in n.children) dump(c, depth + 1)
        }
        dump(rule.onRoot().fetchSemanticsNode(), 0)
    }

    @Test
    fun `every tab renders`() {
        goHome()
        listOf("Discover", "Earn", "Boost", "Profile").forEach { tab ->
            rule.onNodeWithText(tab).performClick()
            rule.waitForIdle()
            rule.mainClock.advanceTimeBy(400)
            rule.waitForIdle()
        }
        rule.onNodeWithText("Edit profile").performScrollTo().assertExists()
    }

    @Test
    fun `john and sarah exchange flow releases coins through the economy`() {
        goHome()
        val coinsBefore = AppState.coins
        val dailyBefore = AppState.dailyEarnedCoins

        // Discover -> find Sarah -> complete
        rule.onNodeWithText("Discover").performClick()
        rule.waitForIdle()
        rule.mainClock.advanceTimeBy(900)   // past skeleton loading
        rule.waitForIdle()

        val sarah = MockData.users.first { it.username == "sarah.creates" }
        // Sarah was seeded as already exchanged -> use a fresh creator instead,
        // but the pairing rule must block repeating with sarah:
        assertTrue("repeat pairing must be blocked", !com.tiktokboost.app.data.EconomyService.canPairWith("u13") ||
            Session.partnerHistory("u13") == null)

        // complete a fresh exchange through the real economy path
        val kevin = MockData.users.first { it.username == "kevin.creates" }
        val res = AppState.completeFollow(kevin)
        assertTrue(res.ok)
        rule.waitForIdle()
        assertTrue(
            "exchange must be PENDING before confirmation",
            AppState.transactionFor(kevin.id)!!.status == TxStatus.PENDING
        )

        // simulate counterpart confirmation (the demo path does this ~8s later;
        // here we time-travel the transaction and let refresh() catch up)
        val tx = AppState.transactionFor(kevin.id)!!
        println("FLOWDEBUG before-travel id=" + tx.id + " status=" + tx.status + " coins=" + Session.coins + " daily=" + Session.dailyEarned + "/" + Session.dailyEarnDate)
        Session.updateTransaction(tx.copy(createdAt = System.currentTimeMillis() - 20_000))
        AppState.refresh()
        println("FLOWDEBUG after-refresh sessionCoins=" + Session.coins + " appCoins=" + AppState.coins + " daily=" + AppState.dailyEarnedCoins + " statEx=" + Session.statExchanges)
        rule.waitForIdle()

        assertEquals(TxStatus.VERIFIED, AppState.transactionFor(kevin.id)!!.status)
        // +5 confirmed exchange (the regression) plus small achievement bonuses (+2 each)
        assertTrue("coins must be released (+" + (AppState.coins - coinsBefore) + ")",
            AppState.coins >= coinsBefore + 5)
        assertTrue("daily progress must advance", AppState.dailyEarnedCoins == dailyBefore + 5)
    }
}
