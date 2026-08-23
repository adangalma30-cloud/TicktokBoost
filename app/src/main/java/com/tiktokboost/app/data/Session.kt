package com.tiktokboost.app.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Lightweight persistence layer for v0.0.1 backed by SharedPreferences.
 * Later versions can swap this for a real backend without touching the UI.
 */
object Session {

    private const val PREFS = "tiktokboost_prefs"
    private lateinit var sp: SharedPreferences

    fun init(context: Context) {
        sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    // ---- onboarding / auth -------------------------------------------------

    var isOnboarded: Boolean
        get() = sp.getBoolean("onboarded", false)
        set(value) { sp.edit().putBoolean("onboarded", value).apply() }

    var isLoggedIn: Boolean
        get() = sp.getBoolean("logged_in", false)
        set(value) { sp.edit().putBoolean("logged_in", value).apply() }

    var displayName: String
        get() = sp.getString("display_name", "") ?: ""
        set(value) { sp.edit().putString("display_name", value).apply() }

    var email: String
        get() = sp.getString("email", "") ?: ""
        set(value) { sp.edit().putString("email", value).apply() }

    var tiktokUsername: String
        get() = sp.getString("tiktok_username", "") ?: ""
        set(value) { sp.edit().putString("tiktok_username", value).apply() }

    // ---- coins --------------------------------------------------------------

    var coins: Int
        get() = sp.getInt("coins", 0)
        set(value) { sp.edit().putInt("coins", value).apply() }

    fun addCoins(n: Int) {
        coins = coins + n
    }

    fun spendCoins(n: Int): Boolean {
        if (coins < n) return false
        coins = coins - n
        return true
    }

    // ---- follow status: userId -> "followed" | "followed_back" --------------

    private fun followedJson(): JSONObject {
        val raw = sp.getString("follow_status", "{}") ?: "{}"
        return try {
            JSONObject(raw)
        } catch (e: Exception) {
            JSONObject()
        }
    }

    private fun saveFollowedJson(obj: JSONObject) {
        sp.edit().putString("follow_status", obj.toString()).apply()
    }

    fun setFollowStatus(userId: String, status: String) {
        val obj = followedJson()
        obj.put(userId, status)
        saveFollowedJson(obj)
    }

    fun followStatus(userId: String): String? {
        val s = followedJson().optString(userId, "")
        return s.ifEmpty { null }
    }

    fun followedCount(): Int {
        val obj = followedJson()
        var n = 0
        val keys = obj.keys()
        while (keys.hasNext()) {
            keys.next()
            n++
        }
        return n
    }

    fun returnedCount(): Int {
        val obj = followedJson()
        var n = 0
        val keys = obj.keys()
        while (keys.hasNext()) {
            if (obj.optString(keys.next()) == "followed_back") n++
        }
        return n
    }

    // ---- history -------------------------------------------------------------

    private fun historyJson(): JSONArray {
        val raw = sp.getString("history", "[]") ?: "[]"
        return try {
            JSONArray(raw)
        } catch (e: Exception) {
            JSONArray()
        }
    }

    fun addHistory(username: String, action: String, coinsEarned: Int) {
        val arr = historyJson()
        val obj = JSONObject()
        obj.put("id", System.currentTimeMillis().toString())
        obj.put("username", username)
        obj.put("action", action)
        obj.put("coins", coinsEarned)
        obj.put("ts", System.currentTimeMillis())
        arr.put(obj)
        sp.edit().putString("history", arr.toString()).apply()
    }

    fun history(): List<FollowEvent> {
        val arr = historyJson()
        val out = mutableListOf<FollowEvent>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            out.add(
                FollowEvent(
                    id = o.optString("id"),
                    username = o.optString("username"),
                    action = o.optString("action"),
                    coinsEarned = o.optInt("coins"),
                    timestamp = o.optLong("ts")
                )
            )
        }
        return out.sortedByDescending { it.timestamp }
    }

    // ---- daily check-in --------------------------------------------------------

    fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun canCheckIn(): Boolean = sp.getString("last_checkin", "") != today()

    fun doCheckIn() {
        sp.edit().putString("last_checkin", today()).apply()
    }

    // ---- one-time task claims --------------------------------------------------

    fun claimTask(id: String): Boolean {
        val set = (sp.getStringSet("claimed_tasks", mutableSetOf()) ?: mutableSetOf()).toMutableSet()
        if (id in set) return false
        set.add(id)
        sp.edit().putStringSet("claimed_tasks", set).apply()
        return true
    }

    fun isTaskClaimed(id: String): Boolean {
        val set = sp.getStringSet("claimed_tasks", mutableSetOf()) ?: mutableSetOf()
        return id in set
    }

    // ---- helpers -----------------------------------------------------------------

    fun clearUser() {
        sp.edit()
            .putBoolean("logged_in", false)
            .remove("display_name")
            .remove("email")
            .remove("tiktok_username")
            .apply()
    }

    fun resetAll() {
        sp.edit().clear().apply()
    }
}
