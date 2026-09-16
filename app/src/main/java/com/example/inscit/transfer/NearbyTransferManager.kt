package com.example.inscit.transfer

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.inscit.models.UserDocument
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class NearbyTransferManager(private val context: Context) {
    companion object {
        const val SERVICE_ID = "com.example.inscit.transfer"
        private const val TAG = "NearbyTransfer"
    }

    private val client: ConnectionsClient by lazy { Nearby.getConnectionsClient(context) }
    private val strategy = Strategy.P2P_STAR

    private var currentPairCode: String? = null
    private var connectedEndpointId: String? = null

    private var onConnectionInitiatedCallback: ((endpointId: String, endpointName: String) -> Unit)? = null
    private var onConnectedCallback: ((endpointId: String) -> Unit)? = null
    private var onPayloadReceivedCallback: ((UserDocument, File?) -> Unit)? = null
    private var onEndpointFoundCallback: ((endpointId: String, endpointName: String) -> Unit)? = null

    private var pendingDoc: UserDocument? = null
    private val incomingFilePayloads = mutableMapOf<Long, Payload>()
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun setPairCode(code: String) {
        currentPairCode = code
    }

    fun setOnPayloadReceived(callback: (UserDocument, File?) -> Unit) {
        onPayloadReceivedCallback = callback
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            when (payload.type) {
                Payload.Type.BYTES -> {
                    val bytes = payload.asBytes() ?: return
                    val pairCode = currentPairCode ?: return
                    try {
                        val decrypted = try {
                            TransferCrypto.decryptPacked(bytes, pairCode)
                        } catch (e: Exception) {
                            Log.e(TAG, "Decrypt failed, trying raw", e)
                            bytes
                        }
                        val jsonStr = String(decrypted, Charsets.UTF_8)
                        if (jsonStr.startsWith("FILE_NAME:")) {
                            return
                        }
                        val doc = json.decodeFromString<UserDocument>(jsonStr)
                        pendingDoc = doc
                        if (doc.profile.photoUrl.isNullOrEmpty()) {
                            onPayloadReceivedCallback?.invoke(doc, null)
                            pendingDoc = null
                        } else {
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                if (pendingDoc == doc) {
                                    onPayloadReceivedCallback?.invoke(doc, null)
                                    pendingDoc = null
                                }
                            }, 3000)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "BYTES decrypt/decode failed", e)
                        try {
                            val jsonStr = String(bytes, Charsets.UTF_8)
                            val doc = json.decodeFromString<UserDocument>(jsonStr)
                            onPayloadReceivedCallback?.invoke(doc, null)
                        } catch (_: Exception) {}
                    }
                }
                Payload.Type.FILE -> {
                    incomingFilePayloads[payload.id] = payload
                }
                else -> {}
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            if (update.status == PayloadTransferUpdate.Status.SUCCESS) {
                val payload = incomingFilePayloads.remove(update.payloadId)
                if (payload != null && payload.type == Payload.Type.FILE) {
                    val pfd = payload.asFile()?.asParcelFileDescriptor() ?: return
                    try {
                        val inputStream = android.os.ParcelFileDescriptor.AutoCloseInputStream(pfd)
                        val file = File(context.filesDir, "profile_pic.jpg")
                        file.outputStream().use { output -> inputStream.copyTo(output) }
                        inputStream.close()
                        pendingDoc?.let { doc ->
                            onPayloadReceivedCallback?.invoke(doc, file)
                            pendingDoc = null
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "File copy failed", e)
                        pendingDoc?.let { doc ->
                            onPayloadReceivedCallback?.invoke(doc, null)
                            pendingDoc = null
                        }
                    }
                }
            } else if (update.status == PayloadTransferUpdate.Status.FAILURE) {
                incomingFilePayloads.remove(update.payloadId)
            }
        }
    }

    private var discoveryConnectionInitiated: ((String, String) -> Unit)? = null
    private var discoveryConnected: ((String) -> Unit)? = null

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.d(TAG, "onConnectionInitiated $endpointId ${info.endpointName}")
            onConnectionInitiatedCallback?.invoke(endpointId, info.endpointName)
            discoveryConnectionInitiated?.invoke(endpointId, info.endpointName)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            Log.d(TAG, "onConnectionResult $endpointId ${result.status.statusCode}")
            if (result.status.isSuccess) {
                connectedEndpointId = endpointId
                onConnectedCallback?.invoke(endpointId)
                discoveryConnected?.invoke(endpointId)
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
            onEndpointFoundCallback?.invoke(endpointId, info.endpointName)
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d(TAG, "onEndpointLost $endpointId")
        }
    }

    fun startAdvertising(
        pairCode: String,
        onConnectionInitiated: (endpointId: String, endpointName: String) -> Unit,
        onConnected: (endpointId: String) -> Unit,
        onPayloadReceived: (UserDocument, File?) -> Unit
    ) {
        currentPairCode = pairCode
        onConnectionInitiatedCallback = onConnectionInitiated
        onConnectedCallback = onConnected
        onPayloadReceivedCallback = onPayloadReceived
        val options = AdvertisingOptions.Builder().setStrategy(strategy).build()
        val localName = "Inscit-$pairCode"
        client.startAdvertising(localName, SERVICE_ID, connectionLifecycleCallback, options)
            .addOnSuccessListener { Log.d(TAG, "Advertising started $pairCode") }
            .addOnFailureListener { e -> Log.e(TAG, "Advertising failed", e) }
    }

    fun startDiscovery(
        onEndpointFound: (endpointId: String, endpointName: String) -> Unit,
        onConnectionInitiated: (endpointId: String, endpointName: String) -> Unit
    ) {
        onEndpointFoundCallback = onEndpointFound
        discoveryConnectionInitiated = onConnectionInitiated
        val options = DiscoveryOptions.Builder().setStrategy(strategy).build()
        client.startDiscovery(SERVICE_ID, endpointDiscoveryCallback, options)
            .addOnSuccessListener { Log.d(TAG, "Discovery started") }
            .addOnFailureListener { e -> Log.e(TAG, "Discovery failed", e) }
    }

    fun requestConnection(endpointId: String) {
        client.requestConnection(
            android.os.Build.MODEL ?: "Inscit-Receiver",
            endpointId,
            connectionLifecycleCallback
        ).addOnSuccessListener { Log.d(TAG, "requestConnection success $endpointId") }
            .addOnFailureListener { e -> Log.e(TAG, "requestConnection failed", e) }
    }

    fun acceptConnection(endpointId: String) {
        client.acceptConnection(endpointId, payloadCallback)
            .addOnSuccessListener { Log.d(TAG, "acceptConnection success") }
            .addOnFailureListener { e -> Log.e(TAG, "acceptConnection failed", e) }
    }

    fun rejectConnection(endpointId: String) {
        client.rejectConnection(endpointId)
    }

    fun sendUserDocument(endpointId: String, userDoc: UserDocument) {
        val pairCode = currentPairCode ?: return
        try {
            val jsonStr = json.encodeToString(userDoc)
            val bytes = jsonStr.toByteArray(Charsets.UTF_8)
            val encrypted = TransferCrypto.encryptPacked(bytes, pairCode)
            val bytesPayload = Payload.fromBytes(encrypted)
            client.sendPayload(endpointId, bytesPayload)

            userDoc.profile.photoUrl?.let { photoUrl ->
                try {
                    val file = File(context.filesDir, "profile_pic.jpg")
                    if (file.exists()) {
                        val nameBytes = "FILE_NAME:profile_pic.jpg".toByteArray(Charsets.UTF_8)
                        val encName = TransferCrypto.encryptPacked(nameBytes, pairCode)
                        client.sendPayload(endpointId, Payload.fromBytes(encName))
                        client.sendPayload(endpointId, Payload.fromFile(file))
                    } else {
                        val uri = Uri.parse(photoUrl)
                        if (uri.scheme == "content" || uri.scheme == "file") {
                            val tempFile = File(context.cacheDir, "temp_profile.jpg")
                            context.contentResolver.openInputStream(uri)?.use { input ->
                                tempFile.outputStream().use { output -> input.copyTo(output) }
                            }
                            if (tempFile.exists()) {
                                val nameBytes = "FILE_NAME:profile_pic.jpg".toByteArray(Charsets.UTF_8)
                                val encName = TransferCrypto.encryptPacked(nameBytes, pairCode)
                                client.sendPayload(endpointId, Payload.fromBytes(encName))
                                client.sendPayload(endpointId, Payload.fromFile(tempFile))
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "File payload send error", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "sendUserDocument error", e)
        }
    }

    fun stopAdvertising() {
        client.stopAdvertising()
    }

    fun stopDiscovery() {
        client.stopDiscovery()
    }

    fun disconnect() {
        connectedEndpointId?.let { client.disconnectFromEndpoint(it) }
        client.stopAllEndpoints()
        stopAdvertising()
        stopDiscovery()
        currentPairCode = null
        connectedEndpointId = null
        onConnectionInitiatedCallback = null
        onConnectedCallback = null
        onPayloadReceivedCallback = null
        onEndpointFoundCallback = null
        discoveryConnectionInitiated = null
        discoveryConnected = null
        incomingFilePayloads.clear()
        pendingDoc = null
    }

    fun getConnectedEndpointId(): String? = connectedEndpointId
}
