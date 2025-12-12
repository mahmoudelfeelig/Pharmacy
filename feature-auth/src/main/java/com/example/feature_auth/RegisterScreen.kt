package com.example.feature_auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.core_data.FirestoreUserRepository
import com.example.core_domain.UserProfile
import com.example.pharmacy.core.data.FirebaseAuthRepository
import com.example.core_ui.design.Spacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onDone: (UserProfile)->Unit) {
    val auth = remember { FirebaseAuthRepository() }
    val userRepo = remember { FirestoreUserRepository() }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("patient") }
    var gender by remember { mutableStateOf("male") }
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
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
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
                    Text("Create account", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Set up your role and preferences to get started.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = displayName, onValueChange = { displayName = it },
                        label = { Text("Full name") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth()
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
                    Text("Register as", style = MaterialTheme.typography.labelMedium)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        RoleOption("Patient", selected = role == "patient") { role = "patient" }
                        RoleOption("Pharmacist", selected = role == "pharmacist") { role = "pharmacist" }
                    }
                    Text("Gender", style = MaterialTheme.typography.labelMedium)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        GenderOption("Male", "male", gender) { gender = it }
                        GenderOption("Female", "female", gender) { gender = it }
                        GenderOption("Other", "other", gender) { gender = it }
                    }
                    Button(
                        onClick = {
                            loading = true; err = null
                            scope.launch {
                                auth.register(email, pass)
                                    .onSuccess { uid ->
                                        val profile = UserProfile(
                                            uid = uid,
                                            email = email,
                                            displayName = displayName.ifBlank { null },
                                            role = role,
                                            gender = gender
                                        )
                                        userRepo.upsert(profile)
                                            .onSuccess { onDone(profile) }
                                            .onFailure { err = it.message }
                                    }
                                    .onFailure { err = it.message ?: "Registration failed" }
                                loading = false
                            }
                        },
                        enabled = email.isNotBlank() && pass.length >= 6 && !loading,
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) { if (loading) CircularProgressIndicator(strokeWidth = 2.dp) else Text("Create account") }
                    err?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
}

@Composable
private fun RoleOption(label: String, selected: Boolean, onSelect: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onSelect,
        label = { Text(label) }
    )
}

@Composable
private fun GenderOption(label: String, value: String, current: String, onSelect: (String) -> Unit) {
    AssistChip(
        onClick = { onSelect(value) },
        label = { Text(label) },
        leadingIcon = {
            RadioButton(selected = current == value, onClick = { onSelect(value) })
        }
    )
}
