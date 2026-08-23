package com.tiktokboost.app.data

/**
 * Demo data so the complete UI, follow-exchange workflow and trust system can
 * be built and tested before any external services are connected.
 * All handles are fictional. v0.0.2 adds the Sarah demo pair: Sarah is the
 * flagship recommended creator used in the guided demo flow.
 */
object MockData {

    val users = listOf(
        // ---- flagship demo creators ----
        User(
            "u13", "sarah.creates", "Sarah Mwangi", "Lifestyle & day-in-my-life content ☀️ Let's grow together!",
            "https://www.tiktok.com/@sarah.creates", 8400, 500, 5, "\uD83C\uDDF0\uD83C\uDDEA", "Lifestyle",
            externalHandle = "sarah.creates", trustLevel = 3, trustScore = 78,
            successfulExchanges = 14, disputes = 0, completionRate = 96, coinReward = 25,
            joinedDaysAgo = 45, activityScore = 92, recommended = true
        ),
        User(
            "u14", "john.grams", "John Otieno", "Skits & street interviews 🎤 New drop every Sunday",
            "https://www.tiktok.com/@john.grams", 5100, 400, 2, "\uD83C\uDDF0\uD83C\uDDEA", "Comedy",
            externalHandle = "john.grams", trustLevel = 2, trustScore = 61,
            successfulExchanges = 5, disputes = 0, completionRate = 91, coinReward = 18,
            joinedDaysAgo = 6, activityScore = 74, recommended = true
        ),
        // ---- established community ----
        User("u1", "kevin.creates", "Kevin Creates", "Daily tech & editing tips 🔥", "https://www.tiktok.com/@kevin.creates", 12400, 500, 0, "\uD83C\uDDF0\uD83C\uDDEA", "Tech",
            trustLevel = 4, trustScore = 88, successfulExchanges = 33, disputes = 1, completionRate = 97, coinReward = 20,
            joinedDaysAgo = 210, activityScore = 88, recommended = true),
        User("u2", "maya.gaming", "Maya", "Mobile gaming, live streams, glhf ⚡", "https://www.tiktok.com/@maya.gaming", 8700, 400, 1, "\uD83C\uDDFA\uD83C\uDDF8", "Gaming",
            trustLevel = 3, trustScore = 74, successfulExchanges = 11, disputes = 0, completionRate = 93, coinReward = 15,
            joinedDaysAgo = 120, activityScore = 65),
        User("u3", "beats.by.nyash", "Nyash Beats", "Afrobeats producer, drops every Friday 🎧", "https://www.tiktok.com/@beats.by.nyash", 20300, 1000, 2, "\uD83C\uDDFF\uD83C\uDDF2", "Music",
            trustLevel = 5, trustScore = 95, successfulExchanges = 61, disputes = 1, completionRate = 99, coinReward = 30,
            joinedDaysAgo = 400, activityScore = 90, recommended = true),
        User("u4", "fit.with.sam", "Sam Fit", "Home workouts + meal prep, no gym needed 💪", "https://www.tiktok.com/@fit.with.sam", 5600, 300, 3, "\uD83C\uDDFF\uD83C\uDDFC", "Fitness",
            trustLevel = 2, trustScore = 58, successfulExchanges = 3, disputes = 0, completionRate = 90, coinReward = 12,
            joinedDaysAgo = 18, activityScore = 48),
        User("u5", "thriftwithtara", "Tara Thrift", "Affordable streetwear finds 🛍️", "https://www.tiktok.com/@thriftwithtara", 9800, 600, 4, "\uD83C\uDDF3\uD83C\uDDFF", "Fashion",
            trustLevel = 4, trustScore = 85, successfulExchanges = 27, disputes = 2, completionRate = 94, coinReward = 22,
            joinedDaysAgo = 260, activityScore = 71),
        User("u6", "chef.amara", "Chef Amara", "10-minute African recipes 🍲", "https://www.tiktok.com/@chef.amara", 15200, 800, 5, "\uD83C\uDDE8\uD83C\uDDFF", "Food",
            trustLevel = 3, trustScore = 77, successfulExchanges = 10, disputes = 0, completionRate = 95, coinReward = 15,
            joinedDaysAgo = 150, activityScore = 56),
        User("u7", "wander.lens", "Lens Wander", "Hidden gems, budget travel ✈️", "https://www.tiktok.com/@wander.lens", 6400, 350, 6, "\uD83C\uDDFF\uD83C\uDDE6", "Travel",
            trustLevel = 1, trustScore = 41, successfulExchanges = 0, disputes = 0, completionRate = 100, coinReward = 10,
            joinedDaysAgo = 3, activityScore = 35),
        User("u8", "zainab.skits", "Zainab", "Relatable skits from campus 🎭", "https://www.tiktok.com/@zainab.skits", 31000, 1500, 7, "\uD83C\uDDF0\uD83C\uDDEA", "Comedy",
            trustLevel = 5, trustScore = 93, successfulExchanges = 48, disputes = 1, completionRate = 98, coinReward = 28,
            joinedDaysAgo = 500, activityScore = 84, recommended = true),
        User("u9", "paint.bydana", "Dana Arts", "Digital art timelapses 🎨", "https://www.tiktok.com/@paint.bydana", 4300, 250, 0, "\uD83C\uDDF0\uD83C\uDDEA", "Art",
            trustLevel = 1, trustScore = 44, successfulExchanges = 0, disputes = 0, completionRate = 100, coinReward = 10,
            joinedDaysAgo = 9, activityScore = 40),
        User("u10", "dancewithjoe", "Joe Dance", "Afro-dance tutorials, beginner friendly 💃", "https://www.tiktok.com/@dancewithjoe", 18900, 900, 1, "\uD83C\uDDFF\uD83C\uDDF8", "Dance",
            trustLevel = 4, trustScore = 87, successfulExchanges = 25, disputes = 0, completionRate = 96, coinReward = 22,
            joinedDaysAgo = 300, activityScore = 77),
        User("u11", "money.with.mel", "Mel Money", "Side hustles & personal finance 💰", "https://www.tiktok.com/@money.with.mel", 11200, 700, 2, "\uD83C\uDDF0\uD83C\uDDEA", "Business",
            trustLevel = 3, trustScore = 76, successfulExchanges = 12, disputes = 1, completionRate = 92, coinReward = 18,
            joinedDaysAgo = 170, activityScore = 62),
        User("u12", "paws.of.luna", "Luna's Human", "Adventures of Luna the husky 🐺", "https://www.tiktok.com/@paws.of.luna", 7600, 450, 3, "\uD83C\uDDEA\uD83C\uDDFA", "Pets",
            trustLevel = 2, trustScore = 64, successfulExchanges = 6, disputes = 0, completionRate = 94, coinReward = 14,
            joinedDaysAgo = 55, activityScore = 58)
    )

