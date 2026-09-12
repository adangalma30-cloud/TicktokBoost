package com.tiktokboost.app.ui

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tiktokboost.app.data.Achievements
import com.tiktokboost.app.data.AbuseStatus
import com.tiktokboost.app.data.BoostTier
import com.tiktokboost.app.data.DisputeRecord
import com.tiktokboost.app.data.DisputeStatus
import com.tiktokboost.app.data.EconomyConfig
import com.tiktokboost.app.data.EconomyResult
import com.tiktokboost.app.data.EconomyService
import com.tiktokboost.app.data.FailureReason
import com.tiktokboost.app.data.MockData
import com.tiktokboost.app.data.PremiumPlan
import com.tiktokboost.app.data.PremiumTier
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.data.Transaction
import com.tiktokboost.app.data.Trust
import com.tiktokboost.app.data.TxStatus
import com.tiktokboost.app.data.TxType
import com.tiktokboost.app.data.User
import com.tiktokboost.app.data.AppNotification

/**
 * Observable app state. Every coin movement goes through EconomyService —
 * screens never compute balances. Trust, streaks, achievements, premium and
 * the exchange lifecycle (pending → confirmed → released) are orchestrated here.
 */
object AppState {

    private val mainHandler = Handler(Looper.getMainLooper())

    // ── appearance (v0.0.12): reactive, persisted, applies app-wide ──
    var themeMode by mutableStateOf("system")
        private set
    var notificationsEnabled by mutableStateOf(true)
        private set
    var hapticsEnabled by mutableStateOf(true)
        private set
    var language by mutableStateOf("system")
        private set
    var discoverable by mutableStateOf(true)
        private set

    fun setTheme(mode: String) { Session.themeMode = mode; themeMode = mode }
    fun setNotifications(enabled: Boolean) { Session.notificationsEnabled = enabled; notificationsEnabled = enabled }
    fun setHaptics(enabled: Boolean) { Session.hapticsEnabled = enabled; hapticsEnabled = enabled }
    fun updateLanguage(code: String) { Session.language = code; language = code }
    fun updateDiscoverable(enabled: Boolean) { Session.discoverable = enabled; discoverable = enabled }

    // own creator content (observable)
    val contentItems = mutableStateListOf<com.tiktokboost.app.data.ContentItem>()

    fun addContent(item: com.tiktokboost.app.data.ContentItem) {
        Session.addContentItem(item)
        contentItems.clear(); contentItems.addAll(Session.contentItems())
    }

    fun updateContent(item: com.tiktokboost.app.data.ContentItem) {
        Session.updateContentItem(item)
        contentItems.clear(); contentItems.addAll(Session.contentItems())
    }

    fun deleteContent(id: String) {
        Session.deleteContentItem(id)
        contentItems.clear(); contentItems.addAll(Session.contentItems())
    }

    // ── core balances ────────────────────────────────────────────────
    var coins by mutableIntStateOf(0); private set
    var pendingCoins by mutableIntStateOf(0); private set
    var lifetimeEarned by mutableIntStateOf(0); private set
    var lifetimeSpent by mutableIntStateOf(0); private set
    var dailyEarnedCoins by mutableIntStateOf(0); private set
    val dailyCap get() = EconomyConfig.DAILY_EARNING_CAP

    // ── profile ──────────────────────────────────────────────────────
    var loggedIn by mutableStateOf(false); private set
    var displayName by mutableStateOf(""); private set
    var email by mutableStateOf(""); private set
    var tiktokUsername by mutableStateOf(""); private set
    var bio by mutableStateOf(""); private set
    var category by mutableStateOf(""); private set
    var profileCompleteness by mutableIntStateOf(0); private set
    var followedCount by mutableIntStateOf(0); private set
    var returnedCount by mutableIntStateOf(0); private set

