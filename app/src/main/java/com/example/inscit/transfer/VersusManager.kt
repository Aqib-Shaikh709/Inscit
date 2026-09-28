package com.example.inscit.transfer

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy

// Offline PvP transport: Nearby P2P_STAR (Bluetooth / Wi-Fi Direct, no internet).
// Mirrors NearbyTransferManager patterns but speaks the Versus envelope protocol
// (MATCH / READY / START / SCORE) instead of UserDocument payloads.
class VersusManager(private val context: Context) {

    companion object {
        const val SERVICE_ID = "com.example.inscit.versus"
        private const val TAG = "VersusLink"
    }

    private val client: ConnectionsClient by lazy { Nearby.getConnectionsClient(context) }
    private val strategy = Strategy.P2P_STAR

    private var connectedEndpointId: String? = null

    private var onConnectionInitiatedCb: ((endpointId: String, endpointName: String) -> Unit)? = null
    private var onConnectedCb: ((endpointId: String) -> Unit)? = null
    private var onEndpointFoundCb: ((endpointId: String, endpointName: String) -> Unit)? = null
    private var discoveryInitiatedCb: ((endpointId: String, endpointName: String) -> Unit)? = null

    private var onMatchCb: ((VersusMatchPayload) -> Unit)? = null
    private var onReadyCb: ((VersusReady) -> Unit)? = null
    private var onStartCb: ((VersusStart) -> Unit)? = null
    private var onScoreCb: ((VersusScorePayload) -> Unit)? = null

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type != Payload.Type.BYTES) return
            val bytes = payload.asBytes() ?: return
            val env = VersusWire.unpack(bytes) ?: run {
                Log.w(TAG, "Unknown versus payload from $endpointId")
                return
            }
            when (env.type) {
                VersusWire.TYPE_MATCH -> VersusWire.decode<VersusMatchPayload>(env.json)?.let { onMatchCb?.invoke(it) }
                VersusWire.TYPE_READY -> VersusWire.decode<VersusReady>(env.json)?.let { onReadyCb?.invoke(it) }
                VersusWire.TYPE_START -> VersusWire.decode<VersusStart>(env.json)?.let { onStartCb?.invoke(it) }
                VersusWire.TYPE_SCORE -> VersusWire.decode<VersusScorePayload>(env.json)?.let { onScoreCb?.invoke(it) }
                else -> Log.w(TAG, "Unknown envelope type ${env.type}")
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // BYTES only; nothing to track.
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.d(TAG, "onConnectionInitiated $endpointId ${info.endpointName}")
            onConnectionInitiatedCb?.invoke(endpointId, info.endpointName)
            discoveryInitiatedCb?.invoke(endpointId, info.endpointName)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            Log.d(TAG, "onConnectionResult $endpointId ${result.status.statusCode}")
            if (result.status.isSuccess) {
                connectedEndpointId = endpointId
                onConnectedCb?.invoke(endpointId)
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.d(TAG, "onDisconnected $endpointId")
            if (connectedEndpointId == endpointId) connectedEndpointId = null
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.d(TAG, "onEndpointFound $endpointId ${info.endpointName}")
            onEndpointFoundCb?.invoke(endpointId, info.endpointName)
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d(TAG, "onEndpointLost $endpointId")
        }
    }

    fun localName(fallback: String): String = try {
        Build.MODEL ?: fallback
    } catch (_: Exception) { fallback }

    // ---- Host (Device A): advertises, sends MATCH + START, receives READY + SCORE ----
    fun startHosting(
        hostName: String,
        onConnectionInitiated: (endpointId: String, endpointName: String) -> Unit,
        onConnected: (endpointId: String) -> Unit,
        onReady: (VersusReady) -> Unit,
        onScore: (VersusScorePayload) -> Unit
    ) {
        onConnectionInitiatedCb = onConnectionInitiated
        onConnectedCb = onConnected
        onReadyCb = onReady
        onScoreCb = onScore
        val options = AdvertisingOptions.Builder().setStrategy(strategy).build()
        client.startAdvertising(localName(hostName), SERVICE_ID, connectionLifecycleCallback, options)
            .addOnSuccessListener { Log.d(TAG, "Hosting started as $hostName") }
            .addOnFailureListener { e -> Log.e(TAG, "Hosting failed", e) }
    }

    // ---- Guest (Device B): discovers, receives MATCH + START, sends READY, exchanges SCORE ----
    fun startDiscovery(
        onEndpointFound: (endpointId: String, endpointName: String) -> Unit,
        onConnectionInitiated: (endpointId: String, endpointName: String) -> Unit,
        onMatch: (VersusMatchPayload) -> Unit,
        onStart: (VersusStart) -> Unit,
        onScore: (VersusScorePayload) -> Unit
    ) {
        onEndpointFoundCb = onEndpointFound
        discoveryInitiatedCb = onConnectionInitiated
        onMatchCb = onMatch
        onStartCb = onStart
        onScoreCb = onScore
        val options = DiscoveryOptions.Builder().setStrategy(strategy).build()
        client.startDiscovery(SERVICE_ID, endpointDiscoveryCallback, options)
            .addOnSuccessListener { Log.d(TAG, "Discovery started") }
            .addOnFailureListener { e -> Log.e(TAG, "Discovery failed", e) }
    }

    // Allow host to also listen for START echo / guest MATCH edge cases if needed.
    fun setOnMatch(cb: (VersusMatchPayload) -> Unit) { onMatchCb = cb }
    fun setOnReady(cb: (VersusReady) -> Unit) { onReadyCb = cb }
    fun setOnStart(cb: (VersusStart) -> Unit) { onStartCb = cb }
    fun setOnScore(cb: (VersusScorePayload) -> Unit) { onScoreCb = cb }

    fun requestConnection(endpointId: String, guestName: String) {
        client.requestConnection(localName(guestName), endpointId, connectionLifecycleCallback)
            .addOnSuccessListener { Log.d(TAG, "requestConnection ok $endpointId") }
            .addOnFailureListener { e -> Log.e(TAG, "requestConnection failed", e) }
    }

    fun acceptConnection(endpointId: String) {
        client.acceptConnection(endpointId, payloadCallback)
            .addOnSuccessListener { Log.d(TAG, "acceptConnection ok") }
            .addOnFailureListener { e -> Log.e(TAG, "acceptConnection failed", e) }
    }

    fun rejectConnection(endpointId: String) {
        client.rejectConnection(endpointId)
    }

    private fun sendEnvelope(endpointId: String, type: String, bodyJson: String) {
        try {
            client.sendPayload(endpointId, Payload.fromBytes(VersusWire.pack(type, bodyJson)))
        } catch (e: Exception) {
            Log.e(TAG, "send $type failed", e)
        }
    }

    fun sendMatch(endpointId: String, match: VersusMatchPayload) =
        sendEnvelope(endpointId, VersusWire.TYPE_MATCH, VersusWire.encode(match))

    fun sendReady(endpointId: String, ready: VersusReady) =
        sendEnvelope(endpointId, VersusWire.TYPE_READY, VersusWire.encode(ready))

    fun sendStart(endpointId: String, start: VersusStart) =
        sendEnvelope(endpointId, VersusWire.TYPE_START, VersusWire.encode(start))

    fun sendScore(endpointId: String, score: VersusScorePayload) =
        sendEnvelope(endpointId, VersusWire.TYPE_SCORE, VersusWire.encode(score))

    fun stopHosting() {
        try { client.stopAdvertising() } catch (_: Exception) {}
    }

    fun stopDiscovery() {
        try { client.stopDiscovery() } catch (_: Exception) {}
    }

    fun disconnect() {
        try {
            connectedEndpointId?.let { client.disconnectFromEndpoint(it) }
        } catch (_: Exception) {}
        try { client.stopAllEndpoints() } catch (_: Exception) {}
        stopHosting()
        stopDiscovery()
        connectedEndpointId = null
        onConnectionInitiatedCb = null
        onConnectedCb = null
        onEndpointFoundCb = null
        discoveryInitiatedCb = null
        onMatchCb = null
        onReadyCb = null
        onStartCb = null
        onScoreCb = null
    }

    fun getConnectedEndpointId(): String? = connectedEndpointId
}
