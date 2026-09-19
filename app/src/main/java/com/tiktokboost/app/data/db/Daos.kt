package com.tiktokboost.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun byEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun byId(id: String): UserEntity?

    @Upsert
    fun upsert(user: UserEntity)

    @Query("SELECT COUNT(*) FROM users")
    fun count(): Int
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles WHERE userId = :userId LIMIT 1")
    fun byUser(userId: String): ProfileEntity?

    @Upsert
    fun upsert(profile: ProfileEntity)
}

@Dao
interface ContentDao {
    @Query("SELECT * FROM content WHERE userId = :userId ORDER BY createdAt DESC")
    fun byUser(userId: String): List<ContentEntity>

    @Upsert
    fun upsert(item: ContentEntity)

    @Query("DELETE FROM content WHERE id = :id AND userId = :userId")
    fun deleteOwned(id: String, userId: String): Int   // ownership enforced in SQL

    @Query("SELECT COUNT(*) FROM content")
    fun count(): Int
}

@Dao
interface BoostDao {
    @Upsert
    fun upsert(boost: BoostEntity)

    @Query("SELECT * FROM boosts ORDER BY createdAt DESC")
    fun all(): List<BoostEntity>
}

@Dao
interface BoostCompletionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(completion: BoostCompletionEntity)

    @Query("SELECT * FROM boost_completions WHERE boostId = :boostId AND userId = :userId LIMIT 1")
    fun find(boostId: String, userId: String): BoostCompletionEntity?
}

@Dao
interface PointTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(tx: PointTransactionEntity)

    @Query("SELECT * FROM point_transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun byUser(userId: String): List<PointTransactionEntity>

    @Query("SELECT * FROM point_transactions WHERE referenceId = :referenceId LIMIT 1")
    fun byReference(referenceId: String): PointTransactionEntity?

    @Query("SELECT COALESCE(SUM(amount), 0) FROM point_transactions WHERE userId = :userId")
    fun balance(userId: String): Int   // derived — balances are never stored raw
}

@Dao
interface DailyCheckInDao {
    @Upsert
    fun upsert(checkIn: DailyCheckInEntity)

    @Query("SELECT * FROM daily_checkins WHERE userId = :userId LIMIT 1")
    fun byUser(userId: String): DailyCheckInEntity?
}

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(n: NotificationEntity)

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun byUser(userId: String): List<NotificationEntity>
}