    // ── trust ────────────────────────────────────────────────────────
    var myExchanges by mutableIntStateOf(0); private set
    var myDisputes by mutableIntStateOf(0); private set
    var myCompletionRate by mutableIntStateOf(100); private set
    var myTrustLevel by mutableIntStateOf(1); private set
    var myTrustScore by mutableIntStateOf(12); private set

    // ── premium / boost / abuse ──────────────────────────────────────
    var premium by mutableStateOf(PremiumTier.FREE); private set
    var premiumExpiresAt by mutableStateOf<Long?>(null); private set
    var activeBoost by mutableStateOf<Pair<BoostTier, Long>?>(null); private set
    var abuse by mutableStateOf(AbuseStatus.NORMAL); private set
    var cooldownRemainingMs by mutableLongStateOf(0L); private set

    // ── streaks & achievements ───────────────────────────────────────
    var streakDays by mutableIntStateOf(0); private set
    var streakBest by mutableIntStateOf(0); private set
    val achievements = mutableStateListOf<String>()

    // ── collections ─────────────────────────────────────────────────
    val transactions = mutableStateListOf<Transaction>()
    val notifications = mutableStateListOf<AppNotification>()
    val disputes = mutableStateListOf<DisputeRecord>()
    var unreadCount by mutableIntStateOf(0); private set

    // ── animation triggers ──────────────────────────────────────────
    var lastCoinReleaseAt by mutableLongStateOf(0L); private set
    var lastCoinReleaseAmount by mutableIntStateOf(0); private set
    var lastTrustUpAt by mutableLongStateOf(0L); private set

    // ── transient feedback ──────────────────────────────────────────
    var lastResult by mutableStateOf<EconomyResult?>(null)

    fun refresh() {
        themeMode = Session.themeMode
        notificationsEnabled = Session.notificationsEnabled
        hapticsEnabled = Session.hapticsEnabled
        language = Session.language
        discoverable = Session.discoverable
        contentItems.clear(); contentItems.addAll(Session.contentItems())
        coins = Session.coins
        pendingCoins = EconomyService.pendingCoins()
        lifetimeEarned = Session.lifetimeEarned
        lifetimeSpent = Session.lifetimeSpent
        dailyEarnedCoins = EconomyService.dailyEarned()
        loggedIn = Session.isLoggedIn
        displayName = Session.displayName
        email = Session.email
        tiktokUsername = Session.tiktokUsername
        bio = Session.bio
        category = Session.category
        profileCompleteness = computeProfileCompleteness()
        followedCount = Session.followedCount()
        returnedCount = Session.returnedCount()
        refreshTrust()
        premium = EconomyService.premiumActive()
        premiumExpiresAt = Session.premiumExpiresAt
        activeBoost = EconomyService.activeBoost()
        abuse = AbuseStatus.valueOf(Session.abuseStatus)
        cooldownRemainingMs = EconomyService.cooldownRemaining(myTrustLevel, premium)
        streakDays = Session.streakDays
        streakBest = Session.streakBest
        achievements.clear()
        achievements.addAll(Session.unlockedAchievements())
        transactions.clear()
        transactions.addAll(Session.transactions())
        notifications.clear()
        notifications.addAll(Session.notifications())
        disputes.clear()
        disputes.addAll(Session.disputes())
        unreadCount = notifications.count { !it.read }
        // catch up: demo confirmations, expiration, and gentle one-shot reminders
        val now = System.currentTimeMillis()
        val expireMs = EconomyConfig.EXCHANGE_EXPIRATION_HOURS * 3_600_000L
        val remindMs = EconomyConfig.CONFIRMATION_REMINDER_MINUTES * 60_000L
        Session.pendingTransactions().forEach { tx ->
            val age = now - tx.createdAt
            when {
                age > expireMs -> {
                    // unconfirmed exchanges expire quietly — nobody is punished,
                    // and the creator becomes available for a fresh attempt
                    Session.updateTransaction(tx.copy(status = TxStatus.EXPIRED, updatedAt = now))
                    tx.userId?.let { Session.setFollowStatus(it, "expired") }
                    Session.addNotification(
                        "exchange", "Exchange expired",
                        "Your exchange with @${tx.username} wasn't confirmed in time. No action was taken — you can try again later."
                    )
                }
                age > MockData.DEMO_CONFIRM_DELAY + 4_000 -> resolveVerified(tx, notify = true)
                age > remindMs && !Session.wasReminded(tx.id) -> {
                    Session.markReminded(tx.id)
                    val who = tx.username.substringBefore('.').replaceFirstChar { it.uppercase() }
                    Session.addNotification(
                        "exchange", "$who is waiting for your confirmation.",
                        "Confirm the completed exchange so coins can be released."
                    )
                }
            }
        }
        // refresh collections once more if anything above mutated
        transactions.clear(); transactions.addAll(Session.transactions())
        notifications.clear(); notifications.addAll(Session.notifications())
        unreadCount = notifications.count { !it.read }
    }

