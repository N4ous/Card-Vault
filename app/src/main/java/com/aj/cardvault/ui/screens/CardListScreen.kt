package com.aj.cardvault.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import com.aj.cardvault.data.entity.CardEntity
import com.aj.cardvault.viewmodel.CardViewModel

@Composable
fun CardListScreen(
    cardViewModel: CardViewModel,
    onCardClick: (CardEntity) -> Unit
) {
    val query by cardViewModel.searchQuery.collectAsState()
    val cards by cardViewModel.cards.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = cardViewModel::onSearchQueryChanged,
            label = { Text("Search by bank, name, type, last 4 digits") },
            modifier = Modifier.fillMaxWidth()
        )

        if (cards.isEmpty()) {
            Text(
                "No cards found.",
                modifier = Modifier.padding(top = 24.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        LazyColumn(modifier = Modifier.padding(top = 12.dp)) {
            items(cards) { card ->
                CardRow(card = card, onClick = { onCardClick(card) })
            }
        }
    }
}

@Composable
private fun CardRow(card: CardEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(card.bank, style = MaterialTheme.typography.titleMedium)
            Text(card.cardName, style = MaterialTheme.typography.bodyMedium)
            Text("•••• ${card.lastFourDigits}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
