package com.aj.cardvault.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor
import java.util.concurrent.Executors

object BiometricAuthHelper {

    fun isBiometricAvailable(activity: FragmentActivity): Boolean {
        val manager = BiometricManager.from(activity)
        return manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailureOrError: () -> Unit
    ) {
        val executor: Executor = Executors.newSingleThreadExecutor()
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // Never surface raw system error strings for sensitive screens; generic only.
                onFailureOrError()
            }

            override fun onAuthenticationFailed() {
                onFailureOrError()
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Card Vault")
            .setSubtitle("Authenticate to continue")
            .setNegativeButtonText("Use PIN instead")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        prompt.authenticate(info)
    }
}
