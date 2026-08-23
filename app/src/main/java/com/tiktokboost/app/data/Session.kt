package com.tiktokboost.app.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Lightweight persistence layer backed by SharedPreferences.
 * v0.0.1 data (coins, follows, history) is preserved and transparently
 * migrated into the richer v0.0.2 transaction / notification models.
 */
object Session {

    private const val PREFS = "tiktokboost_prefs"
    private lateinit var sp: SharedPreferences

    fun init(context: Context) {
        sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        migrateV1()
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

    // ---- coins ---------------------------------------------------------------

    var coins: Int
        get() = sp.getInt("coins", 0)
        set(value) { sp.edit().putInt("coins", value).apply() }

    /** Coins earned but not yet released (waiting for counterpart confirmation). */
    var pendingCoins: Int
        get() = sp.getInt("pending_coins", 0)
        set(value) { sp.edit().putInt("pending_coins", value).apply() }

    fun addCoins(n: Int) { coins = coins + n }

    fun addPendingCoins(n: Int) { pendingCoins = pendingCoins + n }

    /** Move coins from pending to available. Returns false if not enough pending. */
    fun releasePending(n: Int): Boolean {
        if (pendingCoins < n) return false
        pendingCoins = pendingCoins - n
        coins = coins + n
        return true
    }

    fun spendCoins(n: Int): Boolean {
        if (coins < n) return false
        coins = coins - n
        return true
    }

    // ---- personal trust stats -------------------------------------------------

    var statExchanges: Int
        get() = sp.getInt("stat_exchanges", 0)
        set(value) { sp.edit().putInt("stat_exchanges", value).apply() }

    var statDisputes: Int
        get() = sp.getInt("stat_disputes", 0)
        set(value) { sp.edit().putInt("stat_disputes", value).apply() }

    // ---- follow status: userId -> "followed" | "followed_back" -----------------

    private fun followedJson(): JSONObject {
        val raw = sp.getString("follow_status", "{}") ?: "{}"
        return try { JSONObject(raw) } catch (e: Exception) { JSONObject() }
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
        while (keys.hasNext()) { keys.next(); n++ }
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

    // ---- transactions (v0.0.2) --------------------------------------------------

    private fun txJson(): JSONArray {
        val raw = sp.getString("transactions", "[]") ?: "[]"
        return try { JSONArray(raw) } catch (e: Exception) { JSONArray() }
    }

    private fun saveTxJson(arr: JSONArray) {
        sp.edit().putString("transactions", arr.toString()).apply()
    }

    private fun txFromJson(o: JSONObject): Transaction? = try {
        Transaction(
            id = o.optString("id"),
            userId = o.optString("uid").ifEmpty { null },
            username = o.optString("username"),
            type = runCatching { TxType.valueOf(o.optString("type")) }.getOrDefault(TxType.BONUS),
            status = runCatching { TxStatus.valueOf(o.optString("status")) }.getOrDefault(TxStatus.COMPLETED),
            coins = o.optInt("coins"),
            createdAt = o.optLong("created"),
            updatedAt = o.optLong("updated"),
            note = o.optString("note")
        )
    } catch (e: Exception) { null }

    private fun txToJson(t: Transaction): JSONObject {
        val o = JSONObject()
        o.put("id", t.id)
        o.put("uid", t.userId ?: "")
        o.put("username", t.username)
        o.put("type", t.type.name)
        o.put("status", t.status.name)
        o.put("coins", t.coins)
        o.put("created", t.createdAt)
        o.put("updated", t.updatedAt)
        o.put("note", t.note)
        return o
    }

    fun addTransaction(tx: Transaction) {
        val arr = txJson()
        arr.put(txToJson(tx))
        saveTxJson(arr)
    }

    fun updateTransaction(tx: Transaction) {
        val arr = txJson()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            if (o.optString("id") == tx.id) {
                arr.put(i, txToJson(tx))
                saveTxJson(arr)
                return
            }
        }
    }

    fun transactions(): List<Transaction> {
        val arr = txJson()
        val out = mutableListOf<Transaction>()
        for (i in 0 until arr.length()) {
            txFromJson(arr.optJSONObject(i) ?: continue)?.let { out.add(it) }
        }
        return out.sortedByDescending { it.createdAt }
    }

    fun pendingTransactions(): List<Transaction> =
        transactions().filter { it.status == TxStatus.PENDING }

    // ---- notifications (v0.0.2) ---------------------------------------------------

    private fun notifJson(): JSONArray {
        val raw = sp.getString("notifications", "[]") ?: "[]"
        return try { JSONArray(raw) } catch (e: Exception) { JSONArray() }
    }

    private fun saveNotifJson(arr: JSONArray) {
        sp.edit().putString("notifications", arr.toString()).apply()
    }

    fun addNotification(kind: String, title: String, message: String, timestamp: Long = System.currentTimeMillis()) {
        val arr = notifJson()
        val o = JSONObject()
        o.put("id", "n_$timestamp")
        o.put("kind", kind)
        o.put("title", title)
        o.put("message", message)
        o.put("ts", timestamp)
        o.put("read", false)
        arr.put(o)
        // keep the list bounded
        while (arr.length() > 40) arr.remove(0)
        saveNotifJson(arr)
    }

    fun notifications(): List<AppNotification> {
        val arr = notifJson()
        val out = mutableListOf<AppNotification>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            out.add(
                AppNotification(
                    id = o.optString("id"),
                    kind = o.optString("kind", "system"),
                    title = o.optString("title"),
                    message = o.optString("message"),
                    timestamp = o.optLong("ts"),
                    read = o.optBoolean("read")
                )
            )
        }
        return out.sortedByDescending { it.timestamp }
    }

