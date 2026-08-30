package com.aj.cardvault.security

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

/**
 * Encrypts/decrypts sensitive card fields (card number, CVV) using AES-256-GCM
 * with a key that never leaves the Android Keystore.
 *
 * Realistic scope: this protects data at rest on the device (e.g. if the database file
 * is extracted without the Keystore key). It does not protect against a compromised or
 * rooted device where the app's own process could be inspected while unlocked. This is
 * not "military grade" or "unbreakable" — it is standard, well-reviewed Android platform
 * encryption used appropriately for local sensitive data.
 */
object EncryptionManager {

    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val IV_LENGTH_BYTES = 12

    class DecryptionFailedException(cause: Throwable) : Exception("Decryption failed", cause)

    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, KeyManager.getOrCreateCardDataKey())
        val iv = cipher.iv
        val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Store IV + ciphertext together, Base64-encoded, in a single opaque string.
        val combined = ByteArray(iv.size + cipherBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherBytes, 0, combined, iv.size, cipherBytes.size)
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decrypt(encoded: String): String {
        try {
            val combined = Base64.decode(encoded, Base64.NO_WRAP)
            val iv = combined.copyOfRange(0, IV_LENGTH_BYTES)
            val cipherBytes = combined.copyOfRange(IV_LENGTH_BYTES, combined.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, KeyManager.getOrCreateCardDataKey(), spec)

            return String(cipher.doFinal(cipherBytes), Charsets.UTF_8)
        } catch (t: Throwable) {
            // Fail safely: never surface raw crypto exceptions or partial data.
            throw DecryptionFailedException(t)
        }
    }
}
