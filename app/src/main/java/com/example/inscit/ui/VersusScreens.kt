package com.example.inscit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.inscit.transfer.NearbyPermissions
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
import com.example.inscit.PowerRed
import com.example.inscit.models.Lang
import com.example.inscit.quiz.QuizEngine
import com.example.inscit.quiz.SprintDuration
import com.example.inscit.quiz.VersusMatchViewModel
import com.example.inscit.quiz.VersusRoundState
import com.example.inscit.transfer.VersusCrypto
import com.example.inscit.transfer.VersusManager
import com.example.inscit.transfer.VersusMatchPayload
import com.example.inscit.transfer.VersusResult
import com.example.inscit.transfer.VersusScorePayload
import com.example.inscit.transfer.VersusStart
import com.example.inscit.transfer.buildMatchPayload
import com.example.inscit.transfer.buildScorePayload
import com.example.inscit.transfer.decideVersusWinner
import com.example.inscit.triggerVibration
import kotlinx.coroutines.delay

// Local Versus (offline PvP) UI. Mirrors the screenshot flowchart:
//  1. Offline handshake (Nearby BT / Wi-Fi Direct, no internet).
//  2. Device A sends JSON quiz payload (questions + timer).
//  3. Simultaneous local match, locally evaluated.
//  4. Score + SHA-256 exchange, locally verified.
//  5. Instant winner, no server.

// ---- Role picker (entry below Sprint in QuizModeScreen navigates here) ----

@Composable
fun VersusRoleScreen(
    lang: Lang,
    accent: Color,
    txtCol: Color,
    onHost: () -> Unit,
    onJoin: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            PressableIconButton(onClick = onBack) { BackIcon(color = txtCol) }
            Text(
                if (lang == Lang.HI) "वर्सेस मोड" else "VERSUS MODE",
                fontSize = 20.sp, fontWeight = FontWeight.Black, color = txtCol, letterSpacing = 2.sp
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            if (lang == Lang.HI) "बिना इंटरनेट, पास में बैठे दोस्त से मुकाबला"
            else "Offline PvP - challenge a nearby friend, no internet needed",
            color = GhostWhite.copy(alpha = 0.6f), fontSize = 13.sp, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        VersusRoleCard(
            title = if (lang == Lang.HI) "होस्ट (डिवाइस A)" else "HOST (DEVICE A)",
            subtitle = if (lang == Lang.HI) "प्रश्न + टाइमर भेजें" else "Sends quiz pack (questions + timer)",
            icon = "📤",
            accent = accent,
            onClick = onHost
        )
        Spacer(Modifier.height(16.dp))
        VersusRoleCard(
            title = if (lang == Lang.HI) "जॉइन (डिवाइस B)" else "JOIN (DEVICE B)",
            subtitle = if (lang == Lang.HI) "पैक पाएं + एक साथ खेलें" else "Receives pack, starts together",
            icon = "📥",
            accent = BioLime,
            onClick = onJoin
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "1. Handshake  •  2. Quiz pack  •  3. Together  •  4. SHA-256 check  •  5. Winner",
            color = GhostWhite.copy(alpha = 0.4f), fontSize = 11.sp, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Keep Bluetooth + Wi-Fi ON and stay close. No internet used.",
            color = GhostWhite.copy(alpha = 0.4f), fontSize = 11.sp, textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun VersusRoleCard(
    title: String,
    subtitle: String,
    icon: String,
    accent: Color,
    onClick: () -> Unit
) {
    PressableCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(130.dp),
        shape = RoundedCornerShape(24.dp),
        color = CardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.3f))
    ) {
        Row(Modifier.fillMaxSize().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 40.sp)
            Spacer(Modifier.width(20.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Black, color = GhostWhite, letterSpacing = 1.sp)
                Spacer(Modifier.height(6.dp))
                Text(subtitle, fontSize = 13.sp, color = GhostWhite.copy(alpha = 0.6f))
            }
            Text("→", color = accent, fontSize = 24.sp, fontWeight = FontWeight.Black)
        }
    }
}

// ---- Host (Device A): picks timer + count, advertises, sends MATCH, waits READY ----