    private fun refreshTrust() {
        myExchanges = Session.statExchanges
        myDisputes = Session.statDisputes
        myCompletionRate = Trust.completionRateFor(myExchanges, myDisputes)
        myTrustLevel = Trust.levelFor(myExchanges)
        myTrustScore = Trust.scoreFor(
            myExchanges, myCompletionRate, myDisputes,
            Session.accountAgeDays(), computeProfileCompleteness(), Session.suspiciousFlags
        )
    }

    /** Profile completion: name 15 · handle/external 25 · bio 20 · category 20 · picture 20. */
    fun computeProfileCompleteness(): Int {
        var score = 0
        if (Session.displayName.isNotBlank()) score += 15
        if (Session.tiktokUsername.isNotBlank()) score += 25  // external profile derives from handle
        if (Session.bio.isNotBlank()) score += 20
        if (Session.category.isNotBlank()) score += 20
        if (Session.profilePicturePath != null) score += 20
        return score.coerceAtMost(100)
    }

    /** TickTokBoost match score (0-99): a recommendation heuristic, not a prediction. */
    fun matchScore(u: com.tiktokboost.app.data.User): Int {
        var s = 0f
        val myCat = Session.category
        if (myCat.isNotBlank() && u.category == myCat) s += 45f
        s += u.trustScore * 0.20f
        s += u.activityScore * 0.15f
        s += u.profileCompleteness * 0.10f
        if (u.lastActiveMinutesAgo <= 30) s += 10f
        if (followStatus(u.id) != null || !com.tiktokboost.app.data.EconomyService.canPairWith(u.id)) s -= 25f
        return s.toInt().coerceIn(5, 99)
    }

    /** Overall account risk for admin/review surfaces. */
    fun riskLevel(): com.tiktokboost.app.data.RiskLevel {
        var risk = when (abuse) {
            AbuseStatus.NORMAL -> com.tiktokboost.app.data.RiskLevel.LOW
            AbuseStatus.WARNING -> com.tiktokboost.app.data.RiskLevel.MEDIUM
            else -> com.tiktokboost.app.data.RiskLevel.HIGH
        }
        if (Session.suspiciousFlags >= 2 && risk == com.tiktokboost.app.data.RiskLevel.LOW) risk = com.tiktokboost.app.data.RiskLevel.MEDIUM
        if (Session.suspiciousFlags >= 4) risk = com.tiktokboost.app.data.RiskLevel.HIGH
        return risk
    }

    fun isBlocked(userId: String): Boolean = Session.isBlocked(userId)

    fun toggleBlock(userId: String): Boolean = Session.toggleBlock(userId)

    // ── discovery ranking ─────────────────────────────────────────────

