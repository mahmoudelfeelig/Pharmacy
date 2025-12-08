package com.example.pharmacy.sip

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

object SipCallLauncher {

    private const val LINPHONE_PACKAGE = "org.linphone"

    fun isLinphoneInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(LINPHONE_PACKAGE, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun launchSipCall(context: Context, rawAddress: String) {
        val normalized = normalizeSipAddress(rawAddress)
        val uri = Uri.parse(normalized)

        // Prefer a direct handoff to Linphone when available.
        val linphoneIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(LINPHONE_PACKAGE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(linphoneIntent)
            return
        } catch (_: ActivityNotFoundException) {
            // Linphone not installed or not able to handle this URI on this device.
        }

        // Fallback to any app that can handle sip:
        val genericIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooser = Intent.createChooser(genericIntent, "Place call with").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(chooser)
        } catch (_: ActivityNotFoundException) {
            // At this point there truly is no SIP-capable app on the device.
            // Let the caller UI show an error message.
            throw ActivityNotFoundException("No SIP application available to place the call.")
        }
    }

    private fun normalizeSipAddress(raw: String): String {
        val trimmed = raw.trim()

        // If user already provided a scheme, keep it.
        if (trimmed.startsWith("sip:", ignoreCase = true)) return trimmed

        // Guard against the common typo you've effectively encountered.
        if (trimmed.startsWith("sip//", ignoreCase = true)) {
            return "sip:" + trimmed.removePrefix("sip//")
        }

        return "sip:$trimmed"
    }
}
