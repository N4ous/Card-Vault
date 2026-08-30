package com.aj.cardvault.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aj.cardvault.data.entity.CardEntity
import com.aj.cardvault.data.repository.DecryptedCard
import com.aj.cardvault.viewmodel.CardViewModel

@Composable
fun EditCardScreen(
    card: CardEntity,
    cardViewModel: CardViewModel,
    onSaved: () -> Unit
) {
    LaunchedEffect(card.id) { cardViewModel.reveal(card) }
    val revealed by cardViewModel.revealedCard.collectAsState()
    val error by cardViewModel.operationError.collectAsState()

    val d = revealed
    if (d == null) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }
        return
    }

    var bank by remember { mutableStateOf(d.bank) }
    var cardName by remember { mutableStateOf(d.cardName) }
    var cardholderName by remember { mutableStateOf(d.cardholderName) }
    var cardNumber by remember { mutableStateOf(d.cardNumber) }
    var expiryMonth by remember { mutableStateOf(d.expiryMonth.toString()) }
    var expiryYear by remember { mutableStateOf(d.expiryYear.toString()) }
    var cvv by remember { mutableStateOf(d.cvv ?: "") }
    var notes by remember { mutableStateOf(d.notes ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Edit Card", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(bank, { bank = it }, label = { Text("Bank / Issuer") }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp))
        OutlinedTextField(cardName, { cardName = it }, label = { Text("Card Name") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(cardholderName, { cardholderName = it }, label = { Text("Cardholder Name") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(
            value = cardNumber,
            onValueChange = { if (it.length <= 19) cardNumber = it.filter(Char::isDigit) },
            label = { Text("Card Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        Row(modifier = Modifier.padding(top = 8.dp)) {
            OutlinedTextField(
                value = expiryMonth,
                onValueChange = { if (it.length <= 2) expiryMonth = it.filter(Char::isDigit) },
                label = { Text("Exp. Month") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = expiryYear,
                onValueChange = { if (it.length <= 4) expiryYear = it.filter(Char::isDigit) },
                label = { Text("Exp. Year") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
        }
        OutlinedTextField(
            value = cvv,
            onValueChange = { if (it.length <= 4) cvv = it.filter(Char::isDigit) },
            label = { Text("CVV (optional)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        OutlinedTextField(notes, { notes = it }, label = { Text("Notes (optional)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        Button(
            onClick = {
                val updated = DecryptedCard(
                    id = d.id,
                    type = d.type,
                    bank = bank,
                    cardName = cardName,
                    cardholderName = cardholderName,
                    cardNumber = cardNumber,
                    lastFourDigits = cardNumber.takeLast(4),
                    expiryMonth = expiryMonth.toIntOrNull() ?: 0,
                    expiryYear = expiryYear.toIntOrNull() ?: 0,
                    cvv = cvv.ifBlank { null },
                    notes = notes.ifBlank { null },
                    nfcIdentifier = d.nfcIdentifier
                )
                cardViewModel.updateCard(card, updated) { success ->
                    if (success) {
                        cardViewModel.clearRevealed()
                        onSaved()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 32.dp)
        ) {
            Text("Save Changes")
        }
    }
}
