package com.tiktokboost.app.data

/**
 * ═══════════════════════════════════════════════════════════════════════════
 *  TickTokBoost v0.0.3 — central economy configuration & service.
 *  Every coin calculation in the app flows through EconomyConfig /
 *  EconomyService so the economy can be tuned without touching UI code.
 * ═══════════════════════════════════════════════════════════════════════════
 */

object EconomyConfig {
    // ---- earning -------------------------------------------------------------
    const val STARTER_COINS = 20
    const val COINS_PER_CONFIRMED_EXCHANGE = 5
    const val COINS_PER_FOLLOW_BACK = 3
    const val DAILY_EARNING_CAP = 25
    const val EXCHANGE_COOLDOWN_MINUTES = 10

    // ---- anti-farming ---------------------------------------------------------
    const val REPEAT_PAIR_COOLDOWN_HOURS = 24      // same two users can't farm each other
    const val BURST_WINDOW_MINUTES = 5             // rapid-action detection window
    const val BURST_LIMIT = 3                      // completions allowed per window
    const val DISPUTES_PER_WEEK_LIMIT = 3
    const val RESTRICT_DURATION_MINUTES = 10

    // ---- streaks & achievements (deliberately SMALL rewards) -------------------
    const val DAILY_CHECKIN_REWARD = 1
    const val STREAK_BONUS = 2                     // from day 3 onward
    const val STREAK_BONUS_START_DAY = 3
    const val ACHIEVEMENT_REWARD = 2

    // ---- visibility boosts -------------------------------------------------------
    const val BOOST_STANDARD_COST = 10
    const val BOOST_BOOSTED_COST = 25
    const val BOOST_FEATURED_COST = 50
    const val BOOST_DURATION_HOURS = 24
    const val BOOST_LIMIT_FREE = 1
    const val BOOST_LIMIT_PREMIUM = 2
    const val BOOST_LIMIT_PRO = 3

    // ---- premium (mock pricing for display only in v0.0.3) ------------------------
    const val PREMIUM_MONTHLY_CENTS = 499
    const val PREMIUM_YEARLY_CENTS = 2999
    const val PRO_MONTHLY_CENTS = 999
    const val PRO_YEARLY_CENTS = 5999
    const val PREMIUM_DAILY_BONUS = 3              // small controlled daily bonus
    const val PREMIUM_COOLDOWN_FACTOR = 0.8f       // premium shortens cooldowns by 20%
    const val TRUST3_COOLDOWN_FACTOR = 0.7f        // L3+ shortens cooldowns by 30%
    const val TRUST5_COOLDOWN_FACTOR = 0.5f        // L5 shortens cooldowns by 50%

    // ---- discovery ranking weights (relative) --------------------------------------
    const val RANK_TRUST = 0.30f
    const val RANK_ACTIVITY = 0.18f
    const val RANK_COMPLETENESS = 0.14f            // profile completion
    const val RANK_CATEGORY_MATCH = 0.14f
    const val RANK_PREMIUM = 0.12f
    const val RANK_BOOST = 0.20f
    const val RANK_SEEN_PENALTY = 0.25f            // down-rank recently shown creators
    const val RANK_INTERACTED_PENALTY = 0.45f      // down-rank past partners

    val categories = listOf(
        "Gaming", "Football", "Music", "Comedy", "Fashion",
        "Lifestyle", "Education", "Tech", "Beauty", "Other"
    )
}

/** Visibility boost tiers. Boosts never guarantee followers — only visibility inside TickTokBoost. */
enum class BoostTier(val label: String, val cost: Int, val rankMultiplier: Float) {
    STANDARD("Standard", EconomyConfig.BOOST_STANDARD_COST, 1.15f),
    BOOSTED("Boosted", EconomyConfig.BOOST_BOOSTED_COST, 1.35f),
    FEATURED("Featured", EconomyConfig.BOOST_FEATURED_COST, 1.60f)
}

/** Subscription tiers. Mock payment flow only in v0.0.3 — no real payments. */
enum class PremiumTier(val label: String) {
    FREE("Free"),
    PREMIUM("Premium"),
    PRO("Premium Pro")
}

enum class PremiumPlan(val months: Int) { MONTHLY(1), YEARLY(12) }

/** Why an economy operation was refused — mapped to friendly messages in the UI. */
enum class FailureReason {
    DAILY_CAP_REACHED,
    INSUFFICIENT_COINS,
    COOLDOWN_ACTIVE,
    ALREADY_EXCHANGED,
    RESTRICTED,
    DUPLICATE,
    BOOST_LIMIT_REACHED,
    INVALID_STATE
}

