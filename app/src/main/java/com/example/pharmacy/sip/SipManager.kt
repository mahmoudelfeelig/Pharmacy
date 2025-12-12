package com.example.pharmacy.sip

import android.content.Context
import android.util.Log
import org.linphone.core.Account
import org.linphone.core.AccountParams
import org.linphone.core.AuthInfo
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.Call
import org.linphone.core.Factory
import org.linphone.core.GlobalState
import org.linphone.core.RegistrationState
import org.linphone.core.TransportType
import org.linphone.core.ProxyConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Thin wrapper around the Linphone Core to keep SIP handling inside the app.
 *
 * Responsibilities:
 *  - Hold a single Core instance
 *  - Register the current user on the PBX
 *  - Place audio calls to a SIP URI
 */
object SipManager {

    private const val TAG = "SipManager"

    private var core: Core? = null
    private var currentAccount: Account? = null
    private val _callState = MutableStateFlow(CallUiState())
    val callState: StateFlow<CallUiState> = _callState.asStateFlow()

    /**
     * Call once from the application entry point (already done in MainActivity).
     */
    fun start(context: Context) {
        if (core != null) return

        val ctx = context.applicationContext

        val factory = Factory.instance()
        factory.setDebugMode(true, "PharmacySIP")

        val createdCore = factory.createCore(null, null, ctx)
        createdCore.addListener(object : CoreListenerStub() {
            override fun onGlobalStateChanged(core: Core, state: GlobalState, message: String) {
                Log.i(TAG, "GlobalState changed: $state ($message)")
            }

            override fun onRegistrationStateChanged(
                core: Core,
                proxyConfig: ProxyConfig,
                state: RegistrationState,
                message: String
            ) {
                val identity = proxyConfig.identityAddress?.asStringUriOnly()
                Log.i(TAG, "Registration state for $identity: $state ($message)")
            }

            override fun onCallStateChanged(
                core: Core,
                call: Call,
                state: Call.State,
                message: String
            ) {
                val remote = call.remoteAddress?.asStringUriOnly()
                Log.i(TAG, "Call state changed for $remote -> $state ($message)")
                when (state) {
                    Call.State.OutgoingInit,
                    Call.State.OutgoingProgress,
                    Call.State.OutgoingRinging,
                    Call.State.OutgoingEarlyMedia -> {
                        updateCallState(call, CallStatus.Outgoing)
                    }
                    Call.State.Connected,
                    Call.State.StreamsRunning,
                    Call.State.Updating,
                    Call.State.UpdatedByRemote -> {
                        updateCallState(call, CallStatus.Connected)
                    }
                    Call.State.End,
                    Call.State.Released,
                    Call.State.Error -> {
                        _callState.value = CallUiState(status = CallStatus.Idle)
                    }
                    else -> Unit
                }
            }
        })

        createdCore.start()
        core = createdCore
        Log.i(TAG, "Linphone Core started")
    }

    /**
     * Registers a SIP account on the PBX.
     */
    fun register(
        sipUser: String,
        sipDomain: String,
        sipPassword: String,
        transport: TransportType = TransportType.Udp
    ) {
        val c = core ?: run {
            Log.w(TAG, "register() called before Core was started")
            return
        }

        val factory = Factory.instance()

        val identity = "sip:$sipUser@$sipDomain"
        val identityAddr = factory.createAddress(identity)
        val serverAddr = factory.createAddress("sip:$sipDomain")

        val params: AccountParams = c.createAccountParams().apply {
            identityAddress = identityAddr
            serverAddress = serverAddr
            isRegisterEnabled = true
            this.transport = transport
        }

        val account: Account = c.createAccount(params)

        currentAccount?.let { c.removeAccount(it) }
        c.clearAccounts()
        c.clearAllAuthInfo()

        c.addAccount(account)
        c.defaultAccount = account
        currentAccount = account

        val authInfo: AuthInfo = factory.createAuthInfo(
            sipUser,
            null,
            sipPassword,
            null,
            null,
            sipDomain
        )
        c.addAuthInfo(authInfo)

        Log.i(TAG, "Requested registration for $identity using $transport")
    }

    /**
     * Places an outgoing audio call to the given SIP URI.
     * Returns true if the Core accepted the call.
     */
    fun call(sipUri: String): Boolean {
        val c = core ?: run {
            Log.w(TAG, "call() ignored because Core is null")
            return false
        }

        val address = c.interpretUrl(sipUri)
        if (address == null) {
            Log.w(TAG, "Invalid SIP URI: $sipUri")
            return false
        }

        val call = c.inviteAddress(address)
        if (call == null) {
            Log.w(TAG, "Failed to start call to $sipUri")
            return false
        }

        updateCallState(call, CallStatus.Outgoing)
        Log.i(TAG, "Call started to $sipUri")
        return true
    }

    /**
     * Optional helper to terminate the current call if you add a hang-up button.
     */
    fun hangUpCurrentCall() {
        val c = core ?: return
        val call = c.currentCall ?: return
        call.terminate()
        _callState.value = CallUiState(status = CallStatus.Idle)
    }

    fun toggleMute() {
        val call = core?.currentCall ?: return
        call.microphoneMuted = !call.microphoneMuted
        updateCallState(call, _callState.value.status)
    }

    fun toggleSpeaker() {
        val c = core ?: return
        val call = c.currentCall ?: return
        val devices = c.audioDevices ?: return
        val isCurrentlySpeaker = _callState.value.isSpeakerOn
        val target = if (!isCurrentlySpeaker) {
            devices.firstOrNull { it.deviceName.contains("speaker", ignoreCase = true) }
        } else {
            // Try to fallback to earpiece/receiver/first device if speaker was on
            devices.firstOrNull {
                val name = it.deviceName.lowercase()
                name.contains("earpiece") || name.contains("receiver") || name.contains("handset")
            } ?: devices.firstOrNull()
        } ?: return
        call.outputAudioDevice = target
        _callState.value = _callState.value.copy(isSpeakerOn = !isCurrentlySpeaker)
    }

    /**
     * Call when the app is shutting down.
     */
    fun stop() {
        core?.stop()
        core = null
        currentAccount = null
        Log.i(TAG, "Linphone Core stopped")
    }

    private fun updateCallState(call: Call, status: CallStatus) {
        val remote = call.remoteAddress?.asStringUriOnly()
        val connectedAt = when (status) {
            CallStatus.Connected -> _callState.value.connectedAt ?: System.currentTimeMillis()
            else -> _callState.value.connectedAt
        }
        val speakerOn = call.outputAudioDevice?.deviceName?.contains("speaker", ignoreCase = true) == true
        _callState.value = CallUiState(
            status = status,
            remote = remote,
            connectedAt = connectedAt,
            isMuted = call.microphoneMuted,
            isSpeakerOn = speakerOn
        )
    }
}

enum class CallStatus { Idle, Outgoing, Connected }

data class CallUiState(
    val status: CallStatus = CallStatus.Idle,
    val remote: String? = null,
    val connectedAt: Long? = null,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false
)
