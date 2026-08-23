package com.tiktokboost.app.data

/**
 * Demo data for v0.0.1 so the complete UI and follow-exchange workflow can
 * be built and tested before any external services are connected.
 * All handles are fictional.
 */
object MockData {

    val users = listOf(
        User("u1", "kevin.creates", "Kevin Creates", "Daily tech & editing tips 🔥", "https://www.tiktok.com/@kevin.creates", 12400, 500, 0, "\uD83C\uDDF0\uD83C\uDDEA", "Tech"),
        User("u2", "maya.gaming", "Maya", "Mobile gaming, live streams, glhf ⚡", "https://www.tiktok.com/@maya.gaming", 8700, 400, 1, "\uD83C\uDDFA\uD83C\uDDF8", "Gaming"),
        User("u3", "beats.by.nyash", "Nyash Beats", "Afrobeats producer, drops every Friday 🎧", "https://www.tiktok.com/@beats.by.nyash", 20300, 1000, 2, "\uD83C\uDDF0\uD83C\uDDF0", "Music"),
        User("u4", "fit.with.sam", "Sam Fit", "Home workouts + meal prep, no gym needed 💪", "https://www.tiktok.com/@fit.with.sam", 5600, 300, 3, "\uD83C\uDDFF\uD83C\uDDFC", "Fitness"),
        User("u5", "thriftwithtara", "Tara Thrift", "Affordable streetwear finds 🛍️", "https://www.tiktok.com/@thriftwithtara", 9800, 600, 4, "\uD83C\uDDF3\uD83C\uDDFF", "Fashion"),
        User("u6", "chef.amara", "Chef Amara", "10-minute African recipes 🍲", "https://www.tiktok.com/@chef.amara", 15200, 800, 5, "\uD83C\uDDE8\uD83C\uDDFF", "Food"),
        User("u7", "wander.lens", "Lens Wander", "Hidden gems, budget travel ✈️", "https://www.tiktok.com/@wander.lens", 6400, 350, 6, "\uD83C\uDDFF\uD83C\uDDE6", "Travel"),
        User("u8", "zainab.skits", "Zainab", "Relatable skits from campus 🎭", "https://www.tiktok.com/@zainab.skits", 31000, 1500, 7, "\uD83C\uDDF0\uD83C\uDDEA", "Comedy"),
        User("u9", "paint.bydana", "Dana Arts", "Digital art timelapses 🎨", "https://www.tiktok.com/@paint.bydana", 4300, 250, 0, "\uD83C\uDDF0\uD83C\uDDEA", "Art"),
        User("u10", "dancewithjoe", "Joe Dance", "Afro-dance tutorials, beginner friendly 💃", "https://www.tiktok.com/@dancewithjoe", 18900, 900, 1, "\uD83C\uDDFF\uD83C\uDDE8", "Dance"),
        User("u11", "money.with.mel", "Mel Money", "Side hustles & personal finance 💰", "https://www.tiktok.com/@money.with.mel", 11200, 700, 2, "\uD83C\uDDF0\uD83C\uDDEA", "Business"),
        User("u12", "paws.of.luna", "Luna's Human", "Adventures of Luna the husky 🐺", "https://www.tiktok.com/@paws.of.luna", 7600, 450, 3, "\uD83C\uDDEA\uD83C\uDDFA", "Pets")
    )

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
}
