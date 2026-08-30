package com.aj.cardvault.viewmodel

import androidx.lifecycle.ViewModel
import com.aj.cardvault.security.AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class AuthUiState {
    object CheckingSetup : AuthUiState()
    object NeedsSetup : AuthUiState()
    object Locked : AuthUiState()
    object Unlocked : AuthUiState()
}

class AuthViewModel(private val authManager: AuthManager) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.CheckingSetup)
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun checkInitialState() {
        _uiState.value = if (authManager.isPinConfigured()) {
            AuthUiState.Locked
        } else {
            AuthUiState.NeedsSetup
        }
    }

    fun completeSetup(pin: String, biometricEnabled: Boolean) {
        authManager.setPin(pin)
        authManager.setBiometricEnabled(biometricEnabled)
        _uiState.value = AuthUiState.Unlocked
    }

    fun submitPin(pin: String) {
        when (val result = authManager.verifyPin(pin)) {
            AuthManager.PinResult.Success -> {
                _errorMessage.value = null
                _uiState.value = AuthUiState.Unlocked
            }
            AuthManager.PinResult.IncorrectPin -> {
                _errorMessage.value = "Incorrect PIN. Please try again."
            }
            is AuthManager.PinResult.LockedOut -> {
                val seconds = (result.remainingMillis / 1000).coerceAtLeast(1)
                _errorMessage.value = "Too many attempts. Try again in ${seconds}s."
            }
        }
    }

    fun onBiometricSuccess() {
        _uiState.value = AuthUiState.Unlocked
    }

    fun isBiometricEnabled(): Boolean = authManager.isBiometricEnabled()

    fun lock() {
        _uiState.value = AuthUiState.Locked
    }
}
