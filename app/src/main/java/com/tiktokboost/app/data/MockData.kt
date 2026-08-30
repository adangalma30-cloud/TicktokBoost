package com.tiktokboost.app.data

/**
 * Demo data. All handles fictional. v0.0.3 adds categories, premium demo
 * creators and boost metadata. Rewards are uniform & driven by EconomyConfig
 * so the coin economy stays balanced.
 */
object MockData {

    val users = listOf(
        // ── flagship demo pair ──────────────────────────────────────────
        User(
            "u13", "sarah.creates", "Sarah Mwangi", "Lifestyle & day-in-my-life content ☀️ Let's grow together!",
            "https://www.tiktok.com/@sarah.creates", 8400, 500, 5, "\uD83C\uDDF0\uD83C\uDDEA", "Lifestyle",
            trustLevel = 3, trustScore = 76, successfulExchanges = 14, disputes = 0,
            completionRate = 96, joinedDaysAgo = 45, activityScore = 92, recommended = true,
            category = "Lifestyle", lastActiveMinutesAgo = 3, profileCompleteness = 90
        ),
        User(
            "u14", "john.grams", "John Otieno", "Skits & street interviews 🎤 New drop every Sunday",
            "https://www.tiktok.com/@john.grams", 5100, 400, 2, "\uD83C\uDDF0\uD83C\uDDEA", "Comedy",
            trustLevel = 2, trustScore = 61, successfulExchanges = 5, disputes = 0,
            completionRate = 91, coinReward = EconomyConfig.COINS_PER_CONFIRMED_EXCHANGE,
            joinedDaysAgo = 6, activityScore = 74, recommended = true,
            category = "Comedy", lastActiveMinutesAgo = 12, profileCompleteness = 75
        ),
        // ── established community ───────────────────────────────────────
        User("u1", "kevin.creates", "Kevin Creates", "Daily tech & editing tips 🔥", "https://www.tiktok.com/@kevin.creates", 12400, 500, 0, "\uD83C\uDDF0\uD83C\uDDEA", "Tech",
            trustLevel = 4, trustScore = 86, successfulExchanges = 33, disputes = 1, completionRate = 97,
            joinedDaysAgo = 210, activityScore = 88, recommended = true,
            category = "Tech", premium = PremiumTier.PREMIUM, lastActiveMinutesAgo = 25, profileCompleteness = 95),
        User("u2", "maya.gaming", "Maya", "Mobile gaming, live streams, glhf ⚡", "https://www.tiktok.com/@maya.gaming", 8700, 400, 1, "\uD83C\uDDFA\uD83C\uDDF8", "Gaming",
            trustLevel = 3, trustScore = 74, successfulExchanges = 11, disputes = 0, completionRate = 93,
            joinedDaysAgo = 120, activityScore = 65, category = "Gaming", lastActiveMinutesAgo = 90, profileCompleteness = 80),
        User("u3", "beats.by.nyash", "Nyash Beats", "Afrobeats producer, drops every Friday 🎧", "https://www.tiktok.com/@beats.by.nyash", 20300, 1000, 2, "\uD83C\uDDFF\uD83C\uDDF2", "Music",
            trustLevel = 5, trustScore = 94, successfulExchanges = 61, disputes = 1, completionRate = 99,
            joinedDaysAgo = 400, activityScore = 90, recommended = true,
            category = "Music", premium = PremiumTier.PRO, activeBoost = BoostTier.FEATURED, lastActiveMinutesAgo = 1, profileCompleteness = 100),
        User("u4", "fit.with.sam", "Sam Fit", "Home workouts + meal prep, no gym needed 💪", "https://www.tiktok.com/@fit.with.sam", 5600, 300, 3, "\uD83C\uDDFF\uD83C\uDDFC", "Fitness",
            trustLevel = 2, trustScore = 58, successfulExchanges = 3, disputes = 0, completionRate = 90,
            joinedDaysAgo = 18, activityScore = 48, category = "Lifestyle", lastActiveMinutesAgo = 40, profileCompleteness = 60),
        User("u5", "thriftwithtara", "Tara Thrift", "Affordable streetwear finds 🛍️", "https://www.tiktok.com/@thriftwithtara", 9800, 600, 4, "\uD83C\uDDF3\uD83C\uDDFF", "Fashion",
            trustLevel = 4, trustScore = 85, successfulExchanges = 27, disputes = 2, completionRate = 94,
            joinedDaysAgo = 260, activityScore = 71, category = "Fashion", activeBoost = BoostTier.BOOSTED, lastActiveMinutesAgo = 7, profileCompleteness = 85),
        User("u6", "chef.amara", "Chef Amara", "10-minute African recipes 🍲", "https://www.tiktok.com/@chef.amara", 15200, 800, 5, "\uD83C\uDDE8\uD83C\uDDFF", "Food",
            trustLevel = 3, trustScore = 77, successfulExchanges = 10, disputes = 0, completionRate = 95,
            joinedDaysAgo = 150, activityScore = 56, category = "Other", lastActiveMinutesAgo = 140, profileCompleteness = 70),
        User("u7", "wander.lens", "Lens Wander", "Hidden gems, budget travel ✈️", "https://www.tiktok.com/@wander.lens", 6400, 350, 6, "\uD83C\uDDFF\uD83C\uDDE6", "Travel",
            trustLevel = 1, trustScore = 41, successfulExchanges = 0, disputes = 0, completionRate = 100,
            joinedDaysAgo = 3, activityScore = 35, category = "Lifestyle", lastActiveMinutesAgo = 10, profileCompleteness = 45),
        User("u8", "zainab.skits", "Zainab", "Relatable skits from campus 🎭", "https://www.tiktok.com/@zainab.skits", 31000, 1500, 7, "\uD83C\uDDF0\uD83C\uDDEA", "Comedy",
            trustLevel = 5, trustScore = 93, successfulExchanges = 48, disputes = 1, completionRate = 98,
            joinedDaysAgo = 500, activityScore = 84, recommended = true,
            category = "Comedy", premium = PremiumTier.PREMIUM, lastActiveMinutesAgo = 5, profileCompleteness = 95),
        User("u9", "paint.bydana", "Dana Arts", "Digital art timelapses 🎨", "https://www.tiktok.com/@paint.bydana", 4300, 250, 0, "\uD83C\uDDF0\uD83C\uDDEA", "Art",
            trustLevel = 1, trustScore = 44, successfulExchanges = 0, disputes = 0, completionRate = 100,
            joinedDaysAgo = 9, activityScore = 40, category = "Other", lastActiveMinutesAgo = 300, profileCompleteness = 55),
        User("u10", "dancewithjoe", "Joe Dance", "Afro-dance tutorials, beginner friendly 💃", "https://www.tiktok.com/@dancewithjoe", 18900, 900, 1, "\uD83C\uDDFF\uD83C\uDDF8", "Dance",
            trustLevel = 4, trustScore = 87, successfulExchanges = 25, disputes = 0, completionRate = 96,
            joinedDaysAgo = 300, activityScore = 77, category = "Music", lastActiveMinutesAgo = 15, profileCompleteness = 85),
        User("u11", "money.with.mel", "Mel Money", "Side hustles & personal finance 💰", "https://www.tiktok.com/@money.with.mel", 11200, 700, 2, "\uD83C\uDDF0\uD83C\uDDEA", "Business",
            trustLevel = 3, trustScore = 76, successfulExchanges = 12, disputes = 1, completionRate = 92,
            joinedDaysAgo = 170, activityScore = 62, category = "Education", lastActiveMinutesAgo = 60, profileCompleteness = 80),
        User("u12", "paws.of.luna", "Luna's Human", "Adventures of Luna the husky 🐺", "https://www.tiktok.com/@paws.of.luna", 7600, 450, 3, "\uD83C\uDDEA\uD83C\uDDFA", "Pets",
            trustLevel = 2, trustScore = 64, successfulExchanges = 6, disputes = 0, completionRate = 94,
            joinedDaysAgo = 55, activityScore = 58, category = "Other", lastActiveMinutesAgo = 8, profileCompleteness = 65),
        // ── new: category coverage ──────────────────────────────────────
        User("u15", "coach.brian", "Coach Brian", "Football skills & drills ⚽", "https://www.tiktok.com/@coach.brian", 9200, 550, 4, "\uD83C\uDDE8\uD83C\uDDEA", "Football",
            trustLevel = 3, trustScore = 79, successfulExchanges = 13, disputes = 0, completionRate = 95,
            joinedDaysAgo = 95, activityScore = 68, category = "Football", lastActiveMinutesAgo = 22, profileCompleteness = 85),
        User("u16", "glow.with.naomi", "Naomi Glow", "Skincare routines & honest reviews 💅", "https://www.tiktok.com/@glow.with.naomi", 6800, 420, 6, "\uD83C\uDDF3\uD83C\uDDEFF", "Beauty",
            trustLevel = 2, trustScore = 63, successfulExchanges = 4, disputes = 0, completionRate = 92,
            joinedDaysAgo = 12, activityScore = 52, category = "Beauty", lastActiveMinutesAgo = 30, profileCompleteness = 70),
        User("u17", "miss.physics", "Miss Physics", "Science in 60 seconds 🔬", "https://www.tiktok.com/@miss.physics", 21500, 900, 5, "\uD83C\uDDE8\uD83C\uDDEA", "Education",
            trustLevel = 5, trustScore = 91, successfulExchanges = 40, disputes = 0, completionRate = 99,
            joinedDaysAgo = 350, activityScore = 81, recommended = true,
            category = "Education", premium = PremiumTier.PREMIUM, lastActiveMinutesAgo = 2, profileCompleteness = 100),
        User("u18", "kicks.kofi", "Kofi Kicks", "Football boots reviews & freestyle 🥾", "https://www.tiktok.com/@kicks.kofi", 4900, 300, 8, "\uD83C\uDDFC\uD83C\uDDEC", "Football",
            trustLevel = 1, trustScore = 43, successfulExchanges = 1, disputes = 0, completionRate = 100,
            joinedDaysAgo = 5, activityScore = 44, category = "Football", lastActiveMinutesAgo = 18, profileCompleteness = 50),
        // ── edge-case fixtures: extreme lengths + ligature-heavy text ──
        User(
            "u19", "alexandrina.michele.creates",
            "Alexandrina-Michele Nnamdinjijeochema Ogbonnayeluvc",
            bio2(),
            "https://www.tiktok.com/@alexandrina.michele.creates", 3300, 250, 8,
            "\uD83C\uDDE8\uD83C\uDDF2", "Fashion",
            trustLevel = 2, trustScore = 60, successfulExchanges = 4, disputes = 0,
            completionRate = 93, joinedDaysAgo = 20, activityScore = 50,
            category = "Fashion", premium = PremiumTier.PREMIUM,
            lastActiveMinutesAgo = 26, profileCompleteness = 70
        ),
        User("u20", "zj", "Zj", "in ij nj 'n — min jingle pins", "https://www.tiktok.com/@zj", 800, 100, 3,
            "\uD83C\uDDF5\uD83C\uDDF1", "Gaming",
            trustLevel = 1, trustScore = 42, successfulExchanges = 0, disputes = 0, completionRate = 100,
            joinedDaysAgo = 2, activityScore = 38, category = "Gaming", lastActiveMinutesAgo = 4, profileCompleteness = 40)
    )

