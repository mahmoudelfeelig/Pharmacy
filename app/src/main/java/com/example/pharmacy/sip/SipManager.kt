package com.example.pharmacy.sip

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast

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

        fun launch(intent: Intent): Boolean {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val handler = intent.resolveActivity(ctx.packageManager)
            if (handler != null) {
                ctx.startActivity(intent)
                return true
            }
            return false
        }

        val knownPackages = listOf(
            "org.linphone",
            "com.belledonnecommunications.linphone",
            "com.belledonnecommunications.linphone.debug"
        )
        for (pkg in knownPackages) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sipUri)).setPackage(pkg)
            if (launch(intent)) return
        }

        val defaultViewIntent = Intent(Intent.ACTION_VIEW, Uri.parse(sipUri))
        if (launch(defaultViewIntent)) return

        // As a last resort launch the Linphone app normally so the user can dial manually
        knownPackages.forEach { pkg ->
            val launchIntent = ctx.packageManager.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ctx.startActivity(launchIntent)
                Toast.makeText(
                    ctx,
                    "Linphone opened—dial $sipUri manually if needed.",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        }

        Log.w("SipManager", "No SIP-capable app found to handle $sipUri")
        Toast.makeText(
            ctx,
            "Install/assign a SIP app such as Linphone to place calls",
            Toast.LENGTH_LONG
        ).show()
    }

    fun stop() {
        // No-op for external client usage
    }
}
