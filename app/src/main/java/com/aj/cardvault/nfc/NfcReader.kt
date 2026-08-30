package com.aj.cardvault.nfc

import android.nfc.NdefMessage
import android.nfc.Tag
import android.nfc.tech.Ndef

/**
 * Reads only non-sensitive, low-level NFC data: the tag's hardware ID and, if present,
 * a plain NDEF text record. Never attempts to read payment applets or EMV data — Android
 * does not expose those to third-party apps for ordinary contactless bank cards, and this
 * app does not pretend otherwise.
 */
object NfcReader {

    fun readRawTagId(tag: Tag): String {
        return tag.id.joinToString(separator = "") { byte -> "%02X".format(byte) }
    }

    fun readNdefText(tag: Tag): String? {
        val ndef = Ndef.get(tag) ?: return null
        return try {
            ndef.connect()
            val message: NdefMessage? = ndef.cachedNdefMessage ?: ndef.ndefMessage
            val record = message?.records?.firstOrNull() ?: return null
            parseTextRecordPayload(record.payload)
        } catch (_: Exception) {
            // Any read failure is treated as "no usable data" — never a crash, never a guess.
            null
        } finally {
            try {
                ndef.close()
            } catch (_: Exception) {
                // ignore
            }
        }
    }

    private fun parseTextRecordPayload(payload: ByteArray): String? {
        if (payload.isEmpty()) return null
        return try {
            val languageCodeLength = payload[0].toInt() and 0x3F
            val textBytes = payload.copyOfRange(1 + languageCodeLength, payload.size)
            String(textBytes, Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }
}
