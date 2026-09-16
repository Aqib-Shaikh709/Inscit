package com.example.inscit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inscit.BioLime
import com.example.inscit.CardBg
import com.example.inscit.DeepSpace
import com.example.inscit.GhostWhite
import com.example.inscit.NeonCyan
import com.example.inscit.PowerRed
import com.example.inscit.models.UserDocument
import com.example.inscit.saveUserDocument
import com.example.inscit.transfer.NearbyTransferManager
import com.example.inscit.transfer.TransferStage
import com.example.inscit.transfer.TransferViewModel
import com.example.inscit.triggerVibration
import com.example.inscit.ui.BackIcon
import kotlinx.coroutines.delay

@Composable
fun TransferSendScreen(
    userDoc: UserDocument,
    accent: Color,
    txtCol: Color,
    manager: NearbyTransferManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember { TransferViewModel() }
    val state by viewModel.state.collectAsState()
    val pairCode = remember { (100000..999999).random().toString() }
    var showDialog by remember { mutableStateOf<Pair<String,String>?>(null) }
    var connectedEndpoint by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(pairCode) {
        viewModel.setPairCode(pairCode)
        viewModel.setStage(TransferStage.ADVERTISING)
        manager.startAdvertising(
            pairCode = pairCode,
            onConnectionInitiated = { endpointId, endpointName ->
                showDialog = endpointId to endpointName
                viewModel.setEndpoint(endpointId, endpointName)
                viewModel.setStage(TransferStage.CONNECTING)
            },
            onConnected = { endpointId ->
                connectedEndpoint = endpointId
                viewModel.setStage(TransferStage.TRANSFERRING)
                // Send after connected
                manager.sendUserDocument(endpointId, userDoc)
                // Simulate transferring progress
            },
            onPayloadReceived = { _, _ -> }
        )
        // 30s timeout auto-deny
        delay(30000)
        if (viewModel.state.value.stage == TransferStage.ADVERTISING || viewModel.state.value.stage == TransferStage.CONNECTING) {
            viewModel.setStage(TransferStage.DENIED)
            manager.disconnect()
        }
    }

    DisposableEffect(Unit) {
        onDispose { manager.stopAdvertising() }
    }

    if (showDialog != null) {
        val (endpointId, endpointName) = showDialog!!
        AlertDialog(
            onDismissRequest = {
                manager.rejectConnection(endpointId)
                showDialog = null
                viewModel.setStage(TransferStage.DENIED)
            },
            title = { Text("Transfer Request", color = accent, fontWeight = FontWeight.Bold) },
            text = { Text("$endpointName wants to import your profile (XP ${userDoc.stats.xp}, Level ${userDoc.stats.level})", color = GhostWhite) },
            confirmButton = {
                TextButton(onClick = {
                    manager.acceptConnection(endpointId)
                    showDialog = null
                    viewModel.setStage(TransferStage.TRANSFERRING)
                }) { Text("Accept", color = accent) }
            },
            dismissButton = {
                TextButton(onClick = {
                    manager.rejectConnection(endpointId)
                    showDialog = null
                    viewModel.setStage(TransferStage.DENIED)
                }) { Text("Deny", color = PowerRed) }
            },
            containerColor = CardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Watch for success via payload? For sender, success is when bytes sent. We'll fake success after send
    LaunchedEffect(connectedEndpoint) {
        if (connectedEndpoint != null) {
            delay(2000)
            viewModel.setStage(TransferStage.SUCCESS)
            triggerVibration(context, "SUCCESS")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                manager.stopAdvertising()
                onBack()
            }) { BackIcon(color = txtCol) }
            Text("SEND TO NEW DEVICE", fontSize = 18.sp, fontWeight = FontWeight.Black, color = txtCol, letterSpacing = 1.sp)
        }
        Spacer(Modifier.height(32.dp))
        Text("Your Pair Code", color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(12.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = accent.copy(alpha = 0.1f),
            border = androidx.compose.foundation.BorderStroke(2.dp, accent)
        ) {
            Text(pairCode, fontSize = 36.sp, fontWeight = FontWeight.Black, color = accent, modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp), letterSpacing = 4.sp)
        }
        Spacer(Modifier.height(16.dp))
        Text("Share this 6-digit code with the new device. No QR needed.", color = GhostWhite.copy(alpha = 0.6f), fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(24.dp))
        when (state.stage) {
            TransferStage.ADVERTISING -> {
                Text("Waiting for receiver to connect...", color = GhostWhite.copy(alpha = 0.7f), fontSize = 14.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                CircularProgressIndicator(color = accent)
            }
            TransferStage.CONNECTING -> {
                Text("Connecting to ${state.endpointName}...", color = accent, fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
                CircularProgressIndicator(color = accent)
            }
            TransferStage.TRANSFERRING -> {
                Text("Transferring profile...", color = accent, fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
                CircularProgressIndicator(color = accent)
                if (state.totalBytes > 0) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(progress = { if (state.totalBytes == 0L) 0f else state.bytesTransferred.toFloat() / state.totalBytes }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = accent, trackColor = accent.copy(alpha = 0.2f))
                }
            }
            TransferStage.SUCCESS -> {
                Text("âœ“ Transfer Complete!", color = BioLime, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Button(onClick = {
                    manager.disconnect()
                    onBack()
                }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = DeepSpace), modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text("DONE", fontWeight = FontWeight.Black)
                }
            }
            TransferStage.DENIED -> {
                Text("Connection denied or timed out (30s)", color = PowerRed, fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
                Button(onClick = {
                    manager.disconnect()
                    onBack()
                }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = GhostWhite.copy(alpha = 0.1f), contentColor = GhostWhite), modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text("CLOSE")
                }
            }
            else -> {}
        }
        Spacer(Modifier.height(24.dp))
        OutlinedButton(
            onClick = {
                manager.stopAdvertising()
                manager.disconnect()
                onBack()
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GhostWhite.copy(alpha = 0.2f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = GhostWhite)
        ) {
            Text("CANCEL")
        }
    }
}

@Composable
fun TransferReceiveScreen(
    manager: NearbyTransferManager,
    onSuccess: (UserDocument) -> Unit,
    onGuest: () -> Unit,
    accent: Color,
    txtCol: Color,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember { TransferViewModel() }
    val state by viewModel.state.collectAsState()
    var discoveredEndpoints by remember { mutableStateOf(mapOf<String, String>()) }
    var isDiscovering by remember { mutableStateOf(false) }
    var pairCodeInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        // 30s timeout
        delay(30000)
        if (viewModel.state.value.stage == TransferStage.DISCOVERING || viewModel.state.value.stage == TransferStage.CONNECTING) {
            viewModel.setStage(TransferStage.DENIED)
            manager.disconnect()
        }
    }

    DisposableEffect(Unit) {
        onDispose { manager.stopDiscovery(); manager.disconnect() }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                manager.stopDiscovery()
                manager.disconnect()
                onBack()
            }) { BackIcon(color = txtCol) }
            Text("RECEIVE ON NEW DEVICE", fontSize = 18.sp, fontWeight = FontWeight.Black, color = txtCol, letterSpacing = 1.sp)
        }
        Spacer(Modifier.height(32.dp))

        if (!isDiscovering) {
            Text("Discover nearby devices with code", color = GhostWhite.copy(alpha = 0.7f), fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    isDiscovering = true
                    viewModel.setStage(TransferStage.DISCOVERING)
                    discoveredEndpoints = emptyMap()
                    manager.startDiscovery(
                        onEndpointFound = { id, name ->
                            discoveredEndpoints = discoveredEndpoints + (id to name)
                        },
                        onConnectionInitiated = { endpointId, endpointName ->
                            viewModel.setEndpoint(endpointId, endpointName)
                            viewModel.setStage(TransferStage.CONNECTING)
                            // Auto accept for receiver - sender will handle auth dialog
                            manager.acceptConnection(endpointId)
                        }
                    )
                    // Also set pairCode if entered? For decrypt, use entered code
                    if (pairCodeInput.length == 6) {
                        manager.setPairCode(pairCodeInput)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = DeepSpace),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                Text("ðŸ” DISCOVER DEVICES", fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = pairCodeInput,
                onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pairCodeInput = it; if (it.length == 6) manager.setPairCode(it) },
                label = { Text("Enter 6-digit code from sender") },
                placeholder = { Text("123456") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accent, unfocusedBorderColor = GhostWhite.copy(alpha = 0.2f), focusedTextColor = GhostWhite, unfocusedTextColor = GhostWhite, cursorColor = accent)
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onGuest,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GhostWhite.copy(alpha = 0.05f), contentColor = GhostWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, GhostWhite.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("SKIP - CONTINUE AS GUEST")
            }
            Spacer(Modifier.height(12.dp))
            Text("Enter the 6-digit code shown on sender", color = GhostWhite.copy(alpha = 0.4f), fontSize = 11.sp)
        } else {
            when (state.stage) {
                TransferStage.DISCOVERING -> {
                    Text("Searching for nearby Inscit devices...", color = accent, fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    CircularProgressIndicator(color = accent)
                    Spacer(Modifier.height(24.dp))
                    if (discoveredEndpoints.isEmpty()) {
                        Text("No devices found yet. Make sure sender is on Send screen.", color = GhostWhite.copy(alpha = 0.5f), fontSize = 12.sp, textAlign = TextAlign.Center)
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(discoveredEndpoints.entries.toList()) { (id, name) ->
                                Surface(
                                    onClick = {
                                        viewModel.setEndpoint(id, name)
                                        viewModel.setStage(TransferStage.CONNECTING)
                                        manager.requestConnection(id)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = CardBg,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.2f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(accent.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                                            Text("ðŸ“±", fontSize = 20.sp)
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(name, color = GhostWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(id.take(8), color = GhostWhite.copy(alpha = 0.5f), fontSize = 10.sp)
                                        }
                                        Text("CONNECT", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = {
                            isDiscovering = false
                            manager.stopDiscovery()
                            viewModel.setStage(TransferStage.SE_IDLE)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) { Text("STOP SEARCH") }
                }
                TransferStage.CONNECTING -> {
                    Text("Connecting to ${state.endpointName}...", color = accent, fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    CircularProgressIndicator(color = accent)
                }
                TransferStage.TRANSFERRING -> {
                    Text("Receiving profile...", color = accent, fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    CircularProgressIndicator(color = accent)
                    if (state.totalBytes > 0) {
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(progress = { if (state.totalBytes == 0L) 0f else state.bytesTransferred.toFloat() / state.totalBytes }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = accent)
                    }
                }
                TransferStage.SUCCESS -> {
                    Text("âœ“ Profile Imported!", color = BioLime, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text("Your data has been restored. Welcome!", color = GhostWhite.copy(alpha = 0.7f), fontSize = 14.sp)
                }
                TransferStage.DENIED -> {
                    Text("Connection denied or timed out", color = PowerRed, fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = {
                        isDiscovering = false
                        viewModel.reset()
                        manager.disconnect()
                    }, colors = ButtonDefaults.buttonColors(containerColor = accent)) { Text("TRY AGAIN") }
                }
                else -> {
                    Text("Ready to discover", color = GhostWhite.copy(alpha = 0.5f))
                }
            }
        }

        LaunchedEffect(Unit) {
            manager.setOnPayloadReceived { doc, file ->
                saveUserDocument(context, doc)
                viewModel.setStage(TransferStage.SUCCESS)
                triggerVibration(context, "SUCCESS")
                onSuccess(doc)
            }
        }
    }
}

