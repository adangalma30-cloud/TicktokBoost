package com.tiktokboost.app.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Persistence layer backed by SharedPreferences. All v0.0.1/v0.0.2 data is
 * preserved; v0.0.3 adds premium, boosts, streaks, achievements, disputes,
 * anti-abuse tracking and economy counters on top.
 */
object Session {

    private const val PREFS = "tiktokboost_prefs"
    private lateinit var sp: SharedPreferences

    fun init(context: Context) {
        sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        migrateV1()
        migrateV2toV3()
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

    // v0.0.3 profile fields (feed the profile-completeness score)
    var bio: String
        get() = sp.getString("bio", "") ?: ""
        set(value) { sp.edit().putString("bio", value).apply() }

    var category: String
        get() = sp.getString("category", "") ?: ""
        set(value) { sp.edit().putString("category", value).apply() }

    var accountCreated: Long
        get() = sp.getLong("account_created", 0L)
        set(value) { sp.edit().putLong("account_created", value).apply() }

    fun accountAgeDays(): Int {
        if (accountCreated == 0L) return 0
        return (((System.currentTimeMillis() - accountCreated) / 86_400_000L).toInt()).coerceAtLeast(0)
    }

    // ---- coins & economy counters ---------------------------------------------

    var coins: Int
        get() = sp.getInt("coins", 0)
        set(value) { sp.edit().putInt("coins", value.coerceAtLeast(0)).apply() }

    var lifetimeEarned: Int
        get() = sp.getInt("lifetime_earned", 0)
        set(value) { sp.edit().putInt("lifetime_earned", value.coerceAtLeast(0)).apply() }

    var lifetimeSpent: Int
        get() = sp.getInt("lifetime_spent", 0)
        set(value) { sp.edit().putInt("lifetime_spent", value.coerceAtLeast(0)).apply() }

    var dailyEarned: Int
        get() = sp.getInt("daily_earned", 0)
        set(value) { sp.edit().putInt("daily_earned", value).apply() }

    var dailyEarnDate: String?
        get() = sp.getString("daily_earned_date", null)
        set(value) { sp.edit().putString("daily_earned_date", value).apply() }

    fun addCoins(n: Int) { coins = coins + n }

    fun spendCoins(n: Int): Boolean {
        if (coins < n) return false
        coins = coins - n
        return true
    }

    @Deprecated("pending coins are derived from PENDING transactions in v0.0.3")
    var pendingCoins: Int
        get() = sp.getInt("pending_coins", 0)
        set(value) { sp.edit().putInt("pending_coins", value).apply() }

    fun addPendingCoins(n: Int) { pendingCoins = pendingCoins + n }

    fun releasePending(n: Int): Boolean {
        if (pendingCoins < n) return false
        pendingCoins = pendingCoins - n
        coins = coins + n
        return true
    }

    // ---- personal trust stats -------------------------------------------------

    var statExchanges: Int
        get() = sp.getInt("stat_exchanges", 0)
        set(value) { sp.edit().putInt("stat_exchanges", value).apply() }

    var statDisputes: Int
        get() = sp.getInt("stat_disputes", 0)
        set(value) { sp.edit().putInt("stat_disputes", value).apply() }

    var suspiciousFlags: Int
        get() = sp.getInt("suspicious_flags", 0)
        set(value) { sp.edit().putInt("suspicious_flags", value).apply() }

    // ---- premium (mock) ---------------------------------------------------------

    var premiumTier: String?
        get() = sp.getString("premium_tier", null)
        set(value) { sp.edit().putString("premium_tier", value).apply() }

    var premiumActivatedAt: Long?
        get() = if (sp.contains("premium_since")) sp.getLong("premium_since", 0) else null
        set(value) { if (value == null) sp.edit().remove("premium_since").apply() else sp.edit().putLong("premium_since", value).apply() }

    var premiumExpiresAt: Long?
        get() = if (sp.contains("premium_until")) sp.getLong("premium_until", 0) else null
        set(value) { if (value == null) sp.edit().remove("premium_until").apply() else sp.edit().putLong("premium_until", value).apply() }

    var premiumRenewalNotified: Boolean
        get() = sp.getBoolean("premium_renewal_notified", false)
        set(value) { sp.edit().putBoolean("premium_renewal_notified", value).apply() }

    // ---- boosts -------------------------------------------------------------------

    var activeBoostTier: String?
        get() = sp.getString("boost_tier", null)
        set(value) { sp.edit().putString("boost_tier", value).apply() }

    var activeBoostUntil: Long?
        get() = if (sp.contains("boost_until")) sp.getLong("boost_until", 0) else null
        set(value) { if (value == null) sp.edit().remove("boost_until").apply() else sp.edit().putLong("boost_until", value).apply() }

    fun setBoost(tier: String, until: Long) {
        activeBoostTier = tier
        activeBoostUntil = until
    }

    fun clearBoost() {
        activeBoostTier = null
        activeBoostUntil = null
    }

    // ---- cooldowns & pairing -------------------------------------------------------

    var cooldownUntil: Long?
        get() = if (sp.contains("cooldown_until")) sp.getLong("cooldown_until", 0) else null
        set(value) { if (value == null) sp.edit().remove("cooldown_until").apply() else sp.edit().putLong("cooldown_until", value).apply() }

    private fun partnerJson(): JSONObject =
        try { JSONObject(sp.getString("partner_history", "{}") ?: "{}") } catch (e: Exception) { JSONObject() }

    fun partnerHistory(userId: String): Long? {
        val v = partnerJson().optLong(userId, -1L)
        return if (v <= 0) null else v
    }

    fun recordPartner(userId: String) {
        val obj = partnerJson()
        obj.put(userId, System.currentTimeMillis())
        sp.edit().putString("partner_history", obj.toString()).apply()
    }

    // ---- anti-abuse --------------------------------------------------------------------

    var abuseStatus: String
        get() = sp.getString("abuse_status", AbuseStatus.NORMAL.name) ?: AbuseStatus.NORMAL.name
        set(value) { sp.edit().putString("abuse_status", value).apply() }

    var restrictedUntil: Long?
        get() = if (sp.contains("restricted_until")) sp.getLong("restricted_until", 0) else null
        set(value) { if (value == null) sp.edit().remove("restricted_until").apply() else sp.edit().putLong("restricted_until", value).apply() }

    private fun actionTs(): MutableList<Long> {
        val raw = sp.getString("action_ts", "[]") ?: "[]"
        val arr = try { JSONArray(raw) } catch (e: Exception) { JSONArray() }
        val out = mutableListOf<Long>()
        for (i in 0 until arr.length()) out.add(arr.optLong(i))
        return out
    }

    fun recordActionTimestamp() {
        val list = actionTs()
        list.add(System.currentTimeMillis())
        // keep only recent hour
        val hourAgo = System.currentTimeMillis() - 3_600_000L
        val arr = JSONArray()
        list.filter { it > hourAgo }.forEach { arr.put(it) }
        sp.edit().putString("action_ts", arr.toString()).apply()
    }

    fun recentActionCount(windowMs: Long): Int =
        actionTs().count { it > System.currentTimeMillis() - windowMs }

    fun clearAbuse() {
        abuseStatus = AbuseStatus.NORMAL.name
        restrictedUntil = null
        sp.edit().remove("action_ts").apply()
    }

    // ---- streaks ---------------------------------------------------------------------------

    var streakDays: Int
        get() = sp.getInt("streak_days", 0)
        set(value) { sp.edit().putInt("streak_days", value).apply() }

    var streakBest: Int
        get() = sp.getInt("streak_best", 0)
        set(value) { sp.edit().putInt("streak_best", value).apply() }

    var streakLastDay: String?
        get() = sp.getString("streak_last_day", null)
        set(value) { sp.edit().putString("streak_last_day", value).apply() }

    // ---- achievements ---------------------------------------------------------------------------

    fun unlockAchievement(id: String): Boolean {
        val set = (sp.getStringSet("achievements", mutableSetOf()) ?: mutableSetOf()).toMutableSet()
        if (id in set) return false
        set.add(id)
        sp.edit().putStringSet("achievements", set).apply()
        return true
    }

    fun isAchievementUnlocked(id: String): Boolean {
        val set = sp.getStringSet("achievements", mutableSetOf()) ?: mutableSetOf()
        return id in set
    }

    fun unlockedAchievements(): List<String> =
        (sp.getStringSet("achievements", mutableSetOf()) ?: mutableSetOf()).toList()

    // ---- follow status: userId -> "followed" | "followed_back" ----------------------------

    private fun followedJson(): JSONObject =
        try { JSONObject(sp.getString("follow_status", "{}") ?: "{}") } catch (e: Exception) { JSONObject() }

    fun setFollowStatus(userId: String, status: String) {
        val obj = followedJson()
        obj.put(userId, status)
        sp.edit().putString("follow_status", obj.toString()).apply()
    }

    fun followStatus(userId: String): String? {
        val s = followedJson().optString(userId, "")
        return s.ifEmpty { null }
    }

    fun followedCount(): Int = followedJson().length()

    fun returnedCount(): Int {
        val obj = followedJson()
        var n = 0
        val keys = obj.keys()
        while (keys.hasNext()) { if (obj.optString(keys.next()) == "followed_back") n++ }
        return n
    }

    // ---- transactions ---------------------------------------------------------------------------

    private fun txJson(): JSONArray =
        try { JSONArray(sp.getString("transactions", "[]") ?: "[]") } catch (e: Exception) { JSONArray() }

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
        o.put("id", t.id); o.put("uid", t.userId ?: ""); o.put("username", t.username)
        o.put("type", t.type.name); o.put("status", t.status.name); o.put("coins", t.coins)
        o.put("created", t.createdAt); o.put("updated", t.updatedAt); o.put("note", t.note)
        return o
    }