    fun rankedCreators(query: String, filter: String, category: String?): List<User> {
        val blocked = Session.blockedIds().toSet()
        val pool = MockData.users.filter { it.id !in blocked }
        val base = if (filter == "For You") pool else MockData.applyFilter(pool, filter)

        // "For You": pure match-score ordering (TickTokBoost recommendation)
        if (filter == "For You") {
            return base.map { it to matchScore(it) }
                .sortedByDescending { it.second }
                .map { it.first }
                .filter { u ->
                    query.isBlank() || u.username.contains(query.trim().removePrefix("@"), true) ||
                        u.displayName.contains(query, true) || u.category.contains(query, true)
                }
                .also { it.take(10).forEach { u -> Session.markSeen(u.id) } }
        }

        val scored = base
            .filter { u ->
                query.isBlank() ||
                    u.username.contains(query.trim().removePrefix("@"), true) ||
                    u.displayName.contains(query, true) ||
                    u.niche.contains(query, true) ||
                    u.category.contains(query, true)
            }
            .filter { u -> category == null || u.category == category }
            .map { u ->
                var s = 0f
                s += EconomyConfig.RANK_TRUST * u.trustScore
                s += EconomyConfig.RANK_ACTIVITY * (u.activityScore * (100f - u.lastActiveMinutesAgo.coerceAtMost(100)) / 100f)
                s += EconomyConfig.RANK_COMPLETENESS * u.profileCompleteness
                if (category != null && u.category == category) s += 0f // already filtered
                if (u.category.isNotBlank()) s += EconomyConfig.RANK_CATEGORY_MATCH * 40
                if (u.premium == PremiumTier.PRO) s += EconomyConfig.RANK_PREMIUM * 100
                else if (u.premium == PremiumTier.PREMIUM) s += EconomyConfig.RANK_PREMIUM * 60
                u.activeBoost?.let { s += EconomyConfig.RANK_BOOST * 100f * (it.rankMultiplier - 1f) * 3f }
                // freshness & history penalties
                Session.lastSeen(u.id)?.let { seen ->
                    val hoursAgo = (System.currentTimeMillis() - seen) / 3_600_000f
                    if (hoursAgo < 24f) s *= (1f - EconomyConfig.RANK_SEEN_PENALTY * (1f - hoursAgo / 24f))
                }
                if (!EconomyService.canPairWith(u.id)) s *= (1f - EconomyConfig.RANK_INTERACTED_PENALTY)
                u to s
            }
            .sortedByDescending { it.second }
            .map { it.first }

        scored.take(10).forEach { Session.markSeen(it.id) }
        return scored
    }

    // ── exchange lifecycle ────────────────────────────────────────────

    /** All gates: restriction, cooldown, pairing, cap, duplicate. */
    fun exchangeBlockReason(user: User): FailureReason? {
        if (Session.isBlocked(user.id)) return FailureReason.ALREADY_EXCHANGED
        if (EconomyService.currentlyRestricted()) return FailureReason.RESTRICTED
        if (followStatus(user.id) != null) return null // already in a relationship state
        if (EconomyService.cooldownRemaining(myTrustLevel, premium) > 0) return FailureReason.COOLDOWN_ACTIVE
        if (!EconomyService.canPairWith(user.id) && Session.followStatus(user.id) == null) return FailureReason.ALREADY_EXCHANGED
        if (EconomyService.dailyRemaining() <= 0) return FailureReason.DAILY_CAP_REACHED
        return null
    }

    fun completeFollow(user: User): EconomyResult {
        exchangeBlockReason(user)?.let { return EconomyResult.Failure(it, detailFor(it)) }
        EconomyService.recordExchangeAction()
        Session.setFollowStatus(user.id, "followed")
        Session.recordPartner(user.id)
        EconomyService.startCooldown(myTrustLevel, premium)
        val now = System.currentTimeMillis()
        val tx = Transaction(
            "tx_$now", user.id, user.username, TxType.FOLLOW, TxStatus.PENDING,
            EconomyConfig.COINS_PER_CONFIRMED_EXCHANGE, now, now,
            "Exchange with @${user.externalHandle} — waiting for confirmation"
        )
        Session.addTransaction(tx)
        Session.addHistory(user.username, "You followed @${user.username}", 0)
        Session.addNotification(
            "exchange", "Exchange pending",
            "You completed an exchange with @${user.username}. ${user.username.substringBefore('.').replaceFirstChar { it.uppercase() }} will confirm shortly."
        )
        refresh()
        scheduleDemoConfirmation(tx.id)
        return EconomyResult.Success(0, "Pending — waiting for @${user.username}'s confirmation")
    }

