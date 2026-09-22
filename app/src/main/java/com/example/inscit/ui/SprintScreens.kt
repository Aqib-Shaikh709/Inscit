package com.example.inscit.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inscit.*
import com.example.inscit.models.Lang
import com.example.inscit.quiz.*
import kotlinx.coroutines.flow.collectLatest

import com.example.inscit.ui.theme.spacing

// Flowchart: initiate quiz button -> normal | sprint mode.
@Composable
fun QuizModeScreen(
    lang: Lang,
    accent: Color,
    txtCol: Color,
    onNormal: () -> Unit,
    onSprint: () -> Unit,
    onBack: () -> Unit
) {
    val spacing = MaterialTheme.spacing
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            PressableIconButton(onClick = onBack) { BackIcon(color = txtCol) }
            Text(
                if (lang == Lang.EN) "START QUIZ" else "क्विज़ शुरू करें",
                fontSize = 20.sp, fontWeight = FontWeight.Black, color = txtCol, letterSpacing = 2.sp
            )
        }
        Spacer(Modifier.height(32.dp))
        ModeCard(
            title = if (lang == Lang.EN) "NORMAL" else "सामान्य",
            subtitle = if (lang == Lang.EN) "10 questions - take your time" else "10 प्रश्न - आराम से",
            icon = "🎯",
            accent = accent,
            onClick = onNormal
        )
        Spacer(Modifier.height(16.dp))
        ModeCard(
            title = if (lang == Lang.EN) "SPRINT MODE" else "स्प्रिंट मोड",
            subtitle = if (lang == Lang.EN) "Bullet chess style - beat the clock" else "बुलेट चेस जैसा - घड़ी को हराएं",
            icon = "⚡",
            accent = PowerRed,
            onClick = onSprint
        )
    }
}

@Composable
private fun ModeCard(
    title: String,
    subtitle: String,
    icon: String,
    accent: Color,
    onClick: () -> Unit
) {
    PressableCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(140.dp),
        shape = RoundedCornerShape(24.dp),
        color = CardBg,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.3f))
    ) {
        Row(Modifier.fillMaxSize().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 44.sp)
            Spacer(Modifier.width(20.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 22.sp, fontWeight = FontWeight.Black, color = GhostWhite, letterSpacing = 1.sp)
                Spacer(Modifier.height(6.dp))
                Text(subtitle, fontSize = 13.sp, color = GhostWhite.copy(alpha = 0.6f))
            }
            Text("→", color = accent, fontSize = 24.sp, fontWeight = FontWeight.Black)
        }
    }
}

