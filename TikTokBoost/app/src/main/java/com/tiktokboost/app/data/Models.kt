package com.tiktokboost.app.data

/**
 * A TikTok creator on the exchange. In v0.0.1 these come from [MockData];
 * later versions will load them from a backend.
 */
data class User(
    val id: String,
    val username: String,           // TikTok handle, without the @
    val displayName: String,
    val bio: String,
    val profileUrl: String,         // https://www.tiktok.com/@handle
    val followers: Int,             // current TikTok follower count
    val followersRequested: Int,    // how many follow-backs they are seeking
    val hueSeed: Int,               // used to pick the avatar gradient
    val country: String,            // flag emoji
    val niche: String
)

/** One entry in the user's follow activity history. */
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
