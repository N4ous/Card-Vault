package com.aj.cardvault.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aj.cardvault.data.entity.CardType
import com.aj.cardvault.viewmodel.CardViewModel

@Composable
fun DashboardScreen(
    cardViewModel: CardViewModel,
    onAddCard: () -> Unit,
    onViewCards: () -> Unit,
    onScanNfc: () -> Unit,
    onSettings: () -> Unit
) {
    val cards by cardViewModel.cards.collectAsState()
    val creditCount = cards.count { it.type == CardType.CREDIT }
    val debitCount = cards.count { it.type == CardType.DEBIT }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Card Vault", style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Total Cards: ${cards.size}", style = MaterialTheme.typography.titleMedium)
                Text("Credit Cards: $creditCount", modifier = Modifier.padding(top = 4.dp))
                Text("Debit Cards: $debitCount")
            }
        }

        Button(onClick = onAddCard, modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
            Text("+ Add Card")
        }
        OutlinedButton(onClick = onScanNfc, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Text("Scan / Identify NFC")
        }
        OutlinedButton(onClick = onViewCards, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Text("View Cards")
        }
        OutlinedButton(onClick = onSettings, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Text("Settings")
        }

        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
            Text(
                "Offline vault — no account, no internet required.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
