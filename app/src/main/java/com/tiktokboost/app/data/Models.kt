package com.tiktokboost.app.data

/**
 * Core data models for TikTokBoost.
 * v0.0.2 adds the trust system, transactions with lifecycle states and
 * notifications, while keeping every v0.0.1 field working.
 */

/** A TikTok creator on the exchange. In v0.0.1 these come from [MockData]; later versions will load them from a backend. */
data class User(
    val id: String,
    val username: String,           // app handle, without the @
    val displayName: String,
    val bio: String,
    val profileUrl: String,         // https://www.tiktok.com/@handle
    val followers: Int,             // current TikTok follower count
    val followersRequested: Int,    // how many follow-backs they are seeking
    val hueSeed: Int,               // used to pick the avatar gradient
    val country: String,            // flag emoji
    val niche: String,
    // ---- v0.0.2 trust & exchange metadata ----
    val externalHandle: String = username,  // handle on the external platform
    val platform: String = "TikTok",
    val trustLevel: Int = 1,        // 1..5, see [Trust]
    val trustScore: Int = 40,       // 0..100
    val successfulExchanges: Int = 0,
    val disputes: Int = 0,
    val completionRate: Int = 100,  // percentage
    val coinReward: Int = 10,       // reward for completing an exchange with this creator
    val joinedDaysAgo: Int = 90,
    val activityScore: Int = 0,     // for "Most Active" sorting
    val recommended: Boolean = false
)

/** Lifecycle states of an exchange transaction. */
enum class TxStatus { PENDING, VERIFIED, DISPUTED, COMPLETED }

/** What a transaction represents. */
enum class TxType { FOLLOW, FOLLOW_BACK, BONUS, PURCHASE }

/** One exchange / coin movement. Replaces the flat v0.0.1 history entries (kept in sync during migration). */
data class Transaction(
    val id: String,
    val userId: String? = null,
    val username: String,
    val type: TxType,
    val status: TxStatus,
    val coins: Int,                 // signed: positive = earned, negative = spent
    val createdAt: Long,
    val updatedAt: Long,
    val note: String = ""
) {
    val earned: Boolean get() = coins >= 0
}

/** An in-app notification. */
data class AppNotification(
    val id: String,
    val kind: String,               // exchange | coins | trust | system
    val title: String,
    val message: String,
    val timestamp: Long,
    val read: Boolean = false
)

/** One entry in the user's follow activity history (v0.0.1 model, still populated for compatibility). */
data class FollowEvent(
    val id: String,
    val username: String,
    val action: String,
    val coinsEarned: Int,
    val timestamp: Long
)

/** A way to earn coins on the Earn Coins screen. */
data class EarnTask(
    val id: String,
    val title: String,
    val description: String,
    val reward: Int,
    val icon: String
)

/**
 * The five-tier trust ladder.
 * Levels rise with verified successful exchanges; disputes are tracked and
 * shown but never permanently punish a demo user.
 */
object Trust {
    data class Tier(
        val level: Int,
        val name: String,
        val minExchanges: Int,
        val color: Long        // ARGB
    )

    val tiers = listOf(
        Tier(1, "New",           0,  0xFF8E99B4),
        Tier(2, "Active",        1,  0xFF25C9D0),
        Tier(3, "Trusted",       3,  0xFF2ECC71),
        Tier(4, "Highly Trusted",6,  0xFF8B5CF6),
        Tier(5, "Elite",         10, 0xFFFFB020)
    )

    fun tier(level: Int): Tier = tiers.getOrElse(level - 1) { tiers.first() }

    fun levelFor(successfulExchanges: Int): Int {
        var lvl = 1
        for (t in tiers) if (successfulExchanges >= t.minExchanges) lvl = t.level
        return lvl
    }

    fun scoreFor(successfulExchanges: Int, completionRate: Int, disputes: Int): Int {
        val raw = 20 + successfulExchanges * 7 + completionRate / 5 - disputes * 4
        return raw.coerceIn(0, 100)
    }

    /** How many more verified exchanges are needed to reach the next tier (null at max). */
    fun toNextLevel(successfulExchanges: Int): Int? {
        val next = tiers.firstOrNull { it.minExchanges > successfulExchanges } ?: return null
        return next.minExchanges - successfulExchanges
    }
}
