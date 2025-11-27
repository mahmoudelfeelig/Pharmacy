package com.example.feature_home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pharmacy.core.data.FirestoreCartRepository
import com.example.pharmacy.core.domain.cart.CartItem
import com.example.core_ui.design.Spacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientCartScreen(
    userId: String,
    onBack: () -> Unit,
    cartRepo: FirestoreCartRepository = remember { FirestoreCartRepository() }
) {
    val cart by cartRepo.streamCart(userId).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val host = remember { SnackbarHostState() }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(message) {
        message?.let { host.showSnackbar(it) }
        message = null
    }

    val total = cart.sumOf { it.price * it.quantity }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cart") },
                navigationIcon = { IconButton(onClick = onBack) { Text("Back") } }
            )
        },
        snackbarHost = { SnackbarHost(host) }
    ) { pad ->
        Column(
            Modifier.padding(pad).padding(Spacing.lg).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            if (cart.isEmpty()) {
                Text("Your cart is empty")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    items(cart, key = { it.medId }) { item ->
                        CartRow(
                            item = item,
                            onRemove = {
                                scope.launch { cartRepo.removeItem(userId, item.medId) }
                            }
                        )
                        Divider()
                    }
                }
                Text("Total: $total EGP", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    TextButton(onClick = {
                        scope.launch {
                            cartRepo.clearCart(userId); message = "Cart cleared"
                        }
                    }) { Text("Clear cart") }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = {
                        scope.launch {
                            cartRepo.placeOrder(userId, cart)
                            cartRepo.clearCart(userId)
                            message = "Order placed"
                        }
                    }) { Text("Order") }
                }
            }
        }
    }
}

@Composable
private fun CartRow(item: CartItem, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.medName, style = androidx.compose.material3.MaterialTheme.typography.titleSmall)
            Text("Qty: ${item.quantity}", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            Text("Price: ${item.price}", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        }
        TextButton(onClick = onRemove) { Text("Remove") }
    }
}
