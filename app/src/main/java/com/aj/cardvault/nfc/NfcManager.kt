package com.aj.cardvault.nfc

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import androidx.activity.ComponentActivity

sealed class NfcScanResult {
    data class Identified(val identifier: String) : NfcScanResult()
    object Unusable : NfcScanResult()
    object NfcNotAvailable : NfcScanResult()
}

/**
 * Facade over the Android NFC APIs used by the UI/ViewModel layer.
 * Keeps NFC plumbing out of Composables per the app's architecture rules.
 */
class NfcManager(private val activity: ComponentActivity) {

    private val adapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)

    fun isNfcSupported(): Boolean = adapter != null

    fun isNfcEnabled(): Boolean = adapter?.isEnabled == true

    fun enableForegroundDispatch() {
        val adapter = adapter ?: return
        val intent = Intent(activity, activity.javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            0
        }
        val pendingIntent = PendingIntent.getActivity(activity, 0, intent, flags)
        val filters = arrayOf(IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED))
        adapter.enableForegroundDispatch(activity, pendingIntent, filters, null)
    }

    fun disableForegroundDispatch() {
        adapter?.disableForegroundDispatch(activity)
    }

    fun handleIntent(intent: Intent): NfcScanResult {
        if (adapter == null) return NfcScanResult.NfcNotAvailable

        val tag: Tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        } ?: return NfcScanResult.Unusable

        val rawId = NfcReader.readRawTagId(tag)
        val ndefText = NfcReader.readNdefText(tag)

        return when (val result = NfcIdentifier.fromTagData(rawId, ndefText)) {
            is NfcIdentifier.ParseResult.Valid -> NfcScanResult.Identified(result.identifier)
            NfcIdentifier.ParseResult.Unusable -> NfcScanResult.Unusable
        }
    }
}
