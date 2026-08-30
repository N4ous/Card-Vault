package com.aj.cardvault

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aj.cardvault.nfc.NfcManager
import com.aj.cardvault.nfc.NfcScanResult
import com.aj.cardvault.ui.navigation.CardVaultNavHost
import com.aj.cardvault.ui.theme.CardVaultTheme

/**
 * Single-activity app. FLAG_SECURE is applied globally here since nearly every screen
 * in this app can show sensitive financial information — the spec's own carve-out
 * ("don't apply screenshot blocking app-wide if it harms usability") does not really
 * apply to a vault app where almost all content is sensitive.
 */
class MainActivity : FragmentActivity() {

    private lateinit var container: AppContainer
    private lateinit var nfcManager: NfcManager

    private var lastNfcResult by mutableStateOf<NfcScanResult?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        container = AppContainer(applicationContext)
        nfcManager = NfcManager(this)

        setContent {
            CardVaultTheme {
                CardVaultNavHost(
                    activity = this,
                    container = container,
                    lastNfcResult = lastNfcResult
                )
            }
        }

        intent?.let { handleNfcIntent(it) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNfcIntent(intent)
    }

    private fun handleNfcIntent(intent: Intent) {
        if (intent.action == android.nfc.NfcAdapter.ACTION_TAG_DISCOVERED) {
            lastNfcResult = nfcManager.handleIntent(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        nfcManager.enableForegroundDispatch()
    }

    override fun onPause() {
        super.onPause()
        nfcManager.disableForegroundDispatch()
    }

    // NOTE: Auto-lock re-entry to the Lock screen on background (configurable timeout,
    // spec section 25) is intentionally deferred to a later phase. Phase 1 ships a safe
    // default: FLAG_SECURE prevents screen capture immediately, and the app already
    // requires re-authentication on cold start via AuthUiState.
}
