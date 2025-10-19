package com.example.feature_auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.pharmacy.core.data.FirebaseAuthRepository
import com.example.core_ui.design.Spacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onRegister: ()->Unit, onLoggedIn: ()->Unit) {
    val auth = remember { FirebaseAuthRepository() }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Sign in") }) }) { pad ->
        Column(
            Modifier.padding(pad).padding(Spacing.lg).fillMaxWidth()
        ) {
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email") }, singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email, imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Spacing.md))
            OutlinedTextField(
                value = pass, onValueChange = { pass = it },
                label = { Text("Password") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Spacing.lg))
            Button(
                onClick = {
                    loading = true; err = null
                    scope.launch {
                        auth.signIn(email, pass)
                            .onSuccess { onLoggedIn() }
                            .onFailure { err = it.message }
                        loading = false
                    }
                },
                enabled = email.isNotBlank() && pass.length >= 6 && !loading,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { if (loading) CircularProgressIndicator(strokeWidth = 2.dp) else Text("Login") }

            TextButton(onClick = onRegister, modifier = Modifier.padding(top = Spacing.md)) {
                Text("Create account")
            }
            err?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}