    private fun String.firstUpper() = replaceFirstChar { it.uppercase() }

    private fun scheduleDemoConfirmation(txId: String) {
        mainHandler.postDelayed({
            val tx = transactions.firstOrNull { it.id == txId } ?: return@postDelayed
            if (tx.status == TxStatus.PENDING) resolveVerified(tx, notify = true)
        }, MockData.DEMO_CONFIRM_DELAY)
    }

    private fun resolveVerified(tx: Transaction, notify: Boolean) {
        val who = tx.username.substringBefore('.').replaceFirstChar { it.uppercase() }
        val result = EconomyService.releasePending(tx)
        if (notify) {
            Session.addNotification("exchange", "$who confirmed your completed exchange.", "Your exchange was verified.")
            if (result is EconomyResult.Success) {
                Session.addNotification("coins", "${result.coins} coins have been released.", "Pending coins moved to your available balance.")
            } else if (result is EconomyResult.Failure && result.reason == FailureReason.DAILY_CAP_REACHED) {
                Session.addNotification("limit", "Coins held until tomorrow", "Daily limit reached — your verified coins will be credited tomorrow.")
            }
        }
        checkAchievements()
        refresh()
        if (result is EconomyResult.Success) {
            lastCoinReleaseAt = System.currentTimeMillis()
            lastCoinReleaseAmount = result.coins
        }
        // trust level-up?
        val after = Trust.levelFor(Session.statExchanges)
        if (after > myTrustLevel) {
            lastTrustUpAt = System.currentTimeMillis()
            Session.addNotification("trust", "Your trust score increased.", "You're now Level $after — ${Trust.tier(after).name}.")
            refresh()
        }
    }

    fun confirmFollowBack(user: User): EconomyResult {
        val res = EconomyService.grantFollowBack(user.username, user.id)
        if (res is EconomyResult.Success) {
            Session.setFollowStatus(user.id, "followed_back")
            Session.addHistory(user.username, "@${user.username} followed you back", (res as EconomyResult.Success).coins)
            checkAchievements()
            refresh()
        }
        return res
    }

    // ── disputes ──────────────────────────────────────────────────────

    fun submitDispute(tx: Transaction, reason: String) {
        if (tx.status != TxStatus.PENDING) return
        val updated = tx.copy(status = TxStatus.DISPUTED, updatedAt = System.currentTimeMillis())
        Session.updateTransaction(updated)
        Session.statDisputes += 1
        val d = DisputeRecord(
            "d_${System.currentTimeMillis()}", tx.id, "You", tx.username,
            System.currentTimeMillis(), reason, DisputeStatus.UNDER_REVIEW
        )
        Session.addDispute(d)
        Session.addNotification("dispute", "Dispute submitted — under review", "Coins for this exchange stay locked until the review is resolved.")
        refresh()
        mainHandler.postDelayed({
            // mock review resolves in the user's favour unless an admin acts first
            val still = Session.disputes().firstOrNull { it.id == d.id }
            if (still != null && still.status == DisputeStatus.UNDER_REVIEW) {
                adminResolveDispute(d.id, true)
            }
        }, MockData.DEMO_DISPUTE_RESOLVE_DELAY)
    }

