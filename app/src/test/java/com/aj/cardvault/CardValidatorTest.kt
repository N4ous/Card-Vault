package com.aj.cardvault

import com.aj.cardvault.data.repository.CardValidator
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class CardValidatorTest {

    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    @Test
    fun `valid card passes`() {
        val result = CardValidator.validate(
            bank = "ABC Bank",
            cardName = "Personal Visa",
            cardholderName = "AJ Shahariar",
            cardNumber = "4111111111111111",
            expiryMonth = 12,
            expiryYear = currentYear + 2,
            cvv = "123"
        )
        assertTrue(result is CardValidator.ValidationResult.Valid)
    }

    @Test
    fun `blank bank fails`() {
        val result = CardValidator.validate(
            bank = "",
            cardName = "Card",
            cardholderName = "AJ",
            cardNumber = "4111111111111111",
            expiryMonth = 12,
            expiryYear = currentYear + 1,
            cvv = null
        )
        assertTrue(result is CardValidator.ValidationResult.Invalid)
    }

    @Test
    fun `card number too short fails`() {
        val result = CardValidator.validate(
            bank = "Bank",
            cardName = "Card",
            cardholderName = "AJ",
            cardNumber = "1234",
            expiryMonth = 12,
            expiryYear = currentYear + 1,
            cvv = null
        )
        assertTrue(result is CardValidator.ValidationResult.Invalid)
    }

    @Test
    fun `invalid expiry month fails`() {
        val result = CardValidator.validate(
            bank = "Bank",
            cardName = "Card",
            cardholderName = "AJ",
            cardNumber = "4111111111111111",
            expiryMonth = 13,
            expiryYear = currentYear + 1,
            cvv = null
        )
        assertTrue(result is CardValidator.ValidationResult.Invalid)
    }

    @Test
    fun `expired card fails`() {
        val result = CardValidator.validate(
            bank = "Bank",
            cardName = "Card",
            cardholderName = "AJ",
            cardNumber = "4111111111111111",
            expiryMonth = 1,
            expiryYear = currentYear - 1,
            cvv = null
        )
        assertTrue(result is CardValidator.ValidationResult.Invalid)
    }

    @Test
    fun `non-numeric cvv fails`() {
        val result = CardValidator.validate(
            bank = "Bank",
            cardName = "Card",
            cardholderName = "AJ",
            cardNumber = "4111111111111111",
            expiryMonth = 12,
            expiryYear = currentYear + 1,
            cvv = "12a"
        )
        assertTrue(result is CardValidator.ValidationResult.Invalid)
    }

    @Test
    fun `missing cvv is allowed`() {
        val result = CardValidator.validate(
            bank = "Bank",
            cardName = "Card",
            cardholderName = "AJ",
            cardNumber = "4111111111111111",
            expiryMonth = 12,
            expiryYear = currentYear + 1,
            cvv = null
        )
        assertTrue(result is CardValidator.ValidationResult.Valid)
    }
}
