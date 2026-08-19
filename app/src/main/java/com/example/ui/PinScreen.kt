package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.compose.ui.platform.LocalContext
import android.content.Context

@Composable
fun PinScreen(onVerify: (String) -> Boolean, onUnlocked: () -> Unit = {}) {
    var pinValue by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val sharedPrefs = context.getSharedPreferences("FinanceTrackerPrefs", Context.MODE_PRIVATE)
    val biometricEnabled = sharedPrefs.getBoolean("biometric_enabled", false)
    val canUseBiometric = remember {
        biometricEnabled && BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun launchBiometric() {
        val act = activity ?: return
        val executor = ContextCompat.getMainExecutor(context)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onUnlocked()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // User dismissed or hardware error — PIN entry remains visible, no action needed.
            }
            override fun onAuthenticationFailed() {
                // Not recognized — user can retry via button or just use PIN.
            }
        }
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Finance Tracker")
            .setSubtitle("Use fingerprint or face to unlock")
            .setNegativeButtonText("Use PIN instead")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
        BiometricPrompt(act, executor, callback).authenticate(promptInfo)
    }

    LaunchedEffect(Unit) {
        if (canUseBiometric) {
            launchBiometric()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { } },
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "App Locked",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Enter your 4-digit PIN",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = pinValue,
                onValueChange = {
                    if (it.length <= 4) {
                        pinValue = it
                        isError = false
                        if (it.length == 4) {
                            val success = onVerify(it)
                            if (!success) {
                                isError = true
                                pinValue = ""
                            }
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = isError,
                supportingText = if (isError) { { Text("Incorrect PIN") } } else null,
                modifier = Modifier.fillMaxWidth(0.6f),
                textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            )
            
            if (canUseBiometric) {
                Spacer(Modifier.height(20.dp))
                TextButton(
                    onClick = { launchBiometric() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Biometric unlock",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Use Fingerprint / Face ID")
                }
            }
        }
    }
}
