package com.tiktokboost.app.data.db

import android.content.Context
import com.tiktokboost.app.data.Session
import java.util.concurrent.Executors

/**
 * DatabaseMirror — the write-through bridge between the existing application
 * services (EconomyService / Session / AppState) and the Room database.
 *
 * Every authoritative mutation (ledger transactions, notifications, content,
 * check-ins, profile, boost tasks) is mirrored into the DB off the main
 * thread. The DB is therefore always consistent with app state and ready to
 * become the single source of truth (or an offline cache) once a cloud API is
 * connected behind the same calls.
 *
 * All calls are safe no-ops before init() (e.g. in unit tests that don't use
 * the DB).
 */
object DatabaseMirror {

    @Volatile private var db: AppDatabase? = null
    private val io = Executors.newSingleThreadExecutor()

    fun init(context: Context) {
        if (db != null) return
        db = AppDatabase.get(context)
        seedIfNeeded()
    }

    /** Test hook: detach the current DB (allows re-init between tests). */
    fun resetForTest() { db = null }

    private fun exec(block: () -> Unit) {
        io.execute {
            try {
                block()
            } catch (t: Throwable) {
                android.util.Log.e("DatabaseMirror", "mirror write failed", t)
                t.printStackTrace()
            }
        }
    }

    fun userId(): String = Session.userDbId

    private fun seedIfNeeded() {
        val database = db ?: return
        exec {
            if (Session.dbSeeded) return@exec
            val uid = Session.userDbId
            val now = System.currentTimeMillis()
            // user + profile rows for the current (possibly legacy) profile
            if (database.userDao().byId(uid) == null) {
                database.userDao().upsert(
                    UserEntity(uid, Session.email.ifBlank { "local@device" }, null, null, now, now)
                )
            }
            database.profileDao().upsert(
                ProfileEntity(
                    uid, Session.displayName, Session.tiktokUsername, Session.bio,
                    Session.profilePicturePath, Session.category, now, now
                )
            )
            // migrate existing content
            Session.contentItems().forEach { c ->
                database.contentDao().upsert(
                    ContentEntity(c.id, uid, c.mediaType.name, c.path, c.caption, c.createdAt, c.updatedAt)
                )
            }
            // migrate existing ledger
            Session.transactions().forEach { t ->
                database.pointTransactionDao().insert(
                    PointTransactionEntity(t.id, uid, t.coins, t.type.name, t.note, t.createdAt, null)
                )
            }
            // migrate check-in state
            database.dailyCheckInDao().upsert(
                DailyCheckInEntity(uid, 0L, Session.streakDays, Session.streakBest, now)
            )
            Session.dbSeeded = true
        }
    }

    // ── write-through APIs (called by the application services) ─────────────

    fun ledger(txId: String, amount: Int, type: String, reason: String, createdAt: Long, referenceId: String?) {
        val database = db ?: return
        exec {
            database.pointTransactionDao().insert(
                PointTransactionEntity(txId, userId(), amount, type, reason, createdAt, referenceId)
            )
        }
    }

    fun notification(id: String, kind: String, title: String, message: String, read: Boolean, createdAt: Long) {
        val database = db ?: return
        exec {
            database.notificationDao().insert(
                NotificationEntity(id, userId(), kind, title, message, read, createdAt)
            )
        }
    }

    fun content(item: com.tiktokboost.app.data.ContentItem) {
        val database = db ?: return
        exec {
            database.contentDao().upsert(
                ContentEntity(item.id, userId(), item.mediaType.name, item.path, item.caption, item.createdAt, item.updatedAt)
            )
        }
    }

    fun contentDeleted(id: String) {
        val database = db ?: return
        exec { database.contentDao().deleteOwned(id, userId()) }
    }

    fun checkIn(lastCheckIn: Long, streak: Int, best: Int) {
        val database = db ?: return
        exec {
            database.dailyCheckInDao().upsert(
                DailyCheckInEntity(userId(), lastCheckIn, streak, best, System.currentTimeMillis())
            )
        }
    }

    fun profile(displayName: String, tiktok: String, bio: String, avatarPath: String?, category: String) {
        val database = db ?: return
        exec {
            val existing = database.profileDao().byUser(userId())
            val now = System.currentTimeMillis()
            database.profileDao().upsert(
                ProfileEntity(
                    userId(), displayName, tiktok, bio, avatarPath, category,
                    existing?.createdAt ?: now, now
                )
            )
        }
    }

    fun boostTask(t: com.tiktokboost.app.data.BoostTask) {
        val database = db ?: return
        exec {
            database.boostDao().upsert(
                BoostEntity(
                    t.id, userId(), t.title, t.description, t.reward,
                    t.status.name, t.startedAt ?: System.currentTimeMillis(), t.expiresAt
                )
            )
            if (t.status == com.tiktokboost.app.data.BoostTaskStatus.COMPLETED && t.rewardTransactionId != null) {
                database.boostCompletionDao().insert(
                    BoostCompletionEntity(
                        "bc_${t.id}", t.id, userId(),
                        t.completedAt ?: System.currentTimeMillis(), t.reward, t.rewardTransactionId
                    )
                )
            }
        }
    }

    fun user(email: String, passwordHash: String?, passwordSalt: String?) {
        val database = db ?: return
        exec {
            val now = System.currentTimeMillis()
            val existing = database.userDao().byId(userId())
            database.userDao().upsert(
                UserEntity(userId(), email, passwordHash ?: existing?.passwordHash, passwordSalt ?: existing?.passwordSalt, existing?.createdAt ?: now, now)
            )
        }
    }

    /**
     * Synchronous read on the DB thread (Room forbids main-thread queries).
     * Small, indexed lookups only. Returns null when the DB isn't initialized.
     */
    fun <T> readSync(block: (AppDatabase) -> T): T? {
        val database = db ?: return null
        return io.submit<T> { block(database) }.get(5, java.util.concurrent.TimeUnit.SECONDS)
    }

    /** Blocks until all queued mirror writes have landed (used by tests). */
    fun awaitWrites() {
        io.submit { }.get(5, java.util.concurrent.TimeUnit.SECONDS)
    }

    /** Synchronous read access for tests / future repository readers. */
    fun read(): AppDatabase? = db
}
