package com.aj.cardvault.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aj.cardvault.AppContainer
import com.aj.cardvault.nfc.NfcScanResult
import com.aj.cardvault.security.BiometricAuthHelper
import com.aj.cardvault.ui.screens.AddCardScreen
import com.aj.cardvault.ui.screens.CardDetailsScreen
import com.aj.cardvault.ui.screens.CardListScreen
import com.aj.cardvault.ui.screens.DashboardScreen
import com.aj.cardvault.ui.screens.EditCardScreen
import com.aj.cardvault.ui.screens.LockScreen
import com.aj.cardvault.ui.screens.NfcScanScreen
import com.aj.cardvault.ui.screens.SettingsScreen
import com.aj.cardvault.ui.screens.SetupScreen
import com.aj.cardvault.ui.screens.SplashScreen
import com.aj.cardvault.viewmodel.AuthUiState
import com.aj.cardvault.viewmodel.AuthViewModel
import com.aj.cardvault.viewmodel.AuthViewModelFactory
import com.aj.cardvault.viewmodel.CardViewModel
import com.aj.cardvault.viewmodel.CardViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun CardVaultNavHost(
    activity: ComponentActivity,
    container: AppContainer,
    lastNfcResult: NfcScanResult?
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(container))
    val cardViewModel: CardViewModel = viewModel(factory = CardViewModelFactory(container))

    val biometricAvailable = remember {
        activity is androidx.fragment.app.FragmentActivity &&
            BiometricAuthHelper.isBiometricAvailable(activity)
    }

    val authState by authViewModel.uiState.collectAsState()

    NavHost(navController = navController, startDestination = NavRoutes.SPLASH) {

        composable(NavRoutes.SPLASH) {
            SplashScreen(authViewModel = authViewModel) {
                val destination = when (authState) {
                    AuthUiState.NeedsSetup -> NavRoutes.SETUP
                    else -> NavRoutes.LOCK
                }
                navController.navigate(destination) {
                    popUpTo(NavRoutes.SPLASH) { inclusive = true }
                }
            }
        }

        composable(NavRoutes.SETUP) {
            SetupScreen(
                authViewModel = authViewModel,
                biometricAvailable = biometricAvailable,
                onSetupComplete = {
                    navController.navigate(NavRoutes.DASHBOARD) {
                        popUpTo(NavRoutes.SETUP) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.LOCK) {
            LockScreen(
                authViewModel = authViewModel,
                biometricAvailable = biometricAvailable,
                onRequestBiometric = {
                    if (activity is androidx.fragment.app.FragmentActivity) {
                        BiometricAuthHelper.authenticate(
                            activity,
                            onSuccess = {
                                authViewModel.onBiometricSuccess()
                                navController.navigate(NavRoutes.DASHBOARD) {
                                    popUpTo(NavRoutes.LOCK) { inclusive = true }
                                }
                            },
                            onFailureOrError = { /* Stay on lock screen; no sensitive info shown. */ }
                        )
                    }
                }
            )
            // Navigate forward once unlocked via PIN too.
            if (authState == AuthUiState.Unlocked) {
                navController.navigate(NavRoutes.DASHBOARD) {
                    popUpTo(NavRoutes.LOCK) { inclusive = true }
                }
            }
        }

        composable(NavRoutes.DASHBOARD) {
            DashboardScreen(
                cardViewModel = cardViewModel,
                onAddCard = { navController.navigate(NavRoutes.ADD_CARD) },
                onViewCards = { navController.navigate(NavRoutes.CARD_LIST) },
                onScanNfc = { navController.navigate(NavRoutes.NFC_SCAN) },
                onSettings = { navController.navigate(NavRoutes.SETTINGS) }
            )
        }

        composable(NavRoutes.CARD_LIST) {
            CardListScreen(
                cardViewModel = cardViewModel,
                onCardClick = { card -> navController.navigate(NavRoutes.cardDetails(card.id)) }
            )
        }

        composable(NavRoutes.ADD_CARD) {
            AddCardScreen(cardViewModel = cardViewModel, onSaved = { navController.popBackStack() })
        }

        composable(
            route = NavRoutes.CARD_DETAILS,
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getLong("cardId") ?: return@composable
            val cards by cardViewModel.cards.collectAsState()
            val card = cards.find { it.id == cardId }
            if (card != null) {
                CardDetailsScreen(
                    card = card,
                    cardViewModel = cardViewModel,
                    onEdit = { navController.navigate(NavRoutes.editCard(card.id)) },
                    onDeleted = { navController.popBackStack() },
                    onAssociateNfc = { navController.navigate(NavRoutes.NFC_SCAN) }
                )
            }
        }

        composable(
            route = NavRoutes.EDIT_CARD,
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getLong("cardId") ?: return@composable
            val cards by cardViewModel.cards.collectAsState()
            val card = cards.find { it.id == cardId }
            if (card != null) {
                EditCardScreen(
                    card = card,
                    cardViewModel = cardViewModel,
                    onSaved = { navController.popBackStack() }
                )
            }
        }

        composable(NavRoutes.NFC_SCAN) {
            NfcScanScreen(
                lastScanResult = lastNfcResult,
                onFoundMatch = { navController.popBackStack() },
                onNoMatchAssociatePrompt = { },
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                authViewModel = authViewModel,
                biometricAvailable = biometricAvailable,
                onClearAllData = {
                    activity.lifecycleScope.launch {
                        container.cardRepository.clearAllCards()
                        container.authManager.resetAuthConfiguration()
                        navController.navigate(NavRoutes.SETUP) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}
