package com.aj.cardvault.nfc

/**
 * NFC data is treated as untrusted input. This class validates and normalizes it into
 * a safe, bounded identifier string before it is ever compared against the database or
 * saved as an association.
 *
 * IMPORTANT LIMITATION (by design, not a bug):
 * Ordinary contactless bank cards do not expose their full payment credentials
 * (PAN, CVV, expiry, PIN) to a generic NFC read on Android. This app never assumes
 * otherwise. It only reads a raw tag ID / NDEF text payload and treats it purely as
 * an opaque identifier for local matching — never as a source of card credentials.
 */
object NfcIdentifier {

    private const val MAX_LENGTH = 64
    private val ALLOWED_CHARS = Regex("^[A-Za-z0-9\\-_]+$")

    sealed class ParseResult {
        data class Valid(val identifier: String) : ParseResult()
        object Unusable : ParseResult()
    }

    /**
     * @param rawTagId hex string of the tag's low-level ID (always present for any NFC tag)
     * @param ndefText optional text payload if the tag carries an NDEF text record
     */
    fun fromTagData(rawTagId: String?, ndefText: String?): ParseResult {
        val candidate = ndefText?.trim()?.takeIf { it.isNotEmpty() } ?: rawTagId?.trim()

        if (candidate.isNullOrEmpty()) {
            return ParseResult.Unusable
        }
        if (candidate.length > MAX_LENGTH) {
            return ParseResult.Unusable
        }
        if (!ALLOWED_CHARS.matches(candidate)) {
            return ParseResult.Unusable
        }

        return ParseResult.Valid(candidate.uppercase())
    }
}