@Composable
fun VersusHostScreen(
    manager: VersusManager,
    matchVm: VersusMatchViewModel,
    lang: Lang,
    accent: Color,
    txtCol: Color,
    myName: String,
    onMyNameChange: (String) -> Unit,
    onMatchSent: (endpointId: String, match: VersusMatchPayload) -> Unit,
    onPeerScore: (VersusScorePayload) -> Unit,
    onPeerName: (String) -> Unit,
    onReadyToStart: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val deviceName = myName
    var duration by remember { mutableStateOf(SprintDuration.SEC_30) }
    var questionCount by remember { mutableStateOf(10) }
    var status by remember { mutableStateOf("Pick timer + questions, then wait for Device B.") }
    var connectedEndpoint by remember { mutableStateOf<String?>(null) }
    var guestName by remember { mutableStateOf<String?>(null) }
    var readyReceived by remember { mutableStateOf(false) }
    var matchSent by remember { mutableStateOf<VersusMatchPayload?>(null) }
    var showDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    // Same root cause as transfer: no runtime radio permissions => no advertising => Device B finds nothing.
    var radioGranted by remember { mutableStateOf(NearbyPermissions.hasAll(context)) }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        radioGranted = NearbyPermissions.hasAll(context)
    }

    LaunchedEffect(radioGranted) {
        if (!radioGranted) {
            status = "Grant nearby access above to start advertising."
            return@LaunchedEffect
        }
        matchVm.reset()
        manager.startHosting(
            hostName = deviceName.ifBlank { "Device-A" },
            onConnectionInitiated = { id, name ->
                showDialog = id to name
            },
            onConnected = { id ->
                connectedEndpoint = id
                status = "Connected to $guestName. Sending quiz pack..."
            },
            onReady = { ready ->
                val ep = connectedEndpoint
                val sent = matchSent
                if (ep != null && sent != null && sent.matchId == ready.matchId) {
                    readyReceived = true
                    guestName = ready.guestName
                    onPeerName(ready.guestName)
                    status = "Both ready. Starting together..."
                    triggerVibration(context, "SUCCESS")
                    manager.sendStart(ep, VersusStart(ready.matchId, 3))
                    onReadyToStart()
                }
            },
            onScore = { score ->
                // Peer finished fast while host still in lobby; forward to session state.
                onPeerScore(score)
                status = "Opponent finished early (${score.correct}/${score.attempted}). Start to see result."
            },
            onError = { e ->
                status = "Nearby radio failed: ${e.message}. Grant access + turn BT/Wi-Fi/Location on."
            }
        )
    }

    // (Re)build + send the pack whenever host name / duration / count changes after connect.
    fun sendPack(endpointId: String) {
        val bank = QuizEngine().getQuestions(lang, questionCount, null, emptySet(), context)
        val payload = buildMatchPayload(
            hostName = deviceName.ifBlank { "Device-A" },
            durationSeconds = duration.seconds,
            bank = bank
        )
        matchVm.setMatch(payload, host = true)
        manager.sendMatch(endpointId, payload)
        matchSent = payload
        onMatchSent(endpointId, payload)
        status = "Quiz pack sent (${payload.questions.size} Qs, ${duration.seconds}s). Waiting for Device B..."
        triggerVibration(context, "CLICK")
    }

    DisposableEffect(Unit) {
        onDispose { manager.stopHosting() }
    }

    if (showDialog != null) {
        val (endpointId, endpointName) = showDialog!!
        AlertDialog(
            onDismissRequest = {
                manager.rejectConnection(endpointId)
                showDialog = null
            },
            title = { Text("Device B wants to join", color = accent, fontWeight = FontWeight.Bold) },
            text = { Text("$endpointName wants the quiz pack.", color = GhostWhite) },
            confirmButton = {
                PressableTextButton(onClick = {
                    manager.acceptConnection(endpointId)
                    guestName = endpointName
                    connectedEndpoint = endpointId
                    showDialog = null
                    status = "Connected. Sending quiz pack..."
                    sendPack(endpointId)
                }) { Text("Accept", color = accent) }
            },
            dismissButton = {
                PressableTextButton(onClick = {
                    manager.rejectConnection(endpointId)
                    showDialog = null
                }) { Text("Deny", color = PowerRed) }
            },
            containerColor = CardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            PressableIconButton(onClick = {
                manager.stopHosting()
                manager.disconnect()
                onBack()
            }) { BackIcon(color = txtCol) }
            Text("HOST - DEVICE A", fontSize = 18.sp, fontWeight = FontWeight.Black, color = txtCol, letterSpacing = 1.sp)
        }
        Spacer(Modifier.height(16.dp))
        if (!radioGranted) {
            NearbyPermissionPrompt(accent = accent, onGrant = { permLauncher.launch(NearbyPermissions.required()) })
            Spacer(Modifier.height(16.dp))
        }
        OutlinedTextField(
            value = deviceName,
            onValueChange = { if (it.length <= 24) onMyNameChange(it) },
            label = { Text("Your name") },
            placeholder = { Text("Device-A") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accent,
                unfocusedBorderColor = GhostWhite.copy(alpha = 0.2f),
                focusedTextColor = GhostWhite, unfocusedTextColor = GhostWhite, cursorColor = accent
            )
        )
        Spacer(Modifier.height(16.dp))
        Text("TIMER (screenshot 1: quiz timer in pack)", color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SprintDuration.entries.forEach { d ->
                val selected = duration == d
                PressableCard(
                    onClick = { duration = d },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) accent.copy(alpha = 0.2f) else CardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) accent else GhostWhite.copy(alpha = 0.15f))
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(d.labelEn, color = if (selected) accent else GhostWhite, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("QUESTIONS", color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(5, 10, 15).forEach { n ->
                val selected = questionCount == n
                PressableCard(
                    onClick = { questionCount = n },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) accent.copy(alpha = 0.2f) else CardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) accent else GhostWhite.copy(alpha = 0.15f))
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("$n Qs", color = if (selected) accent else GhostWhite, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(status, color = GhostWhite.copy(alpha = 0.7f), fontSize = 13.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        if (connectedEndpoint == null) {
            CircularProgressIndicator(color = accent)
            Spacer(Modifier.height(8.dp))
            Text("Advertising... open JOIN on Device B.", color = GhostWhite.copy(alpha = 0.5f), fontSize = 12.sp)
        } else {
            Text("✓ Linked: ${guestName ?: connectedEndpoint?.take(8)}", color = BioLime, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            if (matchSent == null) {
                PressableButton(
                    onClick = { sendPack(connectedEndpoint!!) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = DeepSpace),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) { Text("SEND QUIZ PACK", fontWeight = FontWeight.Black) }
            } else if (!readyReceived) {
                CircularProgressIndicator(color = accent)
                Spacer(Modifier.height(8.dp))
                PressableButton(
                    onClick = { matchSent?.let { sendPack(connectedEndpoint!!) } },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GhostWhite.copy(alpha = 0.08f), contentColor = GhostWhite),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) { Text("RESEND PACK") }
            }
        }
        Spacer(Modifier.height(16.dp))
        PressableOutlinedButton(
            onClick = {
                manager.stopHosting()
                manager.disconnect()
                onBack()
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) { Text("CANCEL") }
    }
}

// ---- Guest (Device B): discovers, receives MATCH, sends READY, waits START ----

@Composable
fun VersusJoinScreen(
    manager: VersusManager,
    matchVm: VersusMatchViewModel,
    lang: Lang,
    accent: Color,
    txtCol: Color,
    myName: String,
    onMyNameChange: (String) -> Unit,
    onMatchReceived: (match: VersusMatchPayload) -> Unit,
    onPeerScore: (VersusScorePayload) -> Unit,
    onStartReceived: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val deviceName = myName
    var discovered by remember { mutableStateOf(mapOf<String, String>()) }
    var isDiscovering by remember { mutableStateOf(false) }
    var connectedEndpoint by remember { mutableStateOf<String?>(null) }
    var match by remember { mutableStateOf<VersusMatchPayload?>(null) }
    var status by remember { mutableStateOf("Discover Device A, connect, and wait for the quiz pack.") }
    var radioGranted by remember { mutableStateOf(NearbyPermissions.hasAll(context)) }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        radioGranted = NearbyPermissions.hasAll(context)
    }

    LaunchedEffect(radioGranted) {
        if (!radioGranted) return@LaunchedEffect
        matchVm.reset()
        // 45s lobby timeout like transfer (30s) but roomier for discovery.
        delay(60_000)
        if (match == null) {
            status = "No Device A found yet. Stay close, keep BT + Wi-Fi ON, retry."
        }
    }

    DisposableEffect(Unit) {
        onDispose { manager.stopDiscovery() }
    }

    fun beginDiscovery() {
        if (!radioGranted) {
            status = "Grant nearby access first."
            permLauncher.launch(NearbyPermissions.required())
            return
        }
        isDiscovering = true
        discovered = emptyMap()
        status = "Searching for Device A..."
        manager.startDiscovery(
            onEndpointFound = { id, name -> discovered = discovered + (id to name) },
            onConnectionInitiated = { id, name ->
                status = "Linking with $name..."
                manager.acceptConnection(id) // guest auto-accepts; host approves on its side
            },
            onMatch = { payload ->
                match = payload
                matchVm.setMatch(payload, host = false)
                connectedEndpoint = manager.getConnectedEndpointId()
                onMatchReceived(payload)
                val ep = manager.getConnectedEndpointId()
                if (ep != null) {
                    manager.sendReady(ep, com.example.inscit.transfer.VersusReady(payload.matchId, deviceName.ifBlank { "Device-B" }))
                }
                status = "Pack received (${payload.questions.size} Qs, ${payload.durationSeconds}s). Waiting for START..."
                triggerVibration(context, "SUCCESS")
            },
            onStart = { start ->
                if (match?.matchId == start.matchId) {
                    status = "GO!"
                    triggerVibration(context, "SUCCESS")
                    onStartReceived()
                }
            },
            onScore = { score -> onPeerScore(score) },
            onError = { e ->
                status = "Nearby radio failed: ${e.message}. Grant access + turn BT/Wi-Fi/Location on."
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            PressableIconButton(onClick = {
                manager.stopDiscovery()
                manager.disconnect()
                onBack()
            }) { BackIcon(color = txtCol) }
            Text("JOIN - DEVICE B", fontSize = 18.sp, fontWeight = FontWeight.Black, color = txtCol, letterSpacing = 1.sp)
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = deviceName,
            onValueChange = { if (it.length <= 24) onMyNameChange(it) },
            label = { Text("Your name") },
            placeholder = { Text("Device-B") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accent,
                unfocusedBorderColor = GhostWhite.copy(alpha = 0.2f),
                focusedTextColor = GhostWhite, unfocusedTextColor = GhostWhite, cursorColor = accent
            )
        )
        Spacer(Modifier.height(16.dp))
        if (!isDiscovering) {
            if (!radioGranted) {
                NearbyPermissionPrompt(accent = accent, onGrant = { permLauncher.launch(NearbyPermissions.required()) })
                Spacer(Modifier.height(16.dp))
            }
            PressableButton(
                onClick = { beginDiscovery() },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = DeepSpace),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) { Text("🔍 DISCOVER DEVICE A", fontWeight = FontWeight.Black) }
            Spacer(Modifier.height(12.dp))
            Text(status, color = GhostWhite.copy(alpha = 0.6f), fontSize = 13.sp, textAlign = TextAlign.Center)
        } else if (match == null) {
            Text(status, color = accent, fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            CircularProgressIndicator(color = accent)
            Spacer(Modifier.height(16.dp))
            if (discovered.isEmpty()) {
                Text("No hosts yet. Keep Device A on the HOST screen.", color = GhostWhite.copy(alpha = 0.5f), fontSize = 12.sp, textAlign = TextAlign.Center)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(discovered.entries.toList()) { (id, name) ->
                        PressableCard(
                            onClick = {
                                status = "Requesting link with $name..."
                                manager.requestConnection(id, deviceName.ifBlank { "Device-B" })
                                triggerVibration(context, "CLICK")
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = CardBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(accent.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) { Text("📱", fontSize = 20.sp) }
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
            PressableOutlinedButton(
                onClick = {
                    isDiscovering = false
                    manager.stopDiscovery()
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) { Text("STOP SEARCH") }
        } else {
            val m = match!!
            Text("✓ Pack from ${m.hostName}", color = BioLime, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Text("${m.questions.size} questions • ${m.durationSeconds}s timer", color = GhostWhite.copy(alpha = 0.7f), fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            Text("pack hash ${m.questionsHash.take(12)}…", color = GhostWhite.copy(alpha = 0.4f), fontSize = 11.sp)
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator(color = accent)
            Spacer(Modifier.height(8.dp))
            Text("Waiting for Device A to start. Get ready!", color = accent, fontSize = 13.sp)
        }
    }
}

// ---- Round (both devices): synced countdown, local timer, local evaluation ----

@Composable
fun VersusRoundScreen(
    manager: VersusManager,
    matchVm: VersusMatchViewModel,
    lang: Lang,
    accent: Color,
    txtCol: Color,
    peerArrived: Boolean,
    onPeerScore: (VersusScorePayload) -> Unit,
    onFinished: (attempted: Int, correct: Int, overallScore: Int) -> Unit,
    onExit: () -> Unit
) {
    val state by matchVm.round.collectAsState()
    val context = LocalContext.current
    var finishSent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        finishSent = false
        // Keep score exchange alive during the round (listener survives lobby, re-arm anyway).
        manager.setOnScore { onPeerScore(it) }
        matchVm.startCountdown(3)
    }

    LaunchedEffect(state) {
        val s = state
        if (s is VersusRoundState.Finished && !finishSent) {
            finishSent = true
            triggerVibration(context, "SUCCESS")
            onFinished(s.attempted, s.correct, s.overallScore)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DeepSpace)) {
        when (val s = state) {
            is VersusRoundState.Idle -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = accent)
            }
            is VersusRoundState.Countdown -> {
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("GET READY", color = accent, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("${s.secondsLeft}", color = GhostWhite, fontSize = 84.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(8.dp))
                    Text("Match starts together on both screens", color = GhostWhite.copy(alpha = 0.5f), fontSize = 12.sp)
                }
            }
            is VersusRoundState.Running -> {
                val q = s.currentQuestion
                val fraction = if (s.totalMs == 0L) 0f else s.timeLeftMs.toFloat() / s.totalMs
                val urgent = s.timeLeftMs <= 5000L
                val clockColor = if (urgent) PowerRed else accent
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (lang == Lang.HI) "प्रश्न ${s.index + 1}/${s.questions.size} • ✓${s.correct}"
                            else "Q ${s.index + 1}/${s.questions.size} • ✓${s.correct}",
                            color = accent, fontSize = 13.sp, fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = clockColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, clockColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                "⏱ ${formatVersusClock(s.timeLeftMs)}",
                                color = clockColor, fontSize = 14.sp, fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = clockColor,
                        trackColor = clockColor.copy(alpha = 0.2f)
                    )
                    Spacer(Modifier.height(20.dp))
                    if (q != null) {
                        Text(q.text, color = GhostWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            q.domain.name,
                            color = GhostWhite.copy(alpha = 0.4f), fontSize = 11.sp, letterSpacing = 2.sp
                        )
                        Spacer(Modifier.height(20.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            q.options.forEach { opt ->
                                QuizOptionButton(
                                    option = opt,
                                    isSelected = s.selectedOptionId == opt.id,
                                    enabled = !s.isTransitioning,
                                    minHeight = 60.dp,
                                    corner = 14.dp,
                                    horizontalPadding = 16.dp,
                                    onAnswer = { matchVm.answerQuestion(it) }
                                )
                                Spacer(Modifier.height(2.dp))
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PressableOutlinedButton(
                                onClick = { matchVm.skipQuestion() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(52.dp)
                            ) { Text("SKIP (−2 XP)") }
                            PressableOutlinedButton(
                                onClick = {
                                    matchVm.finish()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(52.dp)
                            ) { Text("END MATCH") }
                        }
                    }
                }
            }
            is VersusRoundState.Finished -> {
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("✓ You: ${s.correct}/${s.attempted} (${s.overallScore}%)", color = BioLime, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(12.dp))
                    if (!peerArrived) {
                        CircularProgressIndicator(color = accent)
                        Spacer(Modifier.height(12.dp))
                        Text("Exchanging scores + SHA-256 with opponent...", color = GhostWhite.copy(alpha = 0.6f), fontSize = 13.sp, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(20.dp))
                        PressableOutlinedButton(onClick = onExit, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                            Text("LEAVE (forfeit)")
                        }
                    } else {
                        Text("Both scores in. Declaring winner...", color = accent, fontSize = 14.sp)
                        CircularProgressIndicator(color = accent)
                    }
                }
            }
        }
    }
}

private fun formatVersusClock(ms: Long): String {
    val totalSec = ((ms + 999) / 1000).toInt()
    return "${totalSec / 60}:${(totalSec % 60).toString().padStart(2, '0')}"
}

// ---- Result (both devices): both scores + hashes, verification, instant winner ----

@Composable
fun VersusResultScreen(
    myName: String,
    peerName: String,
    isHost: Boolean,
    myAttempted: Int,
    myCorrect: Int,
    myScore: Int,
    peer: VersusScorePayload?,
    peerVerified: Boolean,
    winner: VersusResult,
    xpEarned: Int,
    accent: Color,
    txtCol: Color,
    onDone: () -> Unit
) {
    val title = when (winner) {
        VersusResult.DRAW -> "🤝 DRAW!"
        VersusResult.HOST_WINS -> if (isHost) "🏆 YOU WIN!" else "😞 YOU LOSE"
        VersusResult.GUEST_WINS -> if (!isHost) "🏆 YOU WIN!" else "😞 YOU LOSE"
    }
    val titleColor = when {
        winner == VersusResult.DRAW -> accent
        (winner == VersusResult.HOST_WINS && isHost) || (winner == VersusResult.GUEST_WINS && !isHost) -> BioLime
        else -> PowerRed
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(12.dp))
        Text(title, color = titleColor, fontSize = 30.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(
            "Winner declared instantly on-device • no server",
            color = GhostWhite.copy(alpha = 0.5f), fontSize = 12.sp, textAlign = TextAlign.Center
        )
        if (xpEarned > 0) {
            Spacer(Modifier.height(8.dp))
            Text("+$xpEarned XP added to your profile", color = BioLime, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(20.dp))
        VersusScoreCard(
            name = if (myName.isBlank()) "YOU" else "$myName (YOU)",
            attempted = myAttempted,
            correct = myCorrect,
            score = myScore,
            hashShort = null,
            verified = true,
            accent = accent,
            mine = true
        )
        Spacer(Modifier.height(12.dp))
        Text("VS", color = GhostWhite.copy(alpha = 0.4f), fontWeight = FontWeight.Black, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))
        if (peer == null) {
            Surface(
                shape = RoundedCornerShape(16.dp), color = CardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, GhostWhite.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                    Text("Waiting for opponent score...", color = GhostWhite.copy(alpha = 0.6f), fontSize = 13.sp)
                }
            }
        } else {
            VersusScoreCard(
                name = peer.deviceName,
                attempted = peer.attempted,
                correct = peer.correct,
                score = peer.overallScore,
                hashShort = peer.hash.take(12),
                verified = peerVerified,
                accent = accent,
                mine = false
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (peerVerified) "✓ Opponent hash VERIFIED (SHA-256)" else "⚠ Opponent hash MISMATCH - possible tampering",
                color = if (peerVerified) BioLime else PowerRed,
                fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(20.dp))
        Surface(
            shape = RoundedCornerShape(12.dp), color = accent.copy(alpha = 0.08f),
            border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Integrity: score + SHA-256 exchanged and recomputed locally on both screens.",
                color = GhostWhite.copy(alpha = 0.6f), fontSize = 11.sp,
                modifier = Modifier.padding(14.dp), textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(20.dp))
        PressableButton(
            onClick = onDone,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = DeepSpace),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) { Text("BACK TO QUIZ HUB", fontWeight = FontWeight.Black) }
    }
}

@Composable
private fun VersusScoreCard(
    name: String,
    attempted: Int,
    correct: Int,
    score: Int,
    hashShort: String?,
    verified: Boolean,
    accent: Color,
    mine: Boolean
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, (if (mine) accent else GhostWhite).copy(alpha = if (mine) 0.4f else 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(name.uppercase(), color = GhostWhite, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))
            Text("$correct/$attempted", color = accent, fontSize = 34.sp, fontWeight = FontWeight.Black)
            Text("$score%", color = GhostWhite.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            if (hashShort != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "hash $hashShort… ${if (verified) "✓" else "⚠"}",
                    color = (if (verified) BioLime else PowerRed).copy(alpha = 0.9f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

// Re-export so MainActivity stays small: winner from two score payloads.
fun versusWinnerFor(
    myAttempted: Int, myCorrect: Int, isHost: Boolean,
    peer: VersusScorePayload?
): VersusResult {
    if (peer == null) return VersusResult.DRAW
    val (hostC, hostA, guestC, guestA) = if (isHost) {
        Quad(myCorrect, myAttempted, peer.correct, peer.attempted)
    } else {
        Quad(peer.correct, peer.attempted, myCorrect, myAttempted)
    }
    return decideVersusWinner(hostC, guestC, hostA, guestA)
}

private data class Quad(val a: Int, val b: Int, val c: Int, val d: Int)

fun versusPeerVerified(peer: VersusScorePayload?, match: VersusMatchPayload?): Boolean {
    if (peer == null || match == null) return false
    return VersusCrypto.verifyScore(peer, match.matchId, match.questionsHash)
}
