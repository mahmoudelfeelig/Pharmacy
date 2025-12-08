package com.example.pharmacy.sip

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

/**
 * Lightweight SIP launcher: uses external SIP-capable apps (e.g., Linphone) via intent.
 * This avoids SDK API mismatches while still letting the app initiate calls.
 */
object SipManager {
    private var appContext: Context? = null

    fun start(context: Context) {
        appContext = context.applicationContext
    }

    fun register(
        sipUser: String,
        sipDomain: String,
        sipPassword: String,
        transport: Any? = null // kept for signature compatibility; unused
    ) {
        // No-op: external SIP app handles registration.
        Log.d("SipManager", "register called for $sipUser@$sipDomain (handled by external client)")
    }

    fun call(sipUri: String) {
        val ctx = appContext ?: return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sipUri)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(ctx.packageManager) != null) {
            ctx.startActivity(intent)
        } else {
            Log.w("SipManager", "No SIP-capable app found to handle $sipUri")
        }
    }

    fun stop() {
        // No-op for external client usage
    }
}
