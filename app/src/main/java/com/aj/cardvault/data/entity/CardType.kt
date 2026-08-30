package com.aj.cardvault.data.entity

/**
 * Extensible card type. Add new constants here as needed;
 * Room stores the enum name as a String, so adding types is backward compatible.
 */
enum class CardType {
    CREDIT,
    DEBIT,
    OTHER
}
