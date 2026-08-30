package com.aj.cardvault.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aj.cardvault.data.entity.CardEntity
import com.aj.cardvault.data.entity.CardType
import com.aj.cardvault.data.repository.CardRepository
import com.aj.cardvault.data.repository.DecryptedCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CardViewModel(private val repository: CardRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val cards: StateFlow<List<CardEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.getAllCards() else repository.search(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _operationError = MutableStateFlow<String?>(null)
    val operationError: StateFlow<String?> = _operationError

    private val _revealedCard = MutableStateFlow<DecryptedCard?>(null)
    val revealedCard: StateFlow<DecryptedCard?> = _revealedCard

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun addCard(
        type: CardType,
        bank: String,
        cardName: String,
        cardholderName: String,
        cardNumber: String,
        expiryMonth: Int,
        expiryYear: Int,
        cvv: String?,
        notes: String?,
        onDone: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.addCard(
                type, bank, cardName, cardholderName, cardNumber, expiryMonth, expiryYear, cvv, notes
            )
            result.onSuccess {
                _operationError.value = null
                onDone(true)
            }.onFailure {
                _operationError.value = it.message ?: "Could not save this card."
                onDone(false)
            }
        }
    }

    fun updateCard(existing: CardEntity, updated: DecryptedCard, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.updateCard(existing, updated)
            result.onSuccess {
                _operationError.value = null
                onDone(true)
            }.onFailure {
                _operationError.value = it.message ?: "Could not update this card."
                onDone(false)
            }
        }
    }

    fun deleteCard(entity: CardEntity) {
        viewModelScope.launch { repository.deleteCard(entity) }
    }

    /** Call only after the user has authenticated and explicitly asked to view details. */
    fun reveal(entity: CardEntity) {
        viewModelScope.launch {
            try {
                _revealedCard.value = repository.reveal(entity)
            } catch (e: Exception) {
                _operationError.value = "Could not decrypt this card's data."
            }
        }
    }

    fun clearRevealed() {
        _revealedCard.value = null
    }

    fun associateNfc(entity: CardEntity, identifier: String) {
        viewModelScope.launch { repository.associateNfc(entity, identifier) }
    }

    fun removeNfcAssociation(entity: CardEntity) {
        viewModelScope.launch { repository.removeNfcAssociation(entity) }
    }

    suspend fun findByNfcIdentifier(identifier: String): CardEntity? =
        repository.findByNfcIdentifier(identifier)

    fun clearError() {
        _operationError.value = null
    }
}
