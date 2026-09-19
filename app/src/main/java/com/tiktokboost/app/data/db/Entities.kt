package com.tiktokboost.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ═══════════════════════════════════════════════════════════════════════════
 *  TickTokBoost database schema (v1.0.3 backend foundation).
 *
 *  Local-first authoritative store (Room/SQLite). The legacy SharedPreferences
 *  layer remains the live read path during migration; every mutation is
 *  mirrored here via DatabaseMirror so the DB is always consistent and ready
 *  to become the single source of truth when a cloud API is connected.
 * ═══════════════════════════════════════════════════════════════════════════
 */

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,                 // authentication identifier
    val passwordHash: String?,         // PBKDF2 hash; null = pre-backend legacy session
    val passwordSalt: String?,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val userId: String,
    val displayName: String,
    val tiktokUsername: String,
    val bio: String,
    val avatarPath: String?,           // local media reference
    val category: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "content")
data class ContentEntity(
    @PrimaryKey val id: String,
    val userId: String,                // ownership: only the owner may edit/delete
    val type: String,                  // PHOTO | VIDEO
    val mediaPath: String?,            // media reference (local file or future URL)
    val caption: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "boosts")
data class BoostEntity(
    @PrimaryKey val id: String,
    val creatorId: String,
    val title: String,
    val description: String,
    val reward: Int,
    val status: String,                // AVAILABLE | IN_PROGRESS | COMPLETED | EXPIRED
    val createdAt: Long,
    val expiresAt: Long?
)

@Entity(tableName = "boost_completions")
data class BoostCompletionEntity(
    @PrimaryKey val id: String,
    val boostId: String,
    val userId: String,
    val completedAt: Long,
    val rewardAmount: Int,
    val rewardTransactionId: String    // idempotent link into the ledger
)

@Entity(tableName = "point_transactions")
data class PointTransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val amount: Int,                   // signed: + earned / - spent
    val type: String,                  // FOLLOW | FOLLOW_BACK | BONUS | PURCHASE | BOOST | PREMIUM | STREAK | ACHIEVEMENT
    val reason: String,
    val createdAt: Long,
    val referenceId: String?           // economy reference (task/quest/referral id) for idempotency
)

@Entity(tableName = "daily_checkins")
data class DailyCheckInEntity(
    @PrimaryKey val userId: String,
    val lastCheckIn: Long,
    val currentStreak: Int,
    val longestStreak: Int,
    val updatedAt: Long
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val kind: String,
    val title: String,
    val message: String,
    val read: Boolean,
    val createdAt: Long
)
