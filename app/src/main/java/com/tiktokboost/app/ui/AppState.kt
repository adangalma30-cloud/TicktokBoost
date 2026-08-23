package com.tiktokboost.app.ui

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tiktokboost.app.data.AppNotification
import com.tiktokboost.app.data.MockData
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.data.Transaction
import com.tiktokboost.app.data.Trust
import com.tiktokboost.app.data.TxStatus
import com.tiktokboost.app.data.TxType
import com.tiktokboost.app.data.User

/**
 * Compose-observable snapshot of the session. Screens read from this and it is
 * refreshed after every mutation so the UI stays in sync.
 *
 * v0.0.2 adds the exchange lifecycle: completing a follow creates a PENDING
 * transaction whose coins are held; the (demo) counterpart then confirms,
 * which VERIFIES the transaction, releases the coins, updates trust stats and
 * raises notifications — exactly the demo flow from the product spec.
 */
object AppState {

    private val mainHandler = Handler(Looper.getMainLooper())

    var coins by mutableIntStateOf(0)
        private set
    var pendingCoins by mutableIntStateOf(0)
        private set
    var followedCount by mutableIntStateOf(0)
        private set
    var returnedCount by mutableIntStateOf(0)
        private set
    var loggedIn by mutableStateOf(false)
        private set
    var displayName by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var tiktokUsername by mutableStateOf("")
        private set

    val transactions = mutableStateListOf<Transaction>()
    val notifications = mutableStateListOf<AppNotification>()
    var unreadCount by mutableIntStateOf(0)
        private set

    // personal trust
    var myExchanges by mutableIntStateOf(0)
        private set
    var myDisputes by mutableIntStateOf(0)
        private set
    var myCompletionRate by mutableIntStateOf(100)
        private set
    var myTrustLevel by mutableIntStateOf(1)
        private set
    var myTrustScore by mutableIntStateOf(20)
        private set

    // animation triggers (timestamps)
    var lastCoinReleaseAt by mutableLongStateOf(0L)
        private set
    var lastCoinReleaseAmount by mutableIntStateOf(0)
        private set
    var lastTrustUpAt by mutableLongStateOf(0L)
        private set

    fun refresh() {
        coins = Session.coins
        pendingCoins = Session.pendingCoins
        followedCount = Session.followedCount()
        returnedCount = Session.returnedCount()
        loggedIn = Session.isLoggedIn
        displayName = Session.displayName
        email = Session.email
        tiktokUsername = Session.tiktokUsername
        transactions.clear()
        transactions.addAll(Session.transactions())
        notifications.clear()
        notifications.addAll(Session.notifications())
        unreadCount = notifications.count { !it.read }
        refreshTrust()
        // catch up on demo confirmations that were due while the app was closed
        Session.pendingTransactions().forEach { tx ->
            val age = System.currentTimeMillis() - tx.createdAt
            when {
                tx.type == TxType.FOLLOW && age > MockData.DEMO_CONFIRM_DELAY + 4_000 ->
                    resolveAsVerified(tx, notify = true)
                tx.status == TxStatus.DISPUTED && age > tx.updatedAt - tx.createdAt + MockData.DEMO_DISPUTE_RESOLVE_DELAY + 8_000 ->
                    resolveDispute(tx, notify = false)
            }
        }
    }

    private fun refreshTrust() {
        myExchanges = Session.statExchanges
        myDisputes = Session.statDisputes
        myCompletionRate = if (myExchanges + myDisputes == 0) 100
        else (myExchanges * 100 / (myExchanges + myDisputes)).coerceIn(0, 100)
        myTrustLevel = Trust.levelFor(myExchanges)
        myTrustScore = Trust.scoreFor(myExchanges, myCompletionRate, myDisputes)
    }

    fun followStatus(userId: String): String? = Session.followStatus(userId)

    fun transactionFor(userId: String?): Transaction? =
        transactions.firstOrNull { it.userId == userId && it.type == TxType.FOLLOW }

    // ---- exchange lifecycle ---------------------------------------------------

    /**
     * "I've Completed" — user completed the follow on the external platform.
     * Coins are held as PENDING until the counterpart confirms.
     */
    fun completeFollow(user: User): Transaction {
        Session.setFollowStatus(user.id, "followed")
        val now = System.currentTimeMillis()
        val tx = Transaction(
            id = "tx_$now",
            userId = user.id,
            username = user.username,
            type = TxType.FOLLOW,
            status = TxStatus.PENDING,
            coins = user.coinReward,
            createdAt = now,
            updatedAt = now,
            note = "Followed @${user.externalHandle} on ${user.platform}"
        )
        Session.addTransaction(tx)
        Session.addPendingCoins(user.coinReward)
        Session.addHistory(user.username, "You followed @${user.username}", user.coinReward)
        refresh()
        scheduleDemoConfirmation(tx.id)
        return tx
    }

    /** The demo counterpart confirms a few seconds later → coins are released and trust improves. */
    private fun scheduleDemoConfirmation(txId: String) {
        mainHandler.postDelayed({
            val tx = transactions.firstOrNull { it.id == txId } ?: return@postDelayed
            if (tx.status == TxStatus.PENDING) resolveAsVerified(tx, notify = true)
        }, MockData.DEMO_CONFIRM_DELAY)
    }