    fun addTransaction(tx: Transaction) {
        val arr = txJson()
        val o = txToJson(tx)
        // ensure id uniqueness even under same-millisecond bursts
        val taken = HashSet<String>(arr.length())
        for (i in 0 until arr.length()) {
            arr.optJSONObject(i)?.optString("id")?.let { taken.add(it) }
        }
        var id = tx.id
        var n = 2
        while (id in taken) { id = "${'$'}{tx.id}_$n"; n++ }
        o.put("id", id)
        arr.put(o)
        saveTx(arr)
    }

    fun updateTransaction(tx: Transaction) {
        val arr = txJson()
        for (i in 0 until arr.length()) {
            if (arr.optJSONObject(i)?.optString("id") == tx.id) {
                arr.put(i, txToJson(tx))
                saveTx(arr)
                return
            }
        }
    }

    private fun saveTx(arr: JSONArray) {
        sp.edit().putString("transactions", arr.toString()).apply()
    }

    fun transactions(): List<Transaction> {
        val arr = txJson()
        val out = mutableListOf<Transaction>()
        for (i in 0 until arr.length()) {
            txFromJson(arr.optJSONObject(i) ?: continue)?.let { out.add(it) }
        }
        val seen = HashSet<String>()
        return out.sortedByDescending { it.createdAt }.filter { seen.add(it.id) }
    }

