package com.aj.cardvault.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aj.cardvault.nfc.NfcScanResult

@Composable
fun NfcScanScreen(
    lastScanResult: NfcScanResult?,
    onFoundMatch: (String) -> Unit,
    onNoMatchAssociatePrompt: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Scan / Identify NFC", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Hold a previously associated card or tag near the back of your phone.",
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            style = MaterialTheme.typography.bodyMedium
        )

        when (lastScanResult) {
            null -> Text("Waiting for a tag…", style = MaterialTheme.typography.bodyMedium)
            NfcScanResult.NfcNotAvailable -> Text(
                "NFC is not available on this device.",
                color = MaterialTheme.colorScheme.error
            )
            NfcScanResult.Unusable -> Text(
                "NFC data could not be used to identify this card.",
                color = MaterialTheme.colorScheme.error
            )
            is NfcScanResult.Identified -> {
                Text("Identifier detected: ${lastScanResult.identifier}")
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
        ) {
            Text("Back")
        }
    }
}
