package com.aj.cardvault.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Handles PIN setup/verification and basic brute-force rate limiting.
 *
 * The PIN itself is NEVER stored. Only a salted PBKDF2 hash is stored, inside
 * EncryptedSharedPreferences (itself backed by a Keystore master key).
 */
class AuthManager(context: Context) {

    private val appContext = context.applicationContext

    private val masterKey = MasterKey.Builder(appContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        appContext,
        "cardvault_auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_LOCKOUT_UNTIL = "lockout_until"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"

        private const val PBKDF2_ITERATIONS = 120_000
        private const val KEY_LENGTH_BITS = 256

        private const val MAX_ATTEMPTS_BEFORE_LOCKOUT = 5
        private const val LOCKOUT_DURATION_MS = 30_000L
    }

    fun isPinConfigured(): Boolean = prefs.contains(KEY_PIN_HASH)

    fun setPin(pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = hashPin(pin, salt)
        prefs.edit()
            .putString(KEY_PIN_SALT, android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP))
            .putString(KEY_PIN_HASH, android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP))
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL, 0L)
            .apply()
    }

    /**
     * Result of a PIN check. Never exposes whether the account "exists" or
     * any detail beyond success/failure/lockout.
     */
    sealed class PinResult {
        object Success : PinResult()
        object IncorrectPin : PinResult()
        data class LockedOut(val remainingMillis: Long) : PinResult()
    }

    fun verifyPin(pin: String): PinResult {
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        val now = System.currentTimeMillis()
        if (now < lockoutUntil) {
            return PinResult.LockedOut(lockoutUntil - now)
        }

        val storedSalt = prefs.getString(KEY_PIN_SALT, null)
        val storedHash = prefs.getString(KEY_PIN_HASH, null)
        if (storedSalt == null || storedHash == null) {
            // No PIN configured yet — treat as generic failure, never a helpful hint.
            return PinResult.IncorrectPin
        }

        val salt = android.util.Base64.decode(storedSalt, android.util.Base64.NO_WRAP)
        val expectedHash = android.util.Base64.decode(storedHash, android.util.Base64.NO_WRAP)
        val actualHash = hashPin(pin, salt)

        return if (actualHash.contentEquals(expectedHash)) {
            prefs.edit().putInt(KEY_FAILED_ATTEMPTS, 0).putLong(KEY_LOCKOUT_UNTIL, 0L).apply()
            PinResult.Success
        } else {
            val attempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
            val editor = prefs.edit().putInt(KEY_FAILED_ATTEMPTS, attempts)
            if (attempts >= MAX_ATTEMPTS_BEFORE_LOCKOUT) {
                editor.putLong(KEY_LOCKOUT_UNTIL, now + LOCKOUT_DURATION_MS)
                editor.putInt(KEY_FAILED_ATTEMPTS, 0)
            }
            editor.apply()
            PinResult.IncorrectPin
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)

    /** Wipes PIN/biometric configuration, used only by the explicit "Clear All Data" flow. */
    fun resetAuthConfiguration() {
        prefs.edit().clear().apply()
    }

    private fun hashPin(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }
}
