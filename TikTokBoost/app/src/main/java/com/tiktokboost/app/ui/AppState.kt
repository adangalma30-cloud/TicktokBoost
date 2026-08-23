package com.tiktokboost.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tiktokboost.app.data.MockData
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.data.User

/**
 * Compose-observable snapshot of the session. Screens read from this and it is
 * refreshed after every mutation so the UI stays in sync.
 */
object AppState {

    var coins by mutableIntStateOf(0)
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

    fun refresh() {
        coins = Session.coins
        followedCount = Session.followedCount()
        returnedCount = Session.returnedCount()
        loggedIn = Session.isLoggedIn
        displayName = Session.displayName
        email = Session.email
        tiktokUsername = Session.tiktokUsername
    }

    fun followStatus(userId: String): String? = Session.followStatus(userId)

    fun confirmFollow(user: User): Int {
        Session.setFollowStatus(user.id, "followed")
        Session.addCoins(MockData.REWARD_FOLLOW)
        Session.addHistory(user.username, "You followed @${user.username}", MockData.REWARD_FOLLOW)
        refresh()
        return MockData.REWARD_FOLLOW
    }

    fun confirmFollowBack(user: User): Int {
        Session.setFollowStatus(user.id, "followed_back")
        Session.addCoins(MockData.REWARD_FOLLOW_BACK)
        Session.addHistory(user.username, "@${user.username} followed you back", MockData.REWARD_FOLLOW_BACK)
        refresh()
        return MockData.REWARD_FOLLOW_BACK
    }

    fun checkIn(): Int {
        if (!Session.canCheckIn()) return 0
        Session.doCheckIn()
        Session.addCoins(2)
        Session.addHistory("checkin", "Daily check-in bonus", 2)
        refresh()
        return 2
    }

    fun canCheckIn(): Boolean = Session.canCheckIn()

    fun spendCoins(n: Int): Boolean {
        val ok = Session.spendCoins(n)
        refresh()
        return ok
    }

    fun claimTask(id: String, reward: Int): Boolean {
        if (!Session.claimTask(id)) return false
        Session.addCoins(reward)
        refresh()
        return true
    }

    fun isTaskClaimed(id: String): Boolean = Session.isTaskClaimed(id)
}