    fun pendingTransactions(): List<Transaction> = transactions().filter { it.status == TxStatus.PENDING }

    // ---- disputes ---------------------------------------------------------------------------

    private fun disputeJson(): JSONArray =
        try { JSONArray(sp.getString("disputes", "[]") ?: "[]") } catch (e: Exception) { JSONArray() }

    fun addDispute(d: DisputeRecord) {
        val arr = disputeJson()
        val o = JSONObject()
        o.put("id", d.id); o.put("tx", d.txId); o.put("me", d.initiator)
        o.put("other", d.counterpart); o.put("created", d.createdAt)
        o.put("reason", d.reason); o.put("status", d.status.name)
        arr.put(o)
        sp.edit().putString("disputes", arr.toString()).apply()
    }

    fun updateDispute(d: DisputeRecord) {
        val arr = disputeJson()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            if (o.optString("id") == d.id) {
                o.put("status", d.status.name)
                arr.put(i, o)
                sp.edit().putString("disputes", arr.toString()).apply()
                return
            }
        }
    }

    fun disputes(): List<DisputeRecord> {
        val arr = disputeJson()
        val out = mutableListOf<DisputeRecord>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            out.add(
                DisputeRecord(
                    id = o.optString("id"),
                    txId = o.optString("tx"),
                    initiator = o.optString("me"),
                    counterpart = o.optString("other"),
                    createdAt = o.optLong("created"),
                    reason = o.optString("reason"),
                    status = runCatching { DisputeStatus.valueOf(o.optString("status")) }.getOrDefault(DisputeStatus.OPEN)
                )
            )
        }
        return out.sortedByDescending { it.createdAt }
    }

    // ---- notifications ---------------------------------------------------------------------------

    private fun notifJson(): JSONArray =
        try { JSONArray(sp.getString("notifications", "[]") ?: "[]") } catch (e: Exception) { JSONArray() }

    fun addNotification(kind: String, title: String, message: String, timestamp: Long = System.currentTimeMillis()) {
        if (!notificationsEnabled) return   // settings toggle — no new notifications when off
        val arr = notifJson()
        // collision-proof id: same-millisecond notifications would otherwise
        // produce duplicate LazyColumn keys and crash the Notifications screen
        var id = "n_$timestamp"
        val taken = HashSet<String>(arr.length())
        for (i in 0 until arr.length()) {
            arr.optJSONObject(i)?.optString("id")?.let { taken.add(it) }
        }
        var n = 2
        while (id in taken) { id = "n_${timestamp}_$n"; n++ }
        val o = JSONObject()
        o.put("id", id); o.put("kind", kind); o.put("title", title)
        o.put("message", message); o.put("ts", timestamp); o.put("read", false)
        arr.put(o)
        while (arr.length() > 50) arr.remove(0)
        sp.edit().putString("notifications", arr.toString()).apply()
    }

    fun notifications(): List<AppNotification> {
        val arr = notifJson()
        val out = mutableListOf<AppNotification>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            out.add(
                AppNotification(
                    id = o.optString("id"), kind = o.optString("kind", "system"),
                    title = o.optString("title"), message = o.optString("message"),
                    timestamp = o.optLong("ts"), read = o.optBoolean("read")
                )
            )
        }
        val seen = HashSet<String>()
        return out.sortedByDescending { it.timestamp }.filter { seen.add(it.id) }
    }

    fun unreadNotificationCount(): Int = notifications().count { !it.read }

    fun markAllNotificationsRead() {
        val arr = notifJson()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            o.put("read", true)
            arr.put(i, o)
        }
        sp.edit().putString("notifications", arr.toString()).apply()
    }

    // ---- discovery freshness ---------------------------------------------------------------------------

    private fun seenJson(): JSONObject =
        try { JSONObject(sp.getString("seen_history", "{}") ?: "{}") } catch (e: Exception) { JSONObject() }

    fun markSeen(userId: String) {
        val obj = seenJson()
        obj.put(userId, System.currentTimeMillis())
        // prune to 200 entries
        if (obj.length() > 200) {
            val keys = obj.keys().asSequence().toList()
            keys.take(obj.length() - 200).forEach { obj.remove(it) }
        }
        sp.edit().putString("seen_history", obj.toString()).apply()
    }

    fun lastSeen(userId: String): Long? {
        val v = seenJson().optLong(userId, -1L)
        return if (v <= 0) null else v
    }

    // ---- legacy history (v0.0.1 API kept working) ---------------------------------------------------------

    private fun historyJson(): JSONArray =
        try { JSONArray(sp.getString("history", "[]") ?: "[]") } catch (e: Exception) { JSONArray() }

    fun addHistory(username: String, action: String, coinsEarned: Int) {
        val arr = historyJson()
        val obj = JSONObject()
        obj.put("id", System.currentTimeMillis().toString())
        obj.put("username", username); obj.put("action", action)
        obj.put("coins", coinsEarned); obj.put("ts", System.currentTimeMillis())
        arr.put(obj)
        sp.edit().putString("history", arr.toString()).apply()
    }

    fun history(): List<FollowEvent> {
        val arr = historyJson()
        val out = mutableListOf<FollowEvent>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            out.add(FollowEvent(o.optString("id"), o.optString("username"), o.optString("action"), o.optInt("coins"), o.optLong("ts")))
        }
        return out.sortedByDescending { it.timestamp }
    }

    // ---- daily check-in ----------------------------------------------------------------------------------------

    fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun yesterday(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(System.currentTimeMillis() - 86_400_000L))

    /** Advances the streak if today is a new consecutive day; resets on gap. Returns new streak. */
    fun advanceStreak(): Int {
        val last = streakLastDay
        if (last == today()) return streakDays
        streakDays = if (last == yesterday()) streakDays + 1 else 1
        streakLastDay = today()
        if (streakDays > streakBest) streakBest = streakDays
        return streakDays
    }

    fun canCheckIn(): Boolean = sp.getString("last_checkin", "") != today()

    fun doCheckIn() {
        sp.edit().putString("last_checkin", today()).apply()
    }

    // ---- settings (v0.0.12) -----------------------------------------------------------------------------------------

    var themeMode: String   // "system" | "light" | "dark" — applied app-wide, persisted
        get() = sp.getString("theme_mode", "system") ?: "system"
        set(value) { sp.edit().putString("theme_mode", value).apply() }

    var notificationsEnabled: Boolean
        get() = sp.getBoolean("notifications_enabled", true)
        set(value) { sp.edit().putBoolean("notifications_enabled", value).apply() }

    var hapticsEnabled: Boolean
        get() = sp.getBoolean("haptics_enabled", true)
        set(value) { sp.edit().putBoolean("haptics_enabled", value).apply() }

    var language: String    // "system" | "en" | "sw" — structure ready for localization
        get() = sp.getString("language", "system") ?: "system"
        set(value) { sp.edit().putString("language", value).apply() }

    var discoverable: Boolean // privacy: appear in other users' discovery
        get() = sp.getBoolean("discoverable", true)
        set(value) { sp.edit().putBoolean("discoverable", value).apply() }

    // ---- creator content (v0.0.12): own uploaded photos / videos ------------------

    private fun contentJson(): org.json.JSONArray =
        try { org.json.JSONArray(sp.getString("content_items", "[]") ?: "[]") } catch (e: Exception) { org.json.JSONArray() }

    private fun saveContent(arr: org.json.JSONArray) {
        sp.edit().putString("content_items", arr.toString()).apply()
    }

    fun contentItems(): List<ContentItem> {
        val arr = contentJson()
        val out = mutableListOf<ContentItem>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            out.add(
                ContentItem(
                    id = o.optString("id"),
                    mediaType = runCatching { MediaType.valueOf(o.optString("type")) }.getOrDefault(MediaType.PHOTO),
                    path = o.optString("path").ifEmpty { null },
                    caption = o.optString("caption"),
                    createdAt = o.optLong("created"),
                    updatedAt = o.optLong("updated"),
                    demoVisual = o.optString("demo").ifEmpty { null }
                )
            )
        }
        return out.sortedByDescending { it.createdAt }
    }

    fun addContentItem(item: ContentItem) {
        val arr = contentJson()
        val o = org.json.JSONObject()
        o.put("id", item.id); o.put("type", item.mediaType.name); o.put("path", item.path ?: "")
        o.put("caption", item.caption); o.put("created", item.createdAt)
        o.put("updated", item.updatedAt); o.put("demo", item.demoVisual ?: "")
        arr.put(o)
        saveContent(arr)
    }

    fun updateContentItem(item: ContentItem) {
        val arr = contentJson()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            if (o.optString("id") == item.id) {
                o.put("caption", item.caption); o.put("updated", item.updatedAt)
                arr.put(i, o); saveContent(arr); return
            }
        }
    }

    fun deleteContentItem(id: String) {
        val arr = contentJson()
        for (i in 0 until arr.length()) {
            if (arr.optJSONObject(i)?.optString("id") == id) { arr.remove(i); saveContent(arr); return }
        }
    }

    // ---- profile picture (v0.0.11) --------------------------------------------------------------------------------

    var profilePicturePath: String?
        get() = sp.getString("profile_picture", null)
        set(value) { if (value == null) sp.edit().remove("profile_picture").apply() else sp.edit().putString("profile_picture", value).apply() }

    // ---- blocked creators (v0.0.11) -------------------------------------------------------------------------------

    private fun blockedSet(): MutableSet<String> =
        (sp.getStringSet("blocked_creators", mutableSetOf()) ?: mutableSetOf()).toMutableSet()

    fun isBlocked(userId: String): Boolean = userId in blockedSet()

    fun toggleBlock(userId: String): Boolean {
        val set = blockedSet()
        val nowBlocked = userId !in set
        if (nowBlocked) set.add(userId) else set.remove(userId)
        sp.edit().putStringSet("blocked_creators", set).apply()
        return nowBlocked
    }

    fun blockedIds(): List<String> = blockedSet().toList()

    // ---- creator reports (v0.0.11) ---------------------------------------------------------------------------------

    fun addReport(targetUsername: String, reason: String) {
        val arr = try { JSONArray(sp.getString("reports", "[]") ?: "[]") } catch (e: Exception) { JSONArray() }
        val o = JSONObject()
        val now = System.currentTimeMillis()
        o.put("id", "r_$now"); o.put("target", targetUsername)
        o.put("reason", reason); o.put("ts", now); o.put("resolved", false)
        arr.put(o)
        sp.edit().putString("reports", arr.toString()).apply()
    }

    fun reports(): List<com.tiktokboost.app.data.Report> {
        val arr = try { JSONArray(sp.getString("reports", "[]") ?: "[]") } catch (e: Exception) { JSONArray() }
        val out = mutableListOf<com.tiktokboost.app.data.Report>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            out.add(com.tiktokboost.app.data.Report(o.optString("id"), o.optString("target"), o.optString("reason"), o.optLong("ts"), o.optBoolean("resolved")))
        }
        return out.sortedByDescending { it.createdAt }
    }

    fun resolveReport(id: String) {
        val arr = try { JSONArray(sp.getString("reports", "[]") ?: "[]") } catch (e: Exception) { JSONArray() }
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            if (o.optString("id") == id) { o.put("resolved", true); arr.put(i, o) }
        }
        sp.edit().putString("reports", arr.toString()).apply()
    }

    // ---- confirmation reminders (v0.0.11) — one per transaction, never spam ------------------------------------------

    private fun remindedSet(): MutableSet<String> =
        (sp.getStringSet("reminded_tx", mutableSetOf()) ?: mutableSetOf()).toMutableSet()

    fun wasReminded(txId: String): Boolean = txId in remindedSet()

    fun markReminded(txId: String) {
        val set = remindedSet(); set.add(txId)
        sp.edit().putStringSet("reminded_tx", set).apply()
    }

    // ---- referral counters (v0.0.11 demo) ---------------------------------------------------------------------------

    var referralInvited: Int
        get() = sp.getInt("referral_invited", 0)
        set(value) { sp.edit().putInt("referral_invited", value).apply() }

    // ---- quests (v0.0.4) -----------------------------------------------------------------------------------------

    private const val QUESTS_KEY = "quest_states"

    fun questStatesJson(): String = sp.getString(QUESTS_KEY, "{}") ?: "{}"

    fun saveQuestStatesJson(raw: String) {
        sp.edit().putString(QUESTS_KEY, raw).apply()
    }

    var sharedAppOnce: Boolean
        get() = sp.getBoolean("shared_app_once", false)
        set(value) { sp.edit().putBoolean("shared_app_once", value).apply() }

    var referralQualified: Boolean
        get() = sp.getBoolean("referral_qualified", false)
        set(value) { sp.edit().putBoolean("referral_qualified", value).apply() }

    /** Stable demo referral code derived from the handle. */
    fun referralCode(): String {
        val h = tiktokUsername.ifBlank { "creator" }
        val suffix = ((h.hashCode() % 9000) + 9000) % 9000 + 1000
        return h.uppercase(Locale.US).take(4).padEnd(4, 'X') + suffix
    }

    fun referralLink(): String = "https://ticktokboost.app/r/${referralCode()}"

    // ---- one-time task claims -----------------------------------------------------------------------------------

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

    // ---- migrations & helpers --------------------------------------------------------------------------------------

    private fun migrateV1() {
        if (sp.getBoolean("v2_migrated", false)) return
        val legacy = history()
        legacy.forEach { ev ->
            val type = when {
                ev.action.contains("followed you back", true) -> TxType.FOLLOW_BACK
                ev.action.contains("followed", true) -> TxType.FOLLOW
                else -> TxType.BONUS
            }
            addTransaction(Transaction("m_${ev.id}", null, ev.username, type, TxStatus.COMPLETED, ev.coinsEarned, ev.timestamp, ev.timestamp, ev.action))
        }
        if (legacy.isNotEmpty()) {
            val follows = legacy.count { it.coinsEarned > 0 && it.action.contains("followed", true) }
            if (follows > statExchanges) statExchanges = follows
        }
        if (notifications().isEmpty()) {
            addNotification("system", "Welcome to TickTokBoost ✨", "Discover creators, complete exchanges, build trust.")
        }
        sp.edit().putBoolean("v2_migrated", true).apply()
    }

    /** v0.0.3: seed economy counters from existing data so lifetime stats look right. */
    private fun migrateV2toV3() {
        if (sp.getBoolean("v3_migrated", false)) return
        val txs = transactions()
        if (lifetimeEarned == 0) lifetimeEarned = txs.filter { it.coins > 0 }.sumOf { it.coins }
        if (lifetimeSpent == 0) lifetimeSpent = txs.filter { it.coins < 0 }.sumOf { -it.coins }
        if (accountCreated == 0L) accountCreated = txs.lastOrNull()?.createdAt ?: System.currentTimeMillis()
        sp.edit().putBoolean("v3_migrated", true).apply()
    }

    /** Admin/demo reset for testing economy rules without waiting real time. */
    fun resetEconomyGuards() {
        cooldownUntil = null
        sp.edit().remove("partner_history").apply()
        dailyEarned = 0
        dailyEarnDate = null
        clearAbuse()
        suspiciousFlags = 0
    }

    fun clearUser() {
        sp.edit()
            .putBoolean("logged_in", false)
            .remove("display_name").remove("email").remove("tiktok_username")
            .apply()
    }

    fun resetAll() {
        sp.edit().clear().apply()
    }
}
