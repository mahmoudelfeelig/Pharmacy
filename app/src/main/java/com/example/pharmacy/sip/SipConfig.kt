package com.example.pharmacy.sip

import org.linphone.core.TransportType

/**
 * Simple configuration holder for SIP/PBX.
 * TODO: replace placeholders with your actual PBX IP/domain and credentials mapping.
 */
object SipConfig {
    // PBX IP or domain, e.g., "192.168.1.50"
    const val PBX_DOMAIN = "172.20.10.4" // TODO replace with your actual PBX IP/domain

    // Default transport; change to Tcp or Tls if your PBX is configured accordingly.
    val DEFAULT_TRANSPORT = TransportType.Udp

    data class SipCredentials(val username: String, val password: String, val domain: String = PBX_DOMAIN)

    /**
     * Map app user -> SIP credentials.
     * Update the mapping below to match your real accounts.
     */
    fun credentialsFor(role: String?, profileEmail: String, fallbackUid: String): SipCredentials {
        // Example: map roles to SIP usernames and passwords
        return when (role) {
            "pharmacist" -> SipCredentials(username = "pharmacist", password = "pharmacist_pass")
            "patient" -> SipCredentials(username = "patient", password = "patient_pass")
            else -> {
                // Fallback: derive username from email local part or UID, and use a default/demo password
                val user = profileEmail.substringBefore("@").ifBlank { fallbackUid }
                SipCredentials(username = user, password = "change_me")
            }
        }
    }
}
