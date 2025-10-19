package com.example.pharmacy.feature.auth
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.pharmacy.core.data.FirebaseAuthRepository

@Composable fun LoginScreen(onRegister:()->Unit, onLoggedIn:()->Unit) {
    val auth = remember { FirebaseAuthRepository() }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
        OutlinedTextField(email, { email=it }, label={Text("Email")}, modifier=Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(pass, { pass=it }, label={Text("Password")}, modifier=Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            scope.launch { auth.signIn(email, pass).onSuccess { onLoggedIn() }.onFailure { err=it.message } }
        }, modifier=Modifier.fillMaxWidth()) { Text("Login") }
        TextButton(onClick = onRegister) { Text("Create account") }
        err?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
