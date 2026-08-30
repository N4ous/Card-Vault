package com.aj.cardvault.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.aj.cardvault.viewmodel.AuthViewModel

@Composable
fun SetupScreen(
    authViewModel: AuthViewModel,
    biometricAvailable: Boolean,
    onSetupComplete: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var biometricEnabled by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to Card Vault", style = MaterialTheme.typography.headlineSmall)
        Text(
            "This app works fully offline and stores your card data only on this device.",
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Set a PIN to protect your vault. If you forget this PIN, your stored card " +
                "data cannot be recovered — there is no password reset.",
            style = MaterialTheme.typography.bodySmall
        )

        OutlinedTextField(
            value = pin,
            onValueChange = { if (it.length <= 8) pin = it.filter(Char::isDigit) },
            label = { Text("Create PIN (4–8 digits)") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )
        OutlinedTextField(
            value = confirmPin,
            onValueChange = { if (it.length <= 8) confirmPin = it.filter(Char::isDigit) },
            label = { Text("Confirm PIN") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        if (biometricAvailable) {
            Row(
                modifier = Modifier.padding(top = 16.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Checkbox(checked = biometricEnabled, onCheckedChange = { biometricEnabled = it })
                Text("Also enable biometric unlock")
            }
        }

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        Button(
            onClick = {
                when {
                    pin.length < 4 -> error = "PIN must be at least 4 digits."
                    pin != confirmPin -> error = "PIN entries do not match."
                    else -> {
                        error = null
                        authViewModel.completeSetup(pin, biometricEnabled)
                        onSetupComplete()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text("Create Vault")
        }
    }
}
