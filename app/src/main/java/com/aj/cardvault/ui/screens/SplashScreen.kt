package com.aj.cardvault.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aj.cardvault.viewmodel.AuthViewModel

@Composable
fun SplashScreen(authViewModel: AuthViewModel, onReady: () -> Unit) {
    LaunchedEffect(Unit) {
        authViewModel.checkInitialState()
        onReady()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Card Vault", style = MaterialTheme.typography.headlineMedium)
        CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
    }
}
