package com.aj.cardvault.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.aj.cardvault.viewmodel.AuthViewModel

@Composable
fun LockScreen(
    authViewModel: AuthViewModel,
    biometricAvailable: Boolean,
    onRequestBiometric: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    val error by authViewModel.errorMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Card Vault Locked", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Enter your PIN to continue",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )

        OutlinedTextField(
            value = pin,
            onValueChange = { if (it.length <= 8) pin = it.filter(Char::isDigit) },
            label = { Text("PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        Button(
            onClick = { authViewModel.submitPin(pin); pin = "" },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Unlock")
        }

        if (biometricAvailable && authViewModel.isBiometricEnabled()) {
            OutlinedButton(
                onClick = onRequestBiometric,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Use biometric unlock")
            }
        }
    }
}
