package com.aj.cardvault

import android.content.Context
import com.aj.cardvault.data.database.CardVaultDatabase
import com.aj.cardvault.data.repository.CardRepository
import com.aj.cardvault.security.AuthManager

/**
 * Minimal manual dependency container. Deliberately avoids pulling in a DI framework
 * (e.g. Hilt) at this stage to keep the project simple for a beginner/intermediate
 * maintainer, per the project's stated architecture goals.
 */
class AppContainer(context: Context) {
    val authManager: AuthManager = AuthManager(context)
    val cardRepository: CardRepository by lazy {
        CardRepository(CardVaultDatabase.getInstance(context).cardDao())
    }
}
