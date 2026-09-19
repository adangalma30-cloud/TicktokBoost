package com.tiktokboost.app.data.auth

import android.content.Context
import android.content.SharedPreferences
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Authentication foundation (v1.0.3).
 *
 * Structure mirrors a real backend auth flow (register → verify → session)
 * against the local users table. Passwords are NEVER stored in plaintext —
 * PBKDF2WithHmacSHA256, 24k iterations, 16-byte salt. When a cloud backend is
 * connected, this service delegates to the remote API and keeps the same
 * interface; the UI does not change.
 */
object AuthService {

    private const val PREFS = "ticktokboost_auth"
    private const val ITERATIONS = 24_000
    private const val KEY_LEN = 256

    private lateinit var sp: SharedPreferences

    fun init(context: Context) {
        sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    // ── password hashing ────────────────────────────────────────────────────

    fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hash(password: String, saltHex: String): String {
        val salt = ByteArray(saltHex.length / 2) { i ->
            saltHex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LEN)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded.joinToString("") { "%02x".format(it) }
    }

    // ── session ─────────────────────────────────────────────────────────────

    var sessionUserId: String?
        get() = sp.getString("session_user", null)
        private set(value) { sp.edit().putString("session_user", value).apply() }

    val isAuthenticated: Boolean get() = sessionUserId != null

    data class AuthResult(val ok: Boolean, val error: String? = null)

    /**
     * Register a local account. Persists the hashed credential in the users
     * table via DatabaseMirror and starts a session.
     */
    fun register(userId: String, email: String, password: String): AuthResult {
        if (email.isBlank() || !email.contains("@")) return AuthResult(false, "Please enter a valid email address.")
        if (password.length < 6) return AuthResult(false, "Password must be at least 6 characters.")
        val salt = newSalt()
        val hashValue = hash(password, salt)
        com.tiktokboost.app.data.db.DatabaseMirror.user(email, hashValue, salt)
        sessionUserId = userId
        return AuthResult(true)
    }

    /**
     * Login. If an account exists for this email, the password is VERIFIED
     * against the stored hash. If no account exists yet (legacy/demo device),
     * one is created with the entered password — preserving the demo flow
     * while introducing real credential structure.
     */
    fun login(userId: String, email: String, password: String): AuthResult {
        val existing = com.tiktokboost.app.data.db.DatabaseMirror.readSync { db ->
            db.userDao().byEmail(email)
        }
        if (existing != null && existing.passwordHash != null && existing.passwordSalt != null) {
            val candidate = hash(password, existing.passwordSalt!!)
            if (candidate != existing.passwordHash) {
                return AuthResult(false, "Incorrect password for $email.")
            }
            sessionUserId = existing.id
            return AuthResult(true)
        }
        // no local account yet → create one (demo-compatible onboarding)
        return register(userId, email, password)
    }

    fun logout() {
        sessionUserId = null
    }
}
