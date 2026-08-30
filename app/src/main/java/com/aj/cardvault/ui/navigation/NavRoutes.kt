package com.aj.cardvault.ui.navigation

object NavRoutes {
    const val SPLASH = "splash"
    const val SETUP = "setup"
    const val LOCK = "lock"
    const val DASHBOARD = "dashboard"
    const val CARD_LIST = "card_list"
    const val ADD_CARD = "add_card"
    const val CARD_DETAILS = "card_details/{cardId}"
    const val EDIT_CARD = "edit_card/{cardId}"
    const val NFC_SCAN = "nfc_scan"
    const val SETTINGS = "settings"

    fun cardDetails(id: Long) = "card_details/$id"
    fun editCard(id: Long) = "edit_card/$id"
}