sealed class EconomyResult {
    data class Success(val coins: Int = 0, val message: String? = null) : EconomyResult()
    data class Failure(val reason: FailureReason, val detail: String = "") : EconomyResult()

    val ok: Boolean get() = this is Success
}

/** Abuse-status ladder: Warning → Review → Restriction. Never an automatic permanent ban. */
enum class AbuseStatus(val label: String) {
    NORMAL("Normal"),
    WARNING("Warning"),
    REVIEW("Under review"),
    RESTRICTED("Restricted")
}

/**
 * Single source of truth for coin mutations. All screens call through
 * AppState/EconomyService — no screen ever computes a balance itself.
 * Every mutation writes a transaction record; balances can never go negative.
 */
object EconomyService {

    private val today: String get() = Session.today()

    // ── daily earning cap ────────────────────────────────────────────────
    fun dailyEarned(): Int {
        return if (Session.dailyEarnDate == today) Session.dailyEarned else 0
    }

    fun dailyRemaining(): Int = (EconomyConfig.DAILY_EARNING_CAP - dailyEarned()).coerceAtLeast(0)

    private fun recordEarning(amount: Int) {
        if (Session.dailyEarnDate != today) {
            Session.dailyEarned = 0
            Session.dailyEarnDate = today
        }
        val before = Session.dailyEarned
        Session.dailyEarned = (before + amount).coerceAtMost(EconomyConfig.DAILY_EARNING_CAP)
        if (dailyRemaining() == 0 && before < EconomyConfig.DAILY_EARNING_CAP) {
            Session.addNotification(
                "limit", "Daily earning limit reached.",
                "You've reached today's earning limit. Come back tomorrow."
            )
        }
    }

    // ── earning: coins land only after confirmation ──────────────────────
    fun grantConfirmed(username: String, userId: String?, reason: String): EconomyResult {
        if (hasDuplicate(TxType.FOLLOW, username, minutes = 2)) return EconomyResult.Failure(FailureReason.DUPLICATE)
        val amount = EconomyConfig.COINS_PER_CONFIRMED_EXCHANGE
        val room = dailyRemaining()
        if (room <= 0) {
            return EconomyResult.Failure(FailureReason.DAILY_CAP_REACHED,
                "You've reached today's earning limit. Come back tomorrow.")
        }
        val grant = minOf(amount, room)
        Session.addCoins(grant)
        Session.lifetimeEarned += grant
        recordEarning(grant)
        addTx(username, userId, TxType.FOLLOW, TxStatus.VERIFIED, grant, reason)
        return EconomyResult.Success(grant)
    }

    fun grantFollowBack(username: String, userId: String?): EconomyResult {
        if (hasDuplicate(TxType.FOLLOW_BACK, username, minutes = 2)) return EconomyResult.Failure(FailureReason.DUPLICATE)
        val room = dailyRemaining()
        if (room <= 0) return EconomyResult.Failure(FailureReason.DAILY_CAP_REACHED,
            "You've reached today's earning limit. Come back tomorrow.")
        val grant = minOf(EconomyConfig.COINS_PER_FOLLOW_BACK, room)
        Session.addCoins(grant)
        Session.lifetimeEarned += grant
        recordEarning(grant)
        addTx(username, userId, TxType.FOLLOW_BACK, TxStatus.COMPLETED, grant, "@$username followed you back")
        return EconomyResult.Success(grant)
    }

    fun grantSmall(username: String, type: TxType, amount: Int, reason: String): EconomyResult {
        if (hasDuplicate(type, username, minutes = 2)) return EconomyResult.Failure(FailureReason.DUPLICATE)
        val room = dailyRemaining()
        if (room <= 0) return EconomyResult.Failure(FailureReason.DAILY_CAP_REACHED,
            "You've reached today's earning limit. Come back tomorrow.")
        val grant = minOf(amount, room)
        Session.addCoins(grant)
        Session.lifetimeEarned += grant
        recordEarning(grant)
        addTx(username, null, type, TxStatus.COMPLETED, grant, reason)
        return EconomyResult.Success(grant)
    }

    // ── spending ─────────────────────────────────────────────────────────
    fun spend(title: String, amount: Int, type: TxType = TxType.PURCHASE): EconomyResult {
        if (amount <= 0) return EconomyResult.Failure(FailureReason.INVALID_STATE)
        if (Session.coins < amount) {
            return EconomyResult.Failure(FailureReason.INSUFFICIENT_COINS,
                "Not enough coins — you need $amount and have ${Session.coins}.")
        }
        Session.spendCoins(amount)
        Session.lifetimeSpent += amount
        addTx(title, null, type, TxStatus.COMPLETED, -amount, title)
        return EconomyResult.Success(-amount)
    }