    fun unreadNotificationCount(): Int = notifications().count { !it.read }

    fun markAllNotificationsRead() {
        val arr = notifJson()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            o.put("read", true)
            arr.put(i, o)
        }
        saveNotifJson(arr)
    }

    // ---- legacy history (v0.0.1 API kept working) ------------------------------------

    // ---- history (legacy JSON kept in sync) ------------------------------------
    private fun historyJson(): JSONArray {
        val raw = sp.getString("history", "[]") ?: "[]"
        return try { JSONArray(raw) } catch (e: Exception) { JSONArray() }
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

    // ---- daily check-in -----------------------------------------------------------------

    fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun canCheckIn(): Boolean = sp.getString("last_checkin", "") != today()

    fun doCheckIn() {
        sp.edit().putString("last_checkin", today()).apply()
    }

    // ---- one-time task claims ---------------------------------------------------------

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

    // ---- migration & helpers -------------------------------------------------------------

    /**
     * One-time migration: converts v0.0.1 history entries into v0.0.2
     * transactions (all completed — those coins were already granted) and
     * counts verified follows as successful exchanges so trust survives.
     */
    private fun migrateV1() {
        if (sp.getBoolean("v2_migrated", false)) return
        val legacy = history()
        val now = System.currentTimeMillis()
        legacy.forEachIndexed { i, ev ->
            val type = when {
                ev.action.contains("followed you back", ignoreCase = true) -> TxType.FOLLOW_BACK
                ev.action.contains("followed", ignoreCase = true) -> TxType.FOLLOW
                ev.action.contains("bonus", ignoreCase = true) -> TxType.BONUS
                else -> TxType.BONUS
            }
            addTransaction(
                Transaction(
                    id = "m_${ev.id}",
                    username = ev.username,
                    type = type,
                    status = TxStatus.COMPLETED,
                    coins = ev.coinsEarned,
                    createdAt = ev.timestamp,
                    updatedAt = ev.timestamp,
                    note = ev.action
                )
            )
        }
        // legacy follows that were completed = successful exchanges
        if (legacy.isNotEmpty()) {
            val follows = legacy.count { it.coinsEarned > 0 && it.action.contains("followed", ignoreCase = true) }
            if (follows > statExchanges) statExchanges = follows
        }
        if (notifications().isEmpty()) {
            addNotification(
                "system", "Welcome to TikTokBoost v0.0.2 ✨",
                "Trust levels, pending coins and notifications are new — check your profile to see your trust badge.",
                now
            )
        }
        sp.edit().putBoolean("v2_migrated", true).apply()
    }

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
