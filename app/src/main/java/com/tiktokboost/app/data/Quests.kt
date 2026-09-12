package com.tiktokboost.app.data

import org.json.JSONObject
import com.tiktokboost.app.ui.AppState

/**
 * ═══════════════════════════════════════════════════════════════════════════
 *  Quest engine v0.0.4 — quests track REAL qualifying actions; rewards are
 *  only issued through the central economy service, exactly once, and only
 *  from the READY_TO_CLAIM state. Tapping/opening/restarting never grants.
 * ═══════════════════════════════════════════════════════════════════════════
 */

enum class QuestStatus { AVAILABLE, IN_PROGRESS, READY_TO_CLAIM, COMPLETED }

enum class QuestType { DAILY_CHECKIN, FOLLOW_CREATORS, COMPLETE_PROFILE, INVITE_FRIEND, SHARE_APP }

data class QuestDef(
    val id: String,
    val type: QuestType,
    val title: String,
    val description: String,
    val reward: Int,
    val requirement: Int,
    val action: String
)

data class QuestState(
    val questId: String,
    val progress: Int = 0,
    val status: QuestStatus = QuestStatus.AVAILABLE,
    val completedAt: Long? = null,
    val claimedAt: Long? = null
)

object Quests {

    const val PROFILE_THRESHOLD = 80   // % completion required
    const val FOLLOW_QUEST_REQUIREMENT = 3

    val defs = listOf(
        QuestDef("q_checkin", QuestType.DAILY_CHECKIN, "Daily Check-in",
            "Claim your daily bonus and continue your streak.", EconomyConfig.DAILY_CHECKIN_REWARD, 1, "Check in"),
        QuestDef("q_follow3", QuestType.FOLLOW_CREATORS, "Follow 3 creators",
            "Complete qualifying exchanges in Discover.", 5, FOLLOW_QUEST_REQUIREMENT, "Go to Discover"),
        QuestDef("q_profile", QuestType.COMPLETE_PROFILE, "Complete your profile",
            "Add your bio and category — reach $PROFILE_THRESHOLD% profile completion.", 5, PROFILE_THRESHOLD, "Edit profile"),
        QuestDef("q_share", QuestType.SHARE_APP, "Share the app",
            "Share TickTokBoost with your community.", 3, 1, "Share")
    )

    fun byId(id: String) = defs.firstOrNull { it.id == id }

    // ── persisted state ──────────────────────────────────────────────────

    private fun statesJson(): JSONObject {
        val raw = Session.questStatesJson()
        return try { JSONObject(raw) } catch (e: Exception) { JSONObject() }
    }

    fun stateOf(def: QuestDef): QuestState {
        val o = statesJson().optJSONObject(def.id)
        val saved = if (o != null) QuestState(
            questId = def.id,
            progress = o.optInt("progress"),
            status = runCatching { QuestStatus.valueOf(o.optString("status")) }.getOrDefault(QuestStatus.AVAILABLE),
            completedAt = if (o.has("completed")) o.optLong("completed") else null,
            claimedAt = if (o.has("claimed")) o.optLong("claimed") else null
        ) else QuestState(def.id)

        // live progress — always recomputed from real activity, never stale
        val live = progressOf(def)
        var status = saved.status

        // daily check-in resets every day
        if (def.type == QuestType.DAILY_CHECKIN && saved.claimedAt != null) {
            val claimedToday = Session.today() == java.text.SimpleDateFormat(
                "yyyy-MM-dd", java.util.Locale.US
            ).format(java.util.Date(saved.claimedAt!!))
            if (!claimedToday) status = QuestStatus.AVAILABLE
        }

        if (status != QuestStatus.COMPLETED) {
            status = when {
                live >= def.requirement -> QuestStatus.READY_TO_CLAIM
                live > 0 -> QuestStatus.IN_PROGRESS
                else -> QuestStatus.AVAILABLE
            }
        }
        return saved.copy(progress = live, status = status)
    }

    fun all(): List<Pair<QuestDef, QuestState>> = defs.map { it to stateOf(it) }

    /** Real qualifying progress for each quest — never inflated by taps. */
    private fun progressOf(def: QuestDef): Int = when (def.type) {
        QuestType.DAILY_CHECKIN -> if (Session.canCheckIn()) 0 else 1
        QuestType.FOLLOW_CREATORS -> minOf(Session.followedCount(), def.requirement)
        QuestType.COMPLETE_PROFILE -> AppState.computeProfileCompleteness()
        QuestType.SHARE_APP -> if (Session.sharedAppOnce) 1 else 0
        else -> 0
    }

    private fun save(state: QuestState) {
        val obj = statesJson()
        val o = JSONObject()
        o.put("progress", state.progress)
        o.put("status", state.status.name)
        state.completedAt?.let { o.put("completed", it) }
        state.claimedAt?.let { o.put("claimed", it) }
        obj.put(state.questId, o)
        Session.saveQuestStatesJson(obj.toString())
    }

    // ── qualifiers (called only on REAL events) ──────────────────────────

    /** Called when the native share sheet actually opened for the app-share quest. */
    fun onAppShared() {
        Session.sharedAppOnce = true
    }

    // ── claiming (the ONLY path to rewards) ──────────────────────────────

    /**
     * Claim a READY_TO_CLAIM quest. Returns Failure for anything else —
     * there is no way to claim an unearned quest, and no way to claim twice.
     */
    fun claim(def: QuestDef): EconomyResult {
        val st = stateOf(def)
        if (st.claimedAt != null && st.status == QuestStatus.COMPLETED) {
            return EconomyResult.Failure(FailureReason.DUPLICATE, "Already claimed.")
        }
        if (st.status != QuestStatus.READY_TO_CLAIM) {
            return EconomyResult.Failure(FailureReason.INVALID_STATE, "Quest not complete yet.")
        }
        val now = System.currentTimeMillis()
        val res: EconomyResult = if (def.type == QuestType.DAILY_CHECKIN) {
            // check-in claims through the check-in flow (streak + transaction)
            val r = AppState.checkIn()
            when (r) {
                is EconomyResult.Success -> EconomyResult.Success(r.coins)
                is EconomyResult.Failure -> return r
            }
        } else {
            AppState.economyGrant("Quest: ${def.title}", def.reward)
        }
        if (res is EconomyResult.Failure) return res
        save(QuestState(def.id, st.progress, QuestStatus.COMPLETED, now, now))
        Session.addNotification(
            "achievement", "Quest completed: ${def.title}",
            "+${def.reward} coins added to your balance."
        )
        return EconomyResult.Success(def.reward, "Quest completed · +${def.reward} coins")
    }
}
