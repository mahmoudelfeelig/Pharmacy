package com.example.pharmacy.sip

import org.linphone.core.TransportType

/**
 * Simple configuration holder for SIP/PBX.
 * TODO: replace placeholders with your actual PBX IP/domain and credentials mapping.
 */
object SipConfig {
    // PBX IP or domain inside your lab (update if your Asterisk IP changes).
    const val PBX_DOMAIN = "172.20.10.6"

    // Default transport; change to Tcp or Tls if your PBX is configured accordingly.
    val DEFAULT_TRANSPORT = TransportType.Udp

    data class SipCredentials(val username: String, val password: String, val domain: String = PBX_DOMAIN)

    /**
     * Map app user -> SIP credentials.
     * Update the mapping below to match your real accounts.
     */
    fun credentialsFor(role: String?, profileEmail: String, fallbackUid: String): SipCredentials {
        return when (role) {
            "pharmacist" -> SipCredentials(username = "pharmacist", password = "pharmacist_pass")
            "patient" -> SipCredentials(username = "patient", password = "patient_pass")
            else -> {
                val user = profileEmail.substringBefore("@").ifBlank { fallbackUid }
                SipCredentials(username = user, password = "change_me")
            }
        }
    }
}
