package com.example.inscit.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inscit.CardBg
import com.example.inscit.DeepSpace
import com.example.inscit.GhostWhite
import com.example.inscit.goals.GoalManager
import com.example.inscit.goals.GoalScheduler
import com.example.inscit.models.GoalType
import com.example.inscit.models.Lang
import com.example.inscit.models.UserDocument
import com.example.inscit.models.UserGoal
import com.example.inscit.notifications.NotificationHelper
import com.example.inscit.triggerVibration
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private val BioGreen = Color(0xFF39FF14)

@Composable
fun GoalsScreen(
    userDocument: UserDocument,
    onUpdateUser: (UserDocument) -> Unit,
    accent: Color,
    txtCol: Color,
    lang: Lang,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        val pruned = GoalManager.pruneExpiredCompletedGoals(userDocument)
        if (pruned.goals != userDocument.goals) {
            onUpdateUser(pruned)
        }
    }

    var goalType by remember { mutableStateOf(GoalType.XP) }
    var title by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var extraText by remember { mutableStateOf("") }
    var showForm by remember { mutableStateOf(false) }

    val activeGoals = GoalManager.activeGoals(userDocument)
    val completedGoals = userDocument.goals.filter { it.isCompleted }
    val todayXp = GoalManager.todayXp(userDocument)

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val maxH = maxHeight
        val hPad = if (maxWidth > 600.dp) 48.dp else 24.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = hPad, vertical = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { BackIcon(txtCol) }
                Text(
                    if (lang == Lang.EN) "GOAL MAKER" else "लक्ष्य निर्माता",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = txtCol
                )
            }

            Spacer(Modifier.height(32.dp))

            // ===== Speedometer =====
            val xpGoal = activeGoals.firstOrNull { it.type == GoalType.XP }
            val dailyMode = xpGoal != null && xpGoal.dailyTarget > 0
            val needleValue = if (dailyMode) todayXp.toFloat() else (xpGoal?.currentValue ?: 0).toFloat()
            val needleTarget = if (dailyMode) xpGoal!!.dailyTarget.toFloat() else (xpGoal?.targetValue ?: 1).toFloat()
            val fraction = animateFloatAsState(
                targetValue = if (xpGoal == null) 0f else min(1f, needleValue / needleTarget),
                animationSpec = tween(1000),
                label = "needle"
            )

            SpeedometerGauge(
                fraction = fraction.value,
                accent = accent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (maxH < 600.dp) 150.dp else 190.dp)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = when {
                    xpGoal == null -> {
                        if (lang == Lang.EN) "SET AN XP GOAL TO UNLOCK THE SPEEDOMETER" else "स्पीडोमीटर खोलने के लिए XP लक्ष्य सेट करें"
                    }
                    dailyMode -> {
                        if (lang == Lang.EN) "TODAY: ${todayXp.toInt()} XP OF $needleTarget DAILY TARGET" else "आज: ${todayXp.toInt()} XP / $needleTarget दैनिक लक्ष्य"
                    }
                    else -> {
                        if (lang == Lang.EN) "GOAL PROGRESS: ${xpGoal.currentValue} OF ${xpGoal.targetValue} XP" else "लक्ष्य प्रगति: ${xpGoal.currentValue} / ${xpGoal.targetValue} XP"
                    }
                },
                color = accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // ===== Create new goal =====
            Surface(
                onClick = { showForm = !showForm },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = accent.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.4f))
            ) {
                Text(
                    text = if (showForm) "✕ CLOSE FORM" else "＋ NEW GOAL",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    fontWeight = FontWeight.Black,
                    color = accent,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            }

            if (showForm) {
                Spacer(Modifier.height(16.dp))
                NewGoalForm(
                    goalType = goalType,
                    onTypeChange = { goalType = it },
                    title = title,
                    onTitleChange = { title = it },
                    targetText = targetText,
                    onTargetChange = { targetText = it },
                    extraText = extraText,
                    onExtraChange = { extraText = it },
                    accent = accent,
                    lang = lang
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        val target = targetText.toIntOrNull() ?: 0
                        if (target <= 0) {
                            NotificationHelper.showNotification(
                                context,
                                "GOAL MAKER",
                                if (lang == Lang.EN) "Enter a valid target value first." else "पहले एक मान्य लक्ष्य मान दर्ज करें।"
                            )
                            return@Button
                        }
                        val extra = extraText.toIntOrNull() ?: 0
                        val updated = when (goalType) {
                            GoalType.XP -> GoalManager.createXpGoal(userDocument, title, target, extra)
                            GoalType.QUIZ -> GoalManager.createQuizGoal(
                                userDocument, title, target, if (extra in 1..100) extra else 60
                            )
                        }
                        onUpdateUser(updated)
                        GoalScheduler.scheduleDailyGoalReminder(context)
                        triggerVibration(context, "SUCCESS")
                        title = ""
                        targetText = ""
                        extraText = ""
                        showForm = false
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = DeepSpace)
                ) {
                    Text(if (lang == Lang.EN) "CREATE GOAL" else "लक्ष्य बनाएं", fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(Modifier.height(32.dp))

            // ===== Active goals =====
            Text(
                if (lang == Lang.EN) "ACTIVE GOALS" else "सक्रिय लक्ष्य",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = accent,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(12.dp))

            if (activeGoals.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CardBg,
                    border = BorderStroke(1.dp, GhostWhite.copy(alpha = 0.05f))
                ) {
                    Text(
                        text = if (lang == Lang.EN) "No goals yet. Create your first personal goal above!" else "अभी कोई लक्ष्य नहीं। ऊपर अपना पहला व्यक्तिगत लक्ष्य बनाएं!",
                        modifier = Modifier.padding(20.dp),
                        color = GhostWhite.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                activeGoals.forEach { goal ->
                    GoalCard(
                        goal = goal,
                        accent = accent,
                        lang = lang,
                        onDelete = {
                            triggerVibration(context, "CLICK")
                            onUpdateUser(GoalManager.deleteGoal(userDocument, goal.id))
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            if (completedGoals.isNotEmpty()) {
                Spacer(Modifier.height(32.dp))
                Text(
                    if (lang == Lang.EN) "COMPLETED GOALS 🏆" else "पूर्ण लक्ष्य 🏆",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = accent,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(12.dp))
                completedGoals.forEach { goal ->
                    CompletedGoalCard(goal, accent, lang)
                    Spacer(Modifier.height(12.dp))
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun NewGoalForm(
    goalType: GoalType,
    onTypeChange: (GoalType) -> Unit,
    title: String,
    onTitleChange: (String) -> Unit,
    targetText: String,
    onTargetChange: (String) -> Unit,
    extraText: String,
    onExtraChange: (String) -> Unit,
    accent: Color,
    lang: Lang
) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GoalTypeChip("XP GOAL", GoalType.XP, goalType, accent, onTypeChange, Modifier.weight(1f))
            GoalTypeChip("QUIZ GOAL", GoalType.QUIZ, goalType, accent, onTypeChange, Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { if (it.length <= 60) onTitleChange(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(if (lang == Lang.EN) "Goal name" else "लक्ष्य का नाम") },
            placeholder = {
                Text(
                    if (goalType == GoalType.XP)
                        (if (lang == Lang.EN) "E.g., XP Master" else "जैसे, XP मास्टर")
                    else "E.g., Quiz Champion"
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accent,
                unfocusedBorderColor = GhostWhite.copy(alpha = 0.15f),
                focusedTextColor = GhostWhite,
                unfocusedTextColor = GhostWhite,
                focusedLabelColor = accent,
                unfocusedLabelColor = GhostWhite.copy(alpha = 0.7f),
                focusedPlaceholderColor = GhostWhite.copy(alpha = 0.4f),
                unfocusedPlaceholderColor = GhostWhite.copy(alpha = 0.4f),
                cursorColor = accent
            ),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = targetText,
            onValueChange = { if (it.length <= 7 && it.all { c -> c.isDigit() }) onTargetChange(it) },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    when (goalType) {
                        GoalType.XP -> if (lang == Lang.EN) "Total XP goal" else "कुल XP लक्ष्य"
                        GoalType.QUIZ -> if (lang == Lang.EN) "Amount of quizzes" else "क्विज़ की संख्या"
                    }
                )
            },
            placeholder = { Text(if (goalType == GoalType.XP) "E.g., 500" else "E.g., 5") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accent,
                unfocusedBorderColor = GhostWhite.copy(alpha = 0.15f),
                focusedTextColor = GhostWhite,
                unfocusedTextColor = GhostWhite,
                focusedLabelColor = accent,
                unfocusedLabelColor = GhostWhite.copy(alpha = 0.7f),
                focusedPlaceholderColor = GhostWhite.copy(alpha = 0.4f),
                unfocusedPlaceholderColor = GhostWhite.copy(alpha = 0.4f),
                cursorColor = accent
            ),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = extraText,
            onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) onExtraChange(it) },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    when (goalType) {
                        GoalType.XP -> if (lang == Lang.EN) "Daily XP target" else "दैनिक XP लक्ष्य"
                        GoalType.QUIZ -> if (lang == Lang.EN) "Minimum score per quiz (1-100)" else "प्रति क्विज़ न्यूनतम स्कोर (1-100)"
                    }
                )
            },
            placeholder = {
                Text(
                    when (goalType) {
                        GoalType.XP -> if (lang == Lang.EN) "E.g., 100" else "जैसे, 100"
                        GoalType.QUIZ -> "80"
                    }
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accent,
                unfocusedBorderColor = GhostWhite.copy(alpha = 0.15f),
                focusedTextColor = GhostWhite,
                unfocusedTextColor = GhostWhite,
                focusedLabelColor = accent,
                unfocusedLabelColor = GhostWhite.copy(alpha = 0.7f),
                focusedPlaceholderColor = GhostWhite.copy(alpha = 0.4f),
                unfocusedPlaceholderColor = GhostWhite.copy(alpha = 0.4f),
                cursorColor = accent
            ),
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))
        Text(
            text = when (goalType) {
                GoalType.XP -> if (lang == Lang.EN) "Optional — the speedometer tracks today's XP against this daily target." else "वैकल्पिक — स्पीडोमीटर आज के XP को इस दैनिक लक्ष्य से मापता है।"
                GoalType.QUIZ -> if (lang == Lang.EN) "Every quiz scored at (or above) this percentage counts toward the goal." else "इस प्रतिशत (या अधिक) पर पूरा हर क्विज़ लक्ष्य की ओर गिना जाता है।"
            },
            fontSize = 11.sp,
            color = GhostWhite.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun GoalTypeChip(
    label: String,
    type: GoalType,
    selected: GoalType,
    accent: Color,
    onSelect: (GoalType) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = { onSelect(type) },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (selected == type) accent.copy(alpha = 0.15f) else CardBg,
        border = BorderStroke(1.dp, if (selected == type) accent else GhostWhite.copy(alpha = 0.1f))
    ) {
        Text(
            label,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            fontWeight = FontWeight.Black,
            color = if (selected == type) accent else GhostWhite.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun GoalCard(
    goal: UserGoal,
    accent: Color,
    lang: Lang,
    onDelete: () -> Unit
) {
    val progress = if (goal.targetValue > 0) min(1f, goal.currentValue.toFloat() / goal.targetValue) else 0f
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.2f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (goal.type == GoalType.XP) "⚡" else "🎯", fontSize = 18.sp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        goal.title.uppercase(),
                        fontWeight = FontWeight.Black,
                        color = GhostWhite,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (goal.type == GoalType.XP) {
                            if (lang == Lang.EN) "XP GOAL" else "XP लक्ष्य"
                        } else {
                            if (lang == Lang.EN) "QUIZ GOAL • MIN ${goal.scoreThreshold}%" else "क्विज़ लक्ष्य • न्यूनतम ${goal.scoreThreshold}%"
                        },
                        color = accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    "${goal.currentValue} / ${goal.targetValue}",
                    fontWeight = FontWeight.ExtraBold,
                    color = if (progress >= 1f) BioGreen else accent,
                    fontSize = 13.sp
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    onClick = onDelete,
                    shape = CircleShape,
                    color = Color.Transparent
                ) {
                    Text(
                        "✕",
                        modifier = Modifier.padding(6.dp),
                        color = GhostWhite.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(accent.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(8.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(accent.copy(alpha = 0.5f), accent)
                            ),
                            RoundedCornerShape(4.dp)
                        )
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = when (goal.type) {
                    GoalType.XP -> {
                        if (lang == Lang.EN) "${(progress * 100).toInt()}% OF GOAL EARNED" else "लक्ष्य का ${(progress * 100).toInt()}% अर्जित"
                    }
                    GoalType.QUIZ -> {
                        if (lang == Lang.EN) "${(progress * 100).toInt()}% COMPLETE • ${goal.currentValue} QUIZZES PASSED" else "${(progress * 100).toInt()}% पूर्ण • ${goal.currentValue} क्विज़ पास"
                    }
                },
                fontSize = 10.sp,
                color = GhostWhite.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CompletedGoalCard(goal: UserGoal, accent: Color, lang: Lang) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = accent.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.3f))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🏆", fontSize = 20.sp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    goal.title.uppercase(),
                    fontWeight = FontWeight.Black,
                    color = accent,
                    fontSize = 13.sp
                )
                Text(
                    if (lang == Lang.EN) "COMPLETED • ${goal.targetValue} REACHED" else "पूर्ण • ${goal.targetValue} प्राप्त",
                    fontSize = 10.sp,
                    color = GhostWhite.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
            }
            Text("✓", color = accent, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
    }
}

// ================= Speedometer gauge =================

@Composable
private fun SpeedometerGauge(
    fraction: Float,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxWidth()) {
        val strokeWidth = 14.dp.toPx()
        val radius = size.minDimension / 2f - strokeWidth
        val center = Offset(size.width / 2f, size.height - 8.dp.toPx())
        val arcSize = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)

        // Track (semicircle from left to right through the bottom)
        drawArc(
            color = accent.copy(alpha = 0.15f),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Progress arc
        if (fraction > 0f) {
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(accent.copy(alpha = 0.4f), accent, BioGreen),
                    center = center
                ),
                startAngle = 180f,
                sweepAngle = 180f * fraction.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Needle
        drawNeedle(
            center = center,
            length = radius - strokeWidth / 2f - 6.dp.toPx(),
            fraction = fraction.coerceIn(0f, 1f),
            color = if (fraction >= 1f) BioGreen else accent
        )
    }
}

private fun DrawScope.drawNeedle(
    center: Offset,
    length: Float,
    fraction: Float,
    color: Color
) {
    // fraction = 0 -> pointing to 9 o'clock (180°), 1 -> 3 o'clock (360°)
    val angle = Math.toRadians((180 + 180 * fraction).toDouble())
    val tip = Offset(
        center.x + (length * cos(angle)).toFloat(),
        center.y + (length * sin(angle)).toFloat()
    )
    drawLine(
        color = color,
        start = center,
        end = tip,
        strokeWidth = 5.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawCircle(
        color = color,
        radius = 8.dp.toPx(),
        center = center
    )
}