    private fun bio2() = "Finding joy in joining niche jams 'n rocking every single one — invitations inbox always open, come on in! ✨"

    val filters = listOf("Recommended", "New", "Trusted", "Most Active")

    /** Filter → list, before ranking & search. */
    fun applyFilter(users: List<User>, filter: String): List<User> = when (filter) {
        "Recommended" -> users.filter { it.recommended || it.premium != PremiumTier.FREE || it.activeBoost != null }
        "New" -> users.filter { it.joinedDaysAgo <= 14 }.sortedBy { it.joinedDaysAgo }
        "Trusted" -> users.filter { it.trustLevel >= 4 }.sortedByDescending { it.trustScore }
        "Most Active" -> users.sortedBy { it.lastActiveMinutesAgo }
        else -> users
    }

    // ── onboarding steps ─────────────────────────────────────────────
    val onboardingSteps = listOf(
        Triple("🔍", "Discover creators", "Find people inside the TickTokBoost community."),
        Triple("🤝", "Complete exchanges", "Complete qualifying actions and wait for confirmation."),
        Triple("🛡️", "Build trust", "Successful activity improves your reputation."),
        Triple("🪙", "Use your credits", "Spend credits to improve your visibility within TickTokBoost.")
    )

    val earnTasks = listOf(
        EarnTask("t_checkin", "Daily Check-in", "Claim your free daily bonus", EconomyConfig.DAILY_CHECKIN_REWARD, "\uD83D\uDCC5"),
        EarnTask("t_follow3", "Follow 3 creators", "Follow 3 users in Discovery", 5, "\uD83E\uDD1D"),
        EarnTask("t_profile", "Complete your profile", "Add your bio and category", 5, "\u2728"),
        EarnTask("t_invite", "Invite a friend", "Share your invite link", 10, "\uD83C\uDF81"),
        EarnTask("t_share", "Share the app", "Share TickTokBoost", 3, "\uD83D\uDCE4")
    )

    const val DEMO_CONFIRM_DELAY = 8_000L
    const val DEMO_DISPUTE_RESOLVE_DELAY = 15_000L

    val disputeReasons = listOf(
        "They didn't follow me back",
        "Wrong or fake profile",
        "They unfollowed immediately",
        "Other"
    )

    // ── mock platform stats for the admin dashboard ─────────────────────
    object Admin {
        const val TOTAL_USERS = 12480
        const val ACTIVE_USERS = 3142
        const val PREMIUM_USERS = 861
        const val PRO_USERS = 214
        const val MOCK_EXCHANGES = 41260
        const val MOCK_CONFIRMED = 39902
        const val MOCK_DISPUTED = 418
    }
}