    private fun resolveAsVerified(tx: Transaction, notify: Boolean) {
        val updated = tx.copy(status = TxStatus.VERIFIED, updatedAt = System.currentTimeMillis())
        Session.updateTransaction(updated)
        Session.releasePending(updated.coins)
        Session.statExchanges += 1
        val before = Trust.levelFor(Session.statExchanges - 1)
        if (notify) {
            val who = updated.username.substringBefore('.').replaceFirstChar { it.uppercase() }
            Session.addNotification(
                "exchange", "$who confirmed your completed exchange.",
                "Your follow for @${updated.username} was verified."
            )
            Session.addNotification(
                "coins", "${updated.coins} coins have been released.",
                "They moved from pending to your available balance."
            )
        }
        refresh()
        lastCoinReleaseAt = System.currentTimeMillis()
        lastCoinReleaseAmount = updated.coins
        val after = Trust.levelFor(Session.statExchanges)
        if (after > before) {
            lastTrustUpAt = System.currentTimeMillis()
            Session.addNotification(
                "trust", "Your trust score increased.",
                "You're now Level $after — ${Trust.tier(after).name}. Keep completing exchanges to level up."
            )
            refresh()
        }
    }

    /** User reports an issue with a pending exchange → under review, then resolves. */
    fun dispute(tx: Transaction) {
        if (tx.status != TxStatus.PENDING) return
        val updated = tx.copy(status = TxStatus.DISPUTED, updatedAt = System.currentTimeMillis())
        Session.updateTransaction(updated)
        Session.statDisputes += 1
        Session.addNotification(
            "exchange", "Exchange under review.",
            "We're reviewing your exchange with @${tx.username}. Coins stay pending until it's resolved."
        )
        refresh()
        mainHandler.postDelayed({
            val current = transactions.firstOrNull { it.id == tx.id } ?: return@postDelayed
            if (current.status == TxStatus.DISPUTED) resolveDispute(current, notify = true)
        }, MockData.DEMO_DISPUTE_RESOLVE_DELAY)
    }

    private fun resolveDispute(tx: Transaction, notify: Boolean) {
        val updated = tx.copy(status = TxStatus.COMPLETED, updatedAt = System.currentTimeMillis())
        Session.updateTransaction(updated)
        Session.releasePending(tx.coins)
        if (notify) {
            Session.addNotification(
                "coins", "Review complete — coins released.",
                "The exchange with @${tx.username} was resolved in your favour."
            )
        }
        refresh()
        lastCoinReleaseAt = System.currentTimeMillis()
        lastCoinReleaseAmount = tx.coins
    }

    /** Counterpart followed the user back → instant reward. */
    fun confirmFollowBack(user: User): Int {
        Session.setFollowStatus(user.id, "followed_back")
        Session.addCoins(MockData.REWARD_FOLLOW_BACK)
        val now = System.currentTimeMillis()
        Session.addTransaction(
            Transaction(
                id = "tx_$now",
                userId = user.id,
                username = user.username,
                type = TxType.FOLLOW_BACK,
                status = TxStatus.COMPLETED,
                coins = MockData.REWARD_FOLLOW_BACK,
                createdAt = now,
                updatedAt = now,
                note = "@${user.username} followed you back"
            )
        )
        Session.addHistory(user.username, "@${user.username} followed you back", MockData.REWARD_FOLLOW_BACK)
        refresh()
        return MockData.REWARD_FOLLOW_BACK
    }

    // ---- smaller rewards ------------------------------------------------------------

    fun checkIn(): Int {
        if (!Session.canCheckIn()) return 0
        Session.doCheckIn()
        Session.addCoins(2)
        Session.addHistory("checkin", "Daily check-in bonus", 2)
        val now = System.currentTimeMillis()
        Session.addTransaction(
            Transaction("tx_$now", null, "Daily check-in", TxType.BONUS, TxStatus.COMPLETED, 2, now, now, "Daily check-in bonus")
        )
        refresh()
        return 2
    }

    fun canCheckIn(): Boolean = Session.canCheckIn()

    fun spendCoins(n: Int, title: String): Boolean {
        val ok = Session.spendCoins(n)
        if (ok) {
            val now = System.currentTimeMillis()
            Session.addTransaction(
                Transaction("tx_$now", null, title, TxType.PURCHASE, TxStatus.COMPLETED, -n, now, now, title)
            )
        }
        refresh()
        return ok
    }

    fun claimTask(id: String, reward: Int, title: String = "Task reward"): Boolean {
        if (!Session.claimTask(id)) return false
        Session.addCoins(reward)
        val now = System.currentTimeMillis()
        Session.addTransaction(
            Transaction("tx_$now", null, title, TxType.BONUS, TxStatus.COMPLETED, reward, now, now, "Earned: $title")
        )
        refresh()
        return true
    }

    fun isTaskClaimed(id: String): Boolean = Session.isTaskClaimed(id)

    fun welcomeBonus(handle: String) {
        Session.addCoins(MockData.WELCOME_BONUS)
        Session.addHistory(handle, "Welcome bonus for @$handle", MockData.WELCOME_BONUS)
        val now = System.currentTimeMillis()
        Session.addTransaction(
            Transaction("tx_$now", null, "Welcome bonus", TxType.BONUS, TxStatus.COMPLETED, MockData.WELCOME_BONUS, now, now, "Welcome to TikTokBoost")
        )
    }

    // ---- notifications ----------------------------------------------------------------

    fun markAllRead() {
        Session.markAllNotificationsRead()
        refresh()
    }
}
