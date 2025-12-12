package com.example.feature_auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.pharmacy.core.data.FirebaseAuthRepository
import com.example.core_ui.design.Spacing
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onRegister: ()->Unit, onLoggedIn: (String)->Unit) {
    val auth = remember { FirebaseAuthRepository() }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold { pad ->
        Box(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    Modifier.padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Text("Welcome back", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Sign in to manage prescriptions and consultations.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = email, onValueChange = { email = it },
                        label = { Text("Email") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email, imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pass, onValueChange = { pass = it },
                        label = { Text("Password") }, singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            loading = true; err = null
                            scope.launch {
                                val result = withTimeoutOrNull(10_000) {
                                    auth.signIn(email, pass)
                                }
                                when {
                                    result == null -> err = "Login timed out. Check network/Play Services on this device."
                                    result.isSuccess -> onLoggedIn(result.getOrThrow())
                                    else -> err = result.exceptionOrNull()?.message ?: "Login failed"
                                }
                                loading = false
                            }
                        },
                        enabled = email.isNotBlank() && pass.length >= 6 && !loading,
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) { if (loading) CircularProgressIndicator(strokeWidth = 2.dp) else Text("Login") }

                    TextButton(onClick = onRegister, modifier = Modifier.align(Alignment.End)) {
                        Text("Create account")
                    }
                    err?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
}
