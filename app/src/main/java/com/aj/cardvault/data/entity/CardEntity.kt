package com.aj.cardvault.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local card record.
 *
 * SECURITY NOTE:
 * - encryptedNumber / encryptedCvv hold AES-256-GCM ciphertext (Base64), never plaintext.
 * - lastFourDigits is stored in the clear ONLY for display/search ("Bank •••• 1234") and is
 *   not sufficient by itself to reconstruct the full card number.
 * - cardholderName is treated as sensitive display data; it is not encrypted at rest in this
 *   phase to keep search functional, but it is never logged. This may be revisited in a later
 *   phase if stronger protection is required.
 */
@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val type: CardType,
    val bank: String,
    val cardName: String,
    val cardholderName: String,

    // AES-256-GCM ciphertext, Base64-encoded (IV prefixed). Never plaintext.
    val encryptedNumber: String,
    val lastFourDigits: String,

    val expiryMonth: Int,
    val expiryYear: Int,

    // Null if the user chose not to store a CVV.
    val encryptedCvv: String?,

    val notes: String?,

    // Non-sensitive identifier associated via NFC (e.g. "CARD-0007"). Never the full PAN.
    val nfcIdentifier: String?,

    val createdAt: Long,
    val updatedAt: Long
)