// Flowchart diamond: listed format 10s / 30s / 1min. Goal-maker card style.
@Composable
fun SprintDurationScreen(
    lang: Lang,
    accent: Color,
    txtCol: Color,
    onPick: (SprintDuration) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            PressableIconButton(onClick = onBack) { BackIcon(color = txtCol) }
            Text(
                if (lang == Lang.EN) "SPRINT MODE" else "स्प्रिंट मोड",
                fontSize = 20.sp, fontWeight = FontWeight.Black, color = txtCol, letterSpacing = 2.sp
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            if (lang == Lang.EN) "Answer infinite Qs before the clock hits zero"
            else "घड़ी शून्य होने से पहले अनंत प्रश्न हल करें",
            color = GhostWhite.copy(alpha = 0.6f), fontSize = 13.sp, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        SprintDuration.entries.forEach { duration ->
            PressableCard(
                onClick = { onPick(duration) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                color = CardBg,
                border = BorderStroke(1.dp, accent.copy(alpha = 0.25f))
            ) {
                Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(56.dp).clip(CircleShape)
                            .background(accent.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⏱", fontSize = 28.sp)
                    }
                    Spacer(Modifier.width(20.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (lang == Lang.HI) duration.labelHi else duration.labelEn,
                            fontSize = 24.sp, fontWeight = FontWeight.Black, color = GhostWhite
                        )
                        Text(
                            if (lang == Lang.EN) "Rapid infinite Q round" else "तेज़ अनंत प्रश्न राउंड",
                            fontSize = 12.sp, color = GhostWhite.copy(alpha = 0.5f)
                        )
                    }
                    Text("→", color = accent, fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

private fun formatClock(ms: Long): String {
    val totalSec = (ms + 999) / 1000
    return "${totalSec / 60}:${(totalSec % 60).toString().padStart(2, '0')}"
}

// Sprint round: infinite questions + mini clock top-right near the XP indicator.
@Composable
fun SprintQuizScreen(
    lang: Lang,
    accent: Color,
    duration: SprintDuration,
    currentStreak: Int = 0,
    viewModel: SprintViewModel = viewModel<SprintViewModel>(),
    onFinish: (xpEarned: Int, analytics: SprintAnalytics) -> Unit,
    onExit: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val spacing = MaterialTheme.spacing

    LaunchedEffect(duration) {
        viewModel.reset()
        viewModel.start(lang, duration, context)
        viewModel.events.collectLatest { event ->
            when (event) {
                is SprintEvent.TriggerVibration -> triggerVibration(context, event.type)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DeepSpace)) {
        when (val s = state) {
            is SprintUiState.Idle -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = accent)
            }
            is SprintUiState.Running -> {
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
                            text = if (lang == Lang.EN) "Q ${s.answered + 1} • ✓${s.correct}" else "प्रश्न ${s.answered + 1} • ✓${s.correct}",
                            color = accent,
                            style = MaterialTheme.typography.labelLarge,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = accent.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, accent.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = "XP: ${viewModel.pendingXp}",
                                    color = GhostWhite,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            // Mini clock, top-right next to the XP indicator.
                            Surface(
                                color = clockColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, clockColor.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "⏱ ${formatClock(s.timeLeftMs)}",
                                    color = clockColor,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = clockColor,
                        trackColor = clockColor.copy(alpha = 0.15f)
                    )
                    Spacer(Modifier.height(spacing.large))
                    Box(
                        modifier = Modifier.weight(1.2f).fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = s.currentQuestion,
                            transitionSpec = {
                                (slideInHorizontally(tween(250)) { it / 4 } + fadeIn(tween(250))) togetherWith
                                    (slideOutHorizontally(tween(180)) { -it / 4 } + fadeOut(tween(180)))
                            },
                            label = "sprintQuestion"
                        ) { question ->
                            Text(
                                text = question.text,
                                style = MaterialTheme.typography.headlineMedium,
                                color = GhostWhite,
                                textAlign = TextAlign.Center,
                                lineHeight = 38.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Spacer(Modifier.height(spacing.medium))
                    Column(
                        modifier = Modifier.weight(2f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(spacing.medium)
                    ) {
                        s.currentQuestion.options.forEach { option ->
                            key(option.id) {
                                QuizOptionButton(
                                    option = option,
                                    isSelected = s.selectedOptionId == option.id,
                                    enabled = !s.isTransitioning,
                                    minHeight = 64.dp,
                                    corner = 24.dp,
                                    horizontalPadding = spacing.large,
                                    onAnswer = viewModel::answerQuestion
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    PressableTextButton(onClick = {
                        viewModel.reset()
                        onExit()
                    }) {
                        Text(
                            if (lang == Lang.EN) "END ROUND" else "राउंड समाप्त करें",
                            color = GhostWhite.copy(alpha = 0.5f), fontSize = 12.sp
                        )
                    }
                }
            }
            is SprintUiState.Finished -> {
                // Persist sprint attempt in the same history shape as normal quizzes.
                LaunchedEffect(s.analytics) {
                    try {
                        val prefs = context.getSharedPreferences("quiz_history", android.content.Context.MODE_PRIVATE)
                        val history = prefs.getString("attempts", "[]") ?: "[]"
                        val entry = """{"id":"sprint-${System.currentTimeMillis()}","score":${s.analytics.overallScore},"xp":${viewModel.getFinalXp(currentStreak, s.analytics.overallScore)},"time":${System.currentTimeMillis()}}"""
                        val updated = if (history == "[]") "[$entry]"
                        else {
                            val inner = history.removePrefix("[").removeSuffix("]")
                            val parts = if (inner.isEmpty()) emptyList() else inner.split("},{").map { p ->
                                (if (!p.startsWith("{")) "{" else "") + p + (if (!p.endsWith("}")) "}" else "")
                            }
                            "[" + ((parts + entry).takeLast(50).joinToString(",")) + "]"
                        }
                        prefs.edit().putString("attempts", updated).apply()
                    } catch (_: Exception) {}
                }
                SprintResultScreen(
                    analytics = s.analytics,
                    lang = lang,
                    accent = accent,
                    duration = duration,
                    xpEarned = viewModel.getFinalXp(currentStreak, s.analytics.overallScore),
                    onRetry = { viewModel.reset(); viewModel.start(lang, duration, context) },
                    onFinish = {
                        onFinish(viewModel.getFinalXp(currentStreak, s.analytics.overallScore), s.analytics)
                    }
                )
            }
        }
    }
}

// Sprint result: attempted / right / wrong + the same pentagonal radar.
@Composable
fun SprintResultScreen(
    analytics: SprintAnalytics,
    lang: Lang,
    accent: Color,
    duration: SprintDuration,
    xpEarned: Int,
    onRetry: () -> Unit,
    onFinish: () -> Unit
) {
    val spacing = MaterialTheme.spacing
    var scoreShown by remember { mutableIntStateOf(0) }
    LaunchedEffect(analytics.overallScore) {
        val target = analytics.overallScore
        repeat(20) { i ->
            kotlinx.coroutines.delay(40)
            scoreShown = (target * (i + 1) / 20)
        }
        scoreShown = target
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(DeepSpace),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = if (lang == Lang.EN) "SPRINT REPORT" else "स्प्रिंट रिपोर्ट",
                style = MaterialTheme.typography.labelLarge, color = accent, letterSpacing = 2.sp
            )
            Text(
                text = "$scoreShown%",
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 80.sp),
                color = GhostWhite
            )
            Text(
                text = if (lang == Lang.HI) duration.labelHi else duration.labelEn,
                style = MaterialTheme.typography.headlineSmall, color = accent
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "+$xpEarned XP",
                style = MaterialTheme.typography.titleLarge,
                color = BioLime, fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(24.dp))
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SprintStatCard(
                    value = analytics.attempted.toString(),
                    label = if (lang == Lang.EN) "ATTEMPTED" else "हल किए",
                    accent = accent, modifier = Modifier.weight(1f)
                )
                SprintStatCard(
                    value = analytics.correct.toString(),
                    label = if (lang == Lang.EN) "RIGHT" else "सही",
                    accent = Color(0xFF4CAF50), modifier = Modifier.weight(1f)
                )
                SprintStatCard(
                    value = analytics.wrong.toString(),
                    label = if (lang == Lang.EN) "WRONG" else "गलत",
                    accent = PowerRed, modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(24.dp))
        }
        item {
            Surface(
                modifier = Modifier.size(320.dp).padding(spacing.medium),
                color = CardBg,
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(1.dp, GhostWhite.copy(alpha = 0.05f))
            ) {
                ScienceRadarChart(
                    data = analytics.radarData,
                    accent = accent,
                    modifier = Modifier.fillMaxSize().padding(spacing.large)
                )
            }
            Spacer(Modifier.height(24.dp))
        }
        item {
            ResultTraitsSection(
                title = if (lang == Lang.EN) "STRENGTHS" else "ताकत",
                traits = if (lang == Lang.EN) analytics.strengthsEn else analytics.strengthsHi,
                accent = accent,
                isPositive = true
            )
            Spacer(Modifier.height(spacing.large))
            ResultTraitsSection(
                title = if (lang == Lang.EN) "WEAKNESSES" else "कमजोरियां",
                traits = if (lang == Lang.EN) analytics.weaknessesEn else analytics.weaknessesHi,
                accent = PowerRed,
                isPositive = false
            )
            Spacer(Modifier.height(spacing.huge))
        }
        item {
            PressableButton(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.Black)
            ) {
                Text(if (lang == Lang.EN) "RETRY SPRINT" else "पुनः स्प्रिंट", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(spacing.medium))
            PressableOutlinedButton(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, accent),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = accent)
            ) {
                Text(if (lang == Lang.EN) "BACK TO HUB" else "हब पर वापस जाएं", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(spacing.huge))
        }
    }
}

@Composable
private fun SprintStatCard(value: String, label: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = accent.copy(alpha = 0.08f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.3f))
    ) {
        Column(
            Modifier.padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Black, color = GhostWhite)
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accent, letterSpacing = 1.sp)
        }
    }
}