    fun withdrawDispute(disputeId: String) {
        val d = Session.disputes().firstOrNull { it.id == disputeId } ?: return
        if (d.status == DisputeStatus.UNDER_REVIEW || d.status == DisputeStatus.OPEN) {
            Session.updateDispute(d.copy(status = DisputeStatus.WITHDRAWN))
            val tx = Session.transactions().firstOrNull { it.id == d.txId }
            tx?.let {
                Session.updateTransaction(it.copy(status = TxStatus.PENDING, updatedAt = System.currentTimeMillis()))
            }
            Session.addNotification("dispute", "Dispute withdrawn", "Your exchange is pending confirmation again.")
            refresh()
        }
    }

    /** Admin action from the dashboard. */
    fun adminResolveDispute(disputeId: String, release: Boolean) {
        val d = Session.disputes().firstOrNull { it.id == disputeId } ?: return
        val tx = Session.transactions().firstOrNull { it.id == d.txId }
        Session.updateDispute(
            d.copy(status = if (release) DisputeStatus.RESOLVED_RELEASED else DisputeStatus.RESOLVED_DENIED)
        )
        if (tx != null && (tx.status == TxStatus.DISPUTED || tx.status == TxStatus.PENDING)) {
            if (release) {
                resolveVerified(tx, notify = true)
            } else {
                EconomyService.denyPending(tx)
                Session.addNotification("dispute", "Dispute resolved", "The exchange was closed without a coin release.")
                refresh()
            }
        }
    }

    // ── boosts & premium ─────────────────────────────────────────────

    fun purchaseBoost(tier: BoostTier): EconomyResult {
        val res = EconomyService.purchaseBoost(tier)
        lastResult = res
        refresh()
        return res
    }

    fun purchasePremium(tier: PremiumTier, plan: PremiumPlan): EconomyResult {
        val res = EconomyService.purchasePremium(tier, plan)
        lastResult = res
        refresh()
        return res
    }

    fun cancelPremium() {
        Session.premiumTier = null
        Session.premiumExpiresAt = null
        Session.premiumRenewalNotified = false
        Session.addNotification("premium", "Subscription ended", "You're back on the free plan.")
        refresh()
    }

    // ── small earnings ────────────────────────────────────────────────

    fun checkIn(): EconomyResult {
        if (!Session.canCheckIn()) return EconomyResult.Failure(FailureReason.DUPLICATE, "Already checked in today.")
        Session.doCheckIn()
        val newStreak = Session.advanceStreak()
        var total = 0
        EconomyService.grantSmall("Check-in", TxType.BONUS, EconomyConfig.DAILY_CHECKIN_REWARD, "Daily check-in").let {
            if (it is EconomyResult.Success) total += it.coins
        }
        if (newStreak >= EconomyConfig.STREAK_BONUS_START_DAY) {
            EconomyService.grantSmall("Streak", TxType.STREAK, EconomyConfig.STREAK_BONUS, "$newStreak-day streak bonus").let {
                if (it is EconomyResult.Success) total += it.coins
            }
            Session.addNotification("streak", "🔥 $newStreak-Day Streak", "Keep it going — small bonus added.")
        }
        checkAchievements()
        refresh()
        return EconomyResult.Success(total, "+$total coins")
    }

    fun canCheckIn(): Boolean = Session.canCheckIn()

    /** Central-economy grant used by the quest engine (single path for quest rewards). */
    fun economyGrant(reason: String, amount: Int): EconomyResult =
        EconomyService.grantSmall(reason, TxType.ACHIEVEMENT, amount, reason)

    fun claimTask(id: String, reward: Int, title: String): EconomyResult {
        if (!Session.claimTask(id)) return EconomyResult.Failure(FailureReason.DUPLICATE, "Already claimed.")
        val res = EconomyService.grantSmall(title, TxType.BONUS, reward, "Earned: $title")
        checkAchievements()
        refresh()
        return res
    }

    fun isTaskClaimed(id: String): Boolean = Session.isTaskClaimed(id)

