package com.aj.cardvault.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aj.cardvault.data.entity.CardEntity
import com.aj.cardvault.viewmodel.CardViewModel

@Composable
fun CardDetailsScreen(
    card: CardEntity,
    cardViewModel: CardViewModel,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
    onAssociateNfc: () -> Unit
) {
    val revealed by cardViewModel.revealedCard.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showRevealed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(card.bank, style = MaterialTheme.typography.headlineSmall)
        Text(card.cardName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp))
        Text("Type: ${card.type}", modifier = Modifier.padding(top = 8.dp))
        Text("Cardholder: ${card.cardholderName}")
        Text("Card Number: •••• ${card.lastFourDigits}", modifier = Modifier.padding(top = 8.dp))
        Text("Expiry: ${card.expiryMonth.toString().padStart(2, '0')}/${card.expiryYear}")
        Text("CVV: " + if (card.encryptedCvv != null) "•••" else "Not stored")
        card.notes?.takeIf { it.isNotBlank() }?.let {
            Text("Notes: $it", modifier = Modifier.padding(top = 8.dp))
        }
        Text(
            "NFC association: " + (card.nfcIdentifier ?: "None"),
            modifier = Modifier.padding(top = 8.dp)
        )

        if (showRevealed && revealed != null) {
            val d = revealed!!
            Text(
                "Full number: ${d.cardNumber}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
            d.cvv?.let {
                Text("CVV: $it", style = MaterialTheme.typography.bodyLarge)
            }
        }

        Button(
            onClick = {
                if (showRevealed) {
                    showRevealed = false
                    cardViewModel.clearRevealed()
                } else {
                    cardViewModel.reveal(card)
                    showRevealed = true
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text(if (showRevealed) "Hide Sensitive Info" else "Reveal Sensitive Info")
        }

        OutlinedButton(onClick = onEdit, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Edit")
        }
        OutlinedButton(onClick = onAssociateNfc, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text(if (card.nfcIdentifier == null) "Associate NFC" else "Re-associate NFC")
        }
        if (card.nfcIdentifier != null) {
            TextButton(
                onClick = { cardViewModel.removeNfcAssociation(card) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Remove NFC Association")
            }
        }
        Button(
            onClick = { showDeleteConfirm = true },
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Delete Card")
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete this card?") },
            text = { Text("This cannot be undone. The card and its stored data will be permanently removed.") },
            confirmButton = {
                TextButton(onClick = {
                    cardViewModel.deleteCard(card)
                    showDeleteConfirm = false
                    onDeleted()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
