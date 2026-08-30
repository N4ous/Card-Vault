package com.aj.cardvault.data.repository

import java.util.Calendar

/**
 * Validation rules for card data. Pure Kotlin (no Android framework dependency) so it
 * can be unit tested directly on the JVM.
 */
object CardValidator {

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }

    fun validate(
        bank: String,
        cardName: String,
        cardholderName: String,
        cardNumber: String,
        expiryMonth: Int,
        expiryYear: Int,
        cvv: String?
    ): ValidationResult {
        if (bank.isBlank()) return ValidationResult.Invalid("Bank/issuer is required.")
        if (cardName.isBlank()) return ValidationResult.Invalid("Card name is required.")
        if (cardholderName.isBlank()) return ValidationResult.Invalid("Cardholder name is required.")

        val digits = cardNumber.filter { it.isDigit() }
        if (digits.length !in 12..19) {
            return ValidationResult.Invalid("Card number must be between 12 and 19 digits.")
        }

        if (expiryMonth !in 1..12) {
            return ValidationResult.Invalid("Expiry month must be between 1 and 12.")
        }

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        if (expiryYear < currentYear || expiryYear > currentYear + 30) {
            return ValidationResult.Invalid("Expiry year is out of a reasonable range.")
        }
        if (expiryYear == currentYear) {
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
            if (expiryMonth < currentMonth) {
                return ValidationResult.Invalid("This card has already expired.")
            }
        }

        if (!cvv.isNullOrBlank()) {
            if (cvv.any { !it.isDigit() } || cvv.length !in 3..4) {
                return ValidationResult.Invalid("CVV must be 3 or 4 digits.")
            }
        }

        return ValidationResult.Valid
    }
}
