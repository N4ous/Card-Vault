package com.aj.cardvault

import com.aj.cardvault.nfc.NfcIdentifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NfcIdentifierTest {

    @Test
    fun `valid ndef text is used and normalized`() {
        val result = NfcIdentifier.fromTagData(rawTagId = "04A1B2C3", ndefText = "card-0007")
        assertTrue(result is NfcIdentifier.ParseResult.Valid)
        assertEquals("CARD-0007", (result as NfcIdentifier.ParseResult.Valid).identifier)
    }

    @Test
    fun `falls back to raw tag id when no ndef text`() {
        val result = NfcIdentifier.fromTagData(rawTagId = "04A1B2C3", ndefText = null)
        assertTrue(result is NfcIdentifier.ParseResult.Valid)
        assertEquals("04A1B2C3", (result as NfcIdentifier.ParseResult.Valid).identifier)
    }

    @Test
    fun `empty data is unusable`() {
        val result = NfcIdentifier.fromTagData(rawTagId = null, ndefText = null)
        assertTrue(result is NfcIdentifier.ParseResult.Unusable)
    }

    @Test
    fun `overly long identifier is unusable`() {
        val longText = "A".repeat(100)
        val result = NfcIdentifier.fromTagData(rawTagId = "04A1B2C3", ndefText = longText)
        assertTrue(result is NfcIdentifier.ParseResult.Unusable)
    }

    @Test
    fun `disallowed characters are unusable`() {
        val result = NfcIdentifier.fromTagData(rawTagId = "04A1B2C3", ndefText = "card 0007!!")
        assertTrue(result is NfcIdentifier.ParseResult.Unusable)
    }
}
