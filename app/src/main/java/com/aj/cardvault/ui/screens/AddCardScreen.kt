package com.aj.cardvault.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aj.cardvault.data.entity.CardType
import com.aj.cardvault.viewmodel.CardViewModel

@Composable
fun AddCardScreen(
    cardViewModel: CardViewModel,
    onSaved: () -> Unit
) {
    var type by remember { mutableStateOf(CardType.CREDIT) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var bank by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf("") }
    var cardholderName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiryMonth by remember { mutableStateOf("") }
    var expiryYear by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val error by cardViewModel.operationError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Add Card", style = MaterialTheme.typography.headlineSmall)

        ExposedDropdownMenuBox(
            expanded = typeMenuExpanded,
            onExpandedChange = { typeMenuExpanded = it },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            OutlinedTextField(
                value = type.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Card Type") },
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                CardType.values().forEach { option ->
                    DropdownMenuItem(text = { Text(option.name) }, onClick = {
                        type = option
                        typeMenuExpanded = false
                    })
                }
            }
        }

        OutlinedTextField(bank, { bank = it }, label = { Text("Bank / Issuer") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
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
                cardViewModel.addCard(
                    type = type,
                    bank = bank,
                    cardName = cardName,
                    cardholderName = cardholderName,
                    cardNumber = cardNumber,
                    expiryMonth = expiryMonth.toIntOrNull() ?: 0,
                    expiryYear = expiryYear.toIntOrNull() ?: 0,
                    cvv = cvv.ifBlank { null },
                    notes = notes.ifBlank { null }
                ) { success -> if (success) onSaved() }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 32.dp)
        ) {
            Text("Save Card")
        }
    }
}