    // ── pending lifecycle ────────────────────────────────────────────────
    /** Pending coins are always DERIVED from PENDING transactions — single source of truth. */
    fun pendingCoins(): Int = Session.transactions()
        .filter { it.status == TxStatus.PENDING || it.status == TxStatus.DISPUTED }
        .sumOf { it.coins }

    fun releasePending(tx: Transaction): EconomyResult {
        val updated = tx.copy(status = TxStatus.VERIFIED, updatedAt = System.currentTimeMillis())
        Session.updateTransaction(updated)
        Session.statExchanges += 1
        return grantConfirmed(tx.username, tx.userId, "Confirmed exchange with @${tx.username}")
    }

    /** Resolve a disputed exchange without releasing coins (admin deny path). */
    fun denyPending(tx: Transaction) {
        val updated = tx.copy(status = TxStatus.COMPLETED, updatedAt = System.currentTimeMillis())
        Session.updateTransaction(updated)
    }

    // ── boosts ───────────────────────────────────────────────────────────
    fun activeBoost(): Pair<BoostTier, Long>? {
        val b = Session.activeBoostUntil ?: return null
        val tierName = Session.activeBoostTier ?: return null
        val now = System.currentTimeMillis()
        if (b > now) {
            val t = BoostTier.entries.firstOrNull { it.name == tierName } ?: return null
            return t to b
        }
        if (tierName.isNotBlank()) {
            Session.addNotification("boost", "Boost expired.", "Your ${tierName.lowercase()} boost has ended.")
            Session.clearBoost()
        }
        return null
    }

    fun boostLimit(tier: PremiumTier): Int = when (tier) {
        PremiumTier.FREE -> EconomyConfig.BOOST_LIMIT_FREE
        PremiumTier.PREMIUM -> EconomyConfig.BOOST_LIMIT_PREMIUM
        PremiumTier.PRO -> EconomyConfig.BOOST_LIMIT_PRO
    }

    fun purchaseBoost(tier: BoostTier): EconomyResult {
        val current = activeBoost()
        if (current != null) {
            return EconomyResult.Failure(FailureReason.ALREADY_EXCHANGED,
                "A ${current.first.label} boost is already active.")
        }
        val spend = spend("${tier.label} Boost — ${EconomyConfig.BOOST_DURATION_HOURS}h", tier.cost, TxType.BOOST)
        if (spend is EconomyResult.Failure) return spend
        val until = System.currentTimeMillis() + EconomyConfig.BOOST_DURATION_HOURS * 3_600_000L
        Session.setBoost(tier.name, until)
        Session.addNotification(
            "boost", "Boost activated 🚀",
            "Your ${tier.label} boost is live for ${EconomyConfig.BOOST_DURATION_HOURS} hours — better visibility in TickTokBoost discovery."
        )
        return EconomyResult.Success(-tier.cost, "${tier.label} boost active!")
    }

    // ── premium (mock) ───────────────────────────────────────────────────
    fun purchasePremium(tier: PremiumTier, plan: PremiumPlan): EconomyResult {
        if (tier == PremiumTier.FREE) return EconomyResult.Failure(FailureReason.INVALID_STATE)
        if (Session.premiumTier == tier.name) return EconomyResult.Failure(FailureReason.DUPLICATE, "Already subscribed.")
        val now = System.currentTimeMillis()
        val ms = plan.months * 30L * 24 * 3_600_000
        Session.premiumTier = tier.name
        Session.premiumActivatedAt = now
        Session.premiumExpiresAt = now + ms
        Session.addNotification(
            "premium", "${tier.label} activated ✨",
            "Welcome to TickTokBoost ${tier.label}! Enjoy better tools, visibility and analytics inside TickTokBoost."
        )
        addTx("${tier.label} (${plan.name.lowercase()})", null, TxType.PREMIUM, TxStatus.COMPLETED, 0, "Mock purchase — ${tier.label} ${plan.name.lowercase()}")
        return EconomyResult.Success(0, "$tier activated")
    }

