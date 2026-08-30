package com.aj.cardvault.data.repository

import com.aj.cardvault.data.dao.CardDao
import com.aj.cardvault.data.entity.CardEntity
import com.aj.cardvault.data.entity.CardType
import com.aj.cardvault.security.EncryptionManager
import kotlinx.coroutines.flow.Flow

/**
 * Plain (decrypted) representation used only transiently in memory once the user has
 * authenticated and explicitly requested to view sensitive data. Never persisted.
 */
data class DecryptedCard(
    val id: Long,
    val type: CardType,
    val bank: String,
    val cardName: String,
    val cardholderName: String,
    val cardNumber: String,
    val lastFourDigits: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val cvv: String?,
    val notes: String?,
    val nfcIdentifier: String?
)

class CardRepository(private val dao: CardDao) {

    fun getAllCards(): Flow<List<CardEntity>> = dao.getAllCards()

    fun search(query: String): Flow<List<CardEntity>> = dao.search(query)

    suspend fun getCardEntity(id: Long): CardEntity? = dao.getCardById(id)

    suspend fun findByNfcIdentifier(identifier: String): CardEntity? =
        dao.getCardByNfcIdentifier(identifier)

    /**
     * Validates and saves a new card. Sensitive fields are encrypted before ever
     * reaching the database.
     */
    suspend fun addCard(
        type: CardType,
        bank: String,
        cardName: String,
        cardholderName: String,
        cardNumber: String,
        expiryMonth: Int,
        expiryYear: Int,
        cvv: String?,
        notes: String?
    ): Result<Long> {
        val validation = CardValidator.validate(
            bank = bank,
            cardName = cardName,
            cardholderName = cardholderName,
            cardNumber = cardNumber,
            expiryMonth = expiryMonth,
            expiryYear = expiryYear,
            cvv = cvv
        )
        if (validation is CardValidator.ValidationResult.Invalid) {
            return Result.failure(IllegalArgumentException(validation.reason))
        }

        val now = System.currentTimeMillis()
        val entity = CardEntity(
            type = type,
            bank = bank.trim(),
            cardName = cardName.trim(),
            cardholderName = cardholderName.trim(),
            encryptedNumber = EncryptionManager.encrypt(cardNumber.filter { it.isDigit() }),
            lastFourDigits = cardNumber.filter { it.isDigit() }.takeLast(4),
            expiryMonth = expiryMonth,
            expiryYear = expiryYear,
            encryptedCvv = cvv?.takeIf { it.isNotBlank() }?.let { EncryptionManager.encrypt(it) },
            notes = notes?.trim(),
            nfcIdentifier = null,
            createdAt = now,
            updatedAt = now
        )
        val id = dao.insert(entity)
        return Result.success(id)
    }

    suspend fun updateCard(existing: CardEntity, updated: DecryptedCard): Result<Unit> {
        val validation = CardValidator.validate(
            bank = updated.bank,
            cardName = updated.cardName,
            cardholderName = updated.cardholderName,
            cardNumber = updated.cardNumber,
            expiryMonth = updated.expiryMonth,
            expiryYear = updated.expiryYear,
            cvv = updated.cvv
        )
        if (validation is CardValidator.ValidationResult.Invalid) {
            return Result.failure(IllegalArgumentException(validation.reason))
        }

        val digits = updated.cardNumber.filter { it.isDigit() }
        val entity = existing.copy(
            type = updated.type,
            bank = updated.bank.trim(),
            cardName = updated.cardName.trim(),
            cardholderName = updated.cardholderName.trim(),
            encryptedNumber = EncryptionManager.encrypt(digits),
            lastFourDigits = digits.takeLast(4),
            expiryMonth = updated.expiryMonth,
            expiryYear = updated.expiryYear,
            encryptedCvv = updated.cvv?.takeIf { it.isNotBlank() }?.let { EncryptionManager.encrypt(it) },
            notes = updated.notes?.trim(),
            updatedAt = System.currentTimeMillis()
        )
        dao.update(entity)
        return Result.success(Unit)
    }

    suspend fun deleteCard(entity: CardEntity) = dao.delete(entity)

    suspend fun clearAllCards() = dao.deleteAll()

    suspend fun associateNfc(entity: CardEntity, identifier: String) {
        dao.update(entity.copy(nfcIdentifier = identifier, updatedAt = System.currentTimeMillis()))
    }

    suspend fun removeNfcAssociation(entity: CardEntity) {
        dao.update(entity.copy(nfcIdentifier = null, updatedAt = System.currentTimeMillis()))
    }

    /**
     * Decrypts a card's sensitive fields. Callers must only invoke this after the user
     * has authenticated and explicitly asked to reveal the data.
     */
    fun reveal(entity: CardEntity): DecryptedCard {
        return DecryptedCard(
            id = entity.id,
            type = entity.type,
            bank = entity.bank,
            cardName = entity.cardName,
            cardholderName = entity.cardholderName,
            cardNumber = EncryptionManager.decrypt(entity.encryptedNumber),
            lastFourDigits = entity.lastFourDigits,
            expiryMonth = entity.expiryMonth,
            expiryYear = entity.expiryYear,
            cvv = entity.encryptedCvv?.let { EncryptionManager.decrypt(it) },
            notes = entity.notes,
            nfcIdentifier = entity.nfcIdentifier
        )
    }
}
