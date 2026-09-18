package com.example.inscit.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.inscit.*
import com.example.inscit.ui.theme.spacing
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@Serializable
data class BoardEntry(val name: String, val xp: Int, val isYou: Boolean = false)

@Serializable
data class BoardCache(val generatedAt: Long, val entries: List<BoardEntry>)

object LeaderboardCache {
    private const val PREFS = "leaderboard_cache"
    private const val KEY_BOARD = "board_json"
    private const val WORK = "leaderboard_refresh"
    private val json = Json { ignoreUnknownKeys = true }
    const val STALE_MS = 24 * 60 * 60 * 1000L

    fun isOnline(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val net = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(net) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (_: Exception) { true }
    }

    fun loadCached(context: Context): BoardCache? {
        return try {
            val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_BOARD, null)
                ?: return null
            json.decodeFromString<BoardCache>(raw)
        } catch (_: Exception) { null }
    }

    // Offline-first board: deterministic local rivals + live user row. Remote fetch
    // plugs in here when a backend exists - Worker + 24h cache already wired.
    fun refresh(context: Context): BoardCache {
        val doc = try { loadUserDocument(context) } catch (_: Exception) { null }
        val userName = doc?.profile?.name?.takeIf { it.isNotBlank() } ?: "You"
        val userXp = doc?.stats?.xp ?: 0
        val rng = Random(42)
        val bots = listOf("Nova", "Quasar", "Photon", "Vector", "Zenith", "Comet", "Orbit", "Nebula")
            .map { BoardEntry(it, 200 + rng.nextInt(9500)) }
        val entries = (bots + BoardEntry(userName, userXp, true)).sortedByDescending { it.xp }
        val cache = BoardCache(System.currentTimeMillis(), entries)
        try {
            val raw = json.encodeToString(cache)
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_BOARD, raw).apply()
        } catch (_: Exception) {}
        return cache
    }

    fun getBoard(context: Context): Pair<BoardCache, Boolean> {
        val cached = loadCached(context)
        val online = isOnline(context)
        if (cached != null && (System.currentTimeMillis() - cached.generatedAt < STALE_MS || !online)) {
            return cached to online
        }
        if (!online) {
            return (cached ?: BoardCache(0L, emptyList())) to false
        }
        return refresh(context) to true
    }

    fun scheduleRefresh(context: Context) {
        val req = PeriodicWorkRequestBuilder<LeaderboardRefreshWorker>(24, TimeUnit.HOURS)
            .addTag(WORK)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK, ExistingPeriodicWorkPolicy.UPDATE, req
        )
    }
}

class LeaderboardRefreshWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        return try {
            if (LeaderboardCache.isOnline(applicationContext)) {
                LeaderboardCache.refresh(applicationContext)
            }
            Result.success()
        } catch (_: Exception) { Result.retry() }
    }
}

@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    val spacing = MaterialTheme.spacing
    val context = LocalContext.current
    // Rewritten without BoxWithConstraints (same lint as MainActivity batch)
    val screenW = LocalConfiguration.current.screenWidthDp.dp
    val horizontalPadding = if (screenW > 600.dp) spacing.huge else spacing.large

    var board by remember { mutableStateOf<BoardCache?>(null) }
    var online by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        try {
            val (cached, isOnline) = LeaderboardCache.getBoard(context)
            board = cached
            online = isOnline
        } catch (_: Exception) {}
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(DeepSpace),
        contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = spacing.large),
        verticalArrangement = Arrangement.spacedBy(spacing.small)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Leaderboard", style = MaterialTheme.typography.headlineMedium, color = NeonCyan, modifier = Modifier.weight(1f))
                IconButton(onClick = onBack, modifier = Modifier.background(GhostWhite.copy(alpha = 0.05f), CircleShape).semantics { contentDescription = "Close" }) {
                    Text("✕", color = GhostWhite, style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.height(spacing.medium))
            if (!online) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = PowerRed.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Offline - showing cached board",
                        color = GhostWhite.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
                Spacer(Modifier.height(spacing.medium))
            }
        }

        val entries = board?.entries.orEmpty()
        if (entries.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(320.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔭", fontSize = 64.sp)
                        Spacer(Modifier.height(spacing.medium))
                        Text(
                            if (online) "Loading board..." else "No cached board yet - go online once",
                            style = MaterialTheme.typography.titleMedium,
                            color = GhostWhite.copy(alpha = 0.5f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        } else {
            itemsIndexed(entries) { index, entry ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (entry.isYou) NeonCyan.copy(alpha = 0.12f) else CardBg,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (entry.isYou) NeonCyan else GhostWhite.copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "#${index + 1}",
                            color = NeonCyan,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            modifier = Modifier.width(40.dp)
                        )
                        Text(
                            entry.name + if (entry.isYou) " (YOU)" else "",
                            color = GhostWhite,
                            fontWeight = if (entry.isYou) FontWeight.Black else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        Text("${entry.xp} XP", color = GhostWhite.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