    fun premiumActive(): PremiumTier {
        val exp = Session.premiumExpiresAt
        val name = Session.premiumTier
        if (name.isNullOrBlank() || exp == null) return PremiumTier.FREE
        if (exp < System.currentTimeMillis()) {
            Session.premiumTier = null
            Session.premiumExpiresAt = null
            Session.addNotification("premium", "${PremiumTier.entries.first { it.name == name }.label} expired.", "Your subscription ended. Renew anytime.")
            return PremiumTier.FREE
        }
        // mock renewal nudge 3 days before expiry
        if (exp - System.currentTimeMillis() < 3 * 24 * 3_600_000L && !Session.premiumRenewalNotified) {
            Session.premiumRenewalNotified = true
            Session.addNotification("premium", "Renewal coming up", "Your subscription renews soon (mock).")
        }
        return PremiumTier.entries.first { it.name == name }
    }

    // ── cooldowns & pairing ──────────────────────────────────────────────
    fun cooldownMillis(trustLevel: Int, premium: PremiumTier): Long {
        var factor = 1f
        if (trustLevel >= 5) factor = EconomyConfig.TRUST5_COOLDOWN_FACTOR
        else if (trustLevel >= 3) factor = EconomyConfig.TRUST3_COOLDOWN_FACTOR
        if (premium != PremiumTier.FREE) factor *= EconomyConfig.PREMIUM_COOLDOWN_FACTOR
        return (EconomyConfig.EXCHANGE_COOLDOWN_MINUTES * 60_000L * factor).toLong()
    }

    fun cooldownRemaining(trustLevel: Int, premium: PremiumTier): Long {
        val until = Session.cooldownUntil ?: return 0L
        return (until - System.currentTimeMillis()).coerceAtLeast(0)
            .takeIf { it > 0 } ?: 0L
    }

    fun startCooldown(trustLevel: Int, premium: PremiumTier) {
        Session.cooldownUntil = System.currentTimeMillis() + cooldownMillis(trustLevel, premium)
    }

    fun canPairWith(userId: String): Boolean {
        val last = Session.partnerHistory(userId) ?: return true
        val blockMs = EconomyConfig.REPEAT_PAIR_COOLDOWN_HOURS * 3_600_000L
        return System.currentTimeMillis() - last > blockMs
    }

    // ── anti-abuse ───────────────────────────────────────────────────────
    fun recordExchangeAction() {
        Session.recordActionTimestamp()
        val window = EconomyConfig.BURST_WINDOW_MINUTES * 60_000L
        val recent = Session.recentActionCount(window)
        when {
            recent > EconomyConfig.BURST_LIMIT * 2 && Session.abuseStatus != AbuseStatus.RESTRICTED.name -> {
                Session.abuseStatus = AbuseStatus.RESTRICTED.name
                Session.restrictedUntil = System.currentTimeMillis() + EconomyConfig.RESTRICT_DURATION_MINUTES * 60_000L
                Session.addNotification("abuse", "Account temporarily restricted.",
                    "Unusual activity detected. Exchange rewards are paused for ${EconomyConfig.RESTRICT_DURATION_MINUTES} minutes.")
            }
            recent > EconomyConfig.BURST_LIMIT && Session.abuseStatus == AbuseStatus.NORMAL.name -> {
                Session.abuseStatus = AbuseStatus.WARNING.name
                Session.addNotification("abuse", "Unusual activity detected. Please slow down.",
                    "Completing exchanges too quickly can look like farming.")
            }
            recent > EconomyConfig.BURST_LIMIT && Session.abuseStatus == AbuseStatus.WARNING.name -> {
                Session.abuseStatus = AbuseStatus.REVIEW.name
                Session.addNotification("abuse", "Activity under review.",
                    "Our demo review system is watching recent activity. Everything still works.")
            }
        }
    }

    fun currentlyRestricted(): Boolean {
        if (Session.abuseStatus != AbuseStatus.RESTRICTED.name) return false
        val until = Session.restrictedUntil ?: return false
        if (System.currentTimeMillis() > until) {
            Session.abuseStatus = AbuseStatus.REVIEW.name  // step back, never ban
            return false
        }
        return true
    }

    // ── helpers ──────────────────────────────────────────────────────────
    private fun addTx(username: String, userId: String?, type: TxType, status: TxStatus, coins: Int, reason: String) {
        val now = System.currentTimeMillis()
        Session.addTransaction(
            Transaction("tx_$now" + "_${type.ordinal}", userId, username, type, status, coins, now, now, reason)
        )
    }

    private fun hasDuplicate(type: TxType, username: String, minutes: Int): Boolean {
        val window = minutes * 60_000L
        return Session.transactions().any {
            it.type == type && it.username == username &&
                (it.status == TxStatus.VERIFIED || it.status == TxStatus.PENDING) &&
                it.createdAt > System.currentTimeMillis() - window
        }
    }
}
