package com.tiktokboost.app.data

/**
 * Core data models. v0.0.3 adds disputes, achievements, richer trust and
 * premium — while keeping every v0.0.1/v0.0.2 field working.
 */

data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val bio: String,
    val profileUrl: String,
    val followers: Int,
    val followersRequested: Int,
    val hueSeed: Int,
    val country: String,
    val niche: String,
    val externalHandle: String = username,
    val platform: String = "TikTok",
    val trustLevel: Int = 1,
    val trustScore: Int = 40,
    val successfulExchanges: Int = 0,
    val disputes: Int = 0,
    val completionRate: Int = 100,
    val coinReward: Int = EconomyConfig.COINS_PER_CONFIRMED_EXCHANGE,
    val joinedDaysAgo: Int = 90,
    val activityScore: Int = 0,
    val recommended: Boolean = false,
    // v0.0.3
    val category: String = "Other",
    val premium: PremiumTier = PremiumTier.FREE,
    val activeBoost: BoostTier? = null,
    val lastActiveMinutesAgo: Int = 60,
    val profileCompleteness: Int = 60
)

enum class TxStatus { PENDING, VERIFIED, DISPUTED, COMPLETED, EXPIRED }

enum class TxType { FOLLOW, FOLLOW_BACK, BONUS, PURCHASE, BOOST, PREMIUM, STREAK, ACHIEVEMENT }

data class Transaction(
    val id: String,
    val userId: String? = null,
    val username: String,
    val type: TxType,
    val status: TxStatus,
    val coins: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val note: String = ""
) {
    val earned: Boolean get() = coins >= 0
}

data class AppNotification(
    val id: String,
    val kind: String,               // exchange|coins|trust|system|premium|boost|streak|limit|abuse|achievement|dispute
    val title: String,
    val message: String,
    val timestamp: Long,
    val read: Boolean = false
)

data class FollowEvent(
    val id: String,
    val username: String,
    val action: String,
    val coinsEarned: Int,
    val timestamp: Long
)

data class EarnTask(
    val id: String,
    val title: String,
    val description: String,
    val reward: Int,
    val icon: String
)

// ── disputes ─────────────────────────────────────────────────────────────

enum class DisputeStatus { OPEN, UNDER_REVIEW, WITHDRAWN, RESOLVED_RELEASED, RESOLVED_DENIED }

data class DisputeRecord(
    val id: String,
    val txId: String,
    val initiator: String,          // "You" in the demo
    val counterpart: String,        // other user's handle
    val createdAt: Long,
    val reason: String,
    val status: DisputeStatus
)

// ── creator content (photos / short videos shown on public profiles) ───────

enum class MediaType { PHOTO, VIDEO }

data class ContentItem(
    val id: String,
    val mediaType: MediaType,
    val path: String?,            // local file (own uploads) or supported media URL
    val caption: String,
    val createdAt: Long,
    val updatedAt: Long,
    val demoVisual: String? = null // emoji tile for demo creators (no external assets)
)

// ── referrals (persistent, repeatable) ──────────────────────────────────────

enum class ReferralStatus { INVITED, REGISTERED, QUALIFYING, QUALIFIED, REWARDED, REJECTED }

data class Referral(
    val id: String,
    val referrerUserId: String,      // "you" in the local demo
    val referredUserId: String,      // unique per invited friend
    val referredName: String,
    val referralCode: String,
    val status: ReferralStatus,
    val createdAt: Long,
    val qualifiedAt: Long? = null,
    val rewardAmount: Int = 5,
    val rewardTransactionId: String? = null,
    val rewardedAt: Long? = null
)

// ── creator reports (admin review) ─────────────────────────────────────────

data class Report(
    val id: String,
    val targetUsername: String,
    val reason: String,
    val createdAt: Long,
    val resolved: Boolean = false
)

enum class RiskLevel(val label: String) { LOW("Low"), MEDIUM("Medium"), HIGH("High") }

// ── achievements (cosmetic-first) ────────────────────────────────────────

data class AchievementDef(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val reward: Int = EconomyConfig.ACHIEVEMENT_REWARD
)

object Achievements {
    val all = listOf(
        AchievementDef("a_first", "First Exchange", "Complete your first confirmed exchange", "🤝"),
        AchievementDef("a_ten", "10 Successful Exchanges", "Ten confirmed exchanges completed", "🏅"),
        AchievementDef("a_trusted", "Trusted Creator", "Reach Trust Level 3", "🛡️"),
        AchievementDef("a_streak7", "7-Day Streak", "Stay active seven days in a row", "🔥"),
        AchievementDef("a_profile", "Profile Complete", "Reach a 100% complete profile", "✨")
    )
    fun byId(id: String) = all.firstOrNull { it.id == id }
}

// ── trust ────────────────────────────────────────────────────────────────

object Trust {
    data class Tier(
        val level: Int,
        val name: String,
        val minExchanges: Int,
        val color: Long
    )

    val tiers = listOf(
        Tier(1, "New",            0,  0xFF8E99B4),
        Tier(2, "Active",         1,  0xFF25C9D0),
        Tier(3, "Trusted",        3,  0xFF2ECC71),
        Tier(4, "Highly Trusted", 6,  0xFF8B5CF6),
        Tier(5, "Elite",          10, 0xFFFFB020)
    )

    fun tier(level: Int): Tier = tiers.getOrElse(level - 1) { tiers.first() }

    fun levelFor(successfulExchanges: Int): Int {
        var lvl = 1
        for (t in tiers) if (successfulExchanges >= t.minExchanges) lvl = t.level
        return lvl
    }

    /**
     * Trust score v0.0.3: successful exchanges + completion rate + account age
     * + profile completeness − disputes − suspicious-activity flags.
     */
    fun scoreFor(
        successfulExchanges: Int,
        completionRate: Int,
        disputes: Int,
        accountAgeDays: Int,
        profileCompleteness: Int,
        suspiciousFlags: Int
    ): Int {
        val ageBonus = (accountAgeDays.coerceAtMost(90) / 90f) * 12f
        val profileBonus = (profileCompleteness / 100f) * 10f
        val raw = 12f +
            successfulExchanges * 4.5f +
            completionRate / 8f +
            ageBonus + profileBonus -
            disputes * 5f -
            suspiciousFlags * 8f
        return raw.toInt().coerceIn(0, 100)
    }

    /** Completion rate of the local user (verified vs disputed exchanges). */
    fun completionRateFor(exchanges: Int, disputes: Int): Int =
        if (exchanges + disputes == 0) 100
        else (exchanges * 100 / (exchanges + disputes)).coerceIn(0, 100)

    fun toNextLevel(successfulExchanges: Int): Int? {
        val next = tiers.firstOrNull { it.minExchanges > successfulExchanges } ?: return null
        return next.minExchanges - successfulExchanges
    }
}