    fun starterBonus(handle: String) {
        Session.addCoins(EconomyConfig.STARTER_COINS)
        Session.lifetimeEarned += EconomyConfig.STARTER_COINS
        val now = System.currentTimeMillis()
        Session.addTransaction(
            Transaction("tx_$now", null, "Starter bonus", TxType.BONUS, TxStatus.COMPLETED,
                EconomyConfig.STARTER_COINS, now, now, "Welcome to TickTokBoost")
        )
        Session.accountCreated = now
        Session.addNotification(
            "coins", "Welcome! ${EconomyConfig.STARTER_COINS} starter coins.",
            "Participate in exchanges to earn more — today's cap is ${EconomyConfig.DAILY_EARNING_CAP}."
        )
    }

    /** Premium small controlled daily bonus (counts toward cap). */
    fun claimPremiumDailyBonus(): EconomyResult {
        if (premium == PremiumTier.FREE) return EconomyResult.Failure(FailureReason.INVALID_STATE, "Premium only")
        if (!Session.canCheckIn()) return EconomyResult.Failure(FailureReason.DUPLICATE, "Claimed with your check-in today.")
        val res = EconomyService.grantSmall("Premium bonus", TxType.BONUS, EconomyConfig.PREMIUM_DAILY_BONUS, "Premium daily bonus")
        refresh()
        return res
    }

    // ── achievements ──────────────────────────────────────────────────

    fun checkAchievements() {
        fun tryUnlock(id: String) {
            val def = Achievements.byId(id) ?: return
            if (Session.unlockAchievement(id)) {
                Session.addCoins(def.reward)
                Session.lifetimeEarned += def.reward
                val now = System.currentTimeMillis()
                Session.addTransaction(
                    Transaction("tx_$now", null, def.title, TxType.ACHIEVEMENT, TxStatus.COMPLETED, def.reward, now, now, "Achievement: ${def.title}")
                )
                Session.addNotification("achievement", "Achievement unlocked: ${def.title} ${def.emoji}", "+${def.reward} coins — nice work.")
            }
        }
        if (Session.statExchanges >= 1) tryUnlock("a_first")
        if (Session.statExchanges >= 10) tryUnlock("a_ten")
        if (Trust.levelFor(Session.statExchanges) >= 3) tryUnlock("a_trusted")
        if (Session.streakDays >= 7) tryUnlock("a_streak7")
        if (computeProfileCompleteness() >= 100) tryUnlock("a_profile")
    }

    // ── notifications ─────────────────────────────────────────────────

    fun markAllRead() {
        Session.markAllNotificationsRead()
        refresh()
    }

    fun followStatus(userId: String): String? = Session.followStatus(userId)

    fun transactionFor(userId: String?): Transaction? =
        transactions.firstOrNull { it.userId == userId && it.type == TxType.FOLLOW }

    // ── admin helpers (demo) ──────────────────────────────────────────

    fun adminResetEconomyGuards() {
        Session.resetEconomyGuards()
        Session.addNotification("system", "Economy guards reset (admin)", "Cooldowns, pairings and daily cap cleared for testing.")
        refresh()
    }

    fun adminClearAbuse() {
        Session.clearAbuse()
        refresh()
    }

    fun detailFor(reason: FailureReason): String = when (reason) {
        FailureReason.DAILY_CAP_REACHED -> "You've reached today's earning limit. Come back tomorrow."
        FailureReason.INSUFFICIENT_COINS -> "Not enough coins."
        FailureReason.COOLDOWN_ACTIVE -> "Exchange cooldown active — please wait."
        FailureReason.ALREADY_EXCHANGED -> "You've already completed an exchange with this creator."
        FailureReason.RESTRICTED -> "Unusual activity detected. Please slow down."
        FailureReason.DUPLICATE -> "Already done."
        FailureReason.BOOST_LIMIT_REACHED -> "Boost limit reached for your plan."
        FailureReason.INVALID_STATE -> "Not available right now."
    }
}