    /** Exchange filters offered on the Discovery screen. */
    val filters = listOf("Recommended", "New", "Trusted", "Most Active")

    fun applyFilter(users: List<User>, filter: String): List<User> = when (filter) {
        "Recommended" -> users.filter { it.recommended }
        "New" -> users.filter { it.joinedDaysAgo <= 14 }.sortedBy { it.joinedDaysAgo }
        "Trusted" -> users.filter { it.trustLevel >= 4 }.sortedByDescending { it.trustScore }
        "Most Active" -> users.sortedByDescending { it.activityScore }
        else -> users
    }

    val earnTasks = listOf(
        EarnTask("t_checkin", "Daily Check-in", "Claim your free daily bonus", 2, "\uD83D\uDCC5"),
        EarnTask("t_follow3", "Follow 3 creators", "Follow 3 users from the Exchange", 15, "\uD83E\uDD1D"),
        EarnTask("t_profile", "Complete your profile", "Add your display name and TikTok handle", 10, "\u2728"),
        EarnTask("t_invite", "Invite a friend", "Share your invite link with a friend", 50, "\uD83C\uDF81"),
        EarnTask("t_share", "Share the app", "Share TikTokBoost with your community", 5, "\uD83D\uDCE4")
    )

    const val WELCOME_BONUS = 50
    const val REWARD_FOLLOW = 10
    const val REWARD_FOLLOW_BACK = 5

    /** Delay before the demo counterpart confirms a completed exchange (ms). */
    const val DEMO_CONFIRM_DELAY = 8_000L

    /** Delay before a disputed demo exchange resolves (ms). */
    const val DEMO_DISPUTE_RESOLVE_DELAY = 15_000L
}
