package com.example.pharmacy.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pharmacy.sip.CallStatus
import com.example.pharmacy.sip.CallUiState
import kotlinx.coroutines.delay

@Composable
fun CallScreen(
    state: CallUiState,
    onHangUp: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit
) {
    AnimatedVisibility(visible = state.status != CallStatus.Idle) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = state.remote?.removePrefix("sip:") ?: "Connecting…",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center
                        )
                        val statusLabel = when (state.status) {
                            CallStatus.Outgoing -> "Calling…"
                            CallStatus.Connected -> "In call"
                            CallStatus.Idle -> ""
                        }
                        Text(
                            statusLabel,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )

                        var elapsed by remember(state.connectedAt) { mutableStateOf(0L) }
                        LaunchedEffect(state.connectedAt, state.status) {
                            if (state.status == CallStatus.Connected && state.connectedAt != null) {
                                while (true) {
                                    elapsed = (System.currentTimeMillis() - state.connectedAt) / 1000
                                    delay(1000)
                                }
                            } else {
                                elapsed = 0L
                            }
                        }
                        if (state.status == CallStatus.Connected) {
                            val mins = elapsed / 60
                            val secs = elapsed % 60
                            Text(
                                String.format("%02d:%02d", mins, secs),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CallIconButton(
                            onClick = onToggleMute,
                            icon = if (state.isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                            label = if (state.isMuted) "Unmute" else "Mute",
                            active = state.isMuted
                        )
                        CallIconButton(
                            onClick = onToggleSpeaker,
                            icon = if (state.isSpeakerOn) Icons.Filled.VolumeUp else Icons.Outlined.VolumeOff,
                            label = if (state.isSpeakerOn) "Speaker off" else "Speaker on",
                            active = state.isSpeakerOn
                        )
                        CallIconButton(
                            onClick = onHangUp,
                            icon = Icons.Filled.CallEnd,
                            label = "Hang up",
                            active = true,
                            tint = MaterialTheme.colorScheme.onError,
                            background = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CallIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    background: Color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f),
    tint: Color = MaterialTheme.colorScheme.onPrimary
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            modifier = Modifier
                .height(72.dp)
                .background(Color.Transparent, shape = CircleShape),
            shape = CircleShape,
            color = background,
            tonalElevation = if (active) 6.dp else 0.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = label, tint = tint)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, color = MaterialTheme.colorScheme.onPrimary)
    }
}
