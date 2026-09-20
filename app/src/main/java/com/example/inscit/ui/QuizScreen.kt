package com.example.inscit.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inscit.*
import com.example.inscit.models.Lang
import com.example.inscit.quiz.*
import kotlinx.coroutines.flow.collectLatest

import com.example.inscit.ui.theme.spacing

@Composable
fun ScienceQuizScreen(
    lang: Lang,
    accent: Color,
    customQuestionCount: Int = 10,
    difficultyFilter: String? = null,
    round: Int? = null,
    finishButtonLabel: String? = null,
    currentStreak: Int = 0,
    onFinish: (xpEarned: Int, score: Int, strengths: List<String>, weaknesses: List<String>) -> Unit,
    viewModel: QuizViewModel = viewModel<QuizViewModel>()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val spacing = MaterialTheme.spacing

    LaunchedEffect(Unit) {
        if (round != null) {
            viewModel.startRoundQuiz(lang, round)
        } else if (viewModel.uiState.value is QuizUiState.Loading) {
            viewModel.startQuiz(lang, customQuestionCount, difficultyFilter, context)
        }
        viewModel.events.collectLatest { event ->
            when (event) {
                is QuizEvent.TriggerVibration -> triggerVibration(context, event.type)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DeepSpace)) {
        when (val currentState = state) {
            is QuizUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = accent
                )
            }
            is QuizUiState.QuizInProgress -> {
                QuizContent(currentState, lang, accent, viewModel)
            }
            is QuizUiState.Completed -> {
                // Persist attempt history for review link (cap 50, JSON array in quiz_history prefs)
                androidx.compose.runtime.LaunchedEffect(currentState.analytics) {
                    try {
                        val prefs = context.getSharedPreferences("quiz_history", android.content.Context.MODE_PRIVATE)
                        val history = prefs.getString("attempts", "[]") ?: "[]"
                        val entry = """{"id":"${System.currentTimeMillis()}","score":${currentState.analytics.overallScore},"xp":${viewModel.getFinalXp(currentStreak, currentState.analytics.overallScore)},"time":${System.currentTimeMillis()}}"""
                        val updated = if (history == "[]") "[$entry]"
                        else {
                            val inner = history.removePrefix("[").removeSuffix("]")
                            val parts = if (inner.isEmpty()) emptyList() else inner.split("},{").mapIndexed { i, p ->
                                (if (!p.startsWith("{")) "{" else "") + p + (if (!p.endsWith("}")) "}" else "")
                            }
                            val kept = (parts + entry).takeLast(50)
                            "[" + kept.joinToString(",") + "]"
                        }
                        prefs.edit().putString("attempts", updated).apply()
                    } catch (_: Exception) {}
                }
                val weakDomains = remember(currentState.analytics) {
                    val names = currentState.analytics.weaknessesEn + currentState.analytics.weaknessesHi
                    ScienceDomain.entries.filter { d -> d.displayNameEn in names || d.displayNameHi in names }.toSet()
                }
                ScienceResultScreen(
                    analytics = currentState.analytics,
                    lang = lang,
                    accent = accent,
                    finishButtonLabel = finishButtonLabel,
                    onRetry = viewModel::retry,
                    onPracticeWeakness = if (weakDomains.isEmpty()) null else ({
                        viewModel.startQuizWithWeakDomains(lang, weakDomains, context)
                    }),
                    onFinish = {
                        onFinish(
                            viewModel.getFinalXp(currentStreak, currentState.analytics.overallScore),
                            currentState.analytics.overallScore,
                            if (lang == Lang.HI) currentState.analytics.strengthsHi else currentState.analytics.strengthsEn,
                            if (lang == Lang.HI) currentState.analytics.weaknessesHi else currentState.analytics.weaknessesEn
                        )
                    }
                )
            }
            is QuizUiState.Error -> {
                Text(
                    text = currentState.message,
                    color = PowerRed,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}


@Composable
private fun QuizContent(
    state: QuizUiState.QuizInProgress,
    lang: Lang,
    accent: Color,
    viewModel: QuizViewModel
) {
    val spacing = MaterialTheme.spacing
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val isPortrait = screenHeight > screenWidth
        val horizontalPadding = if (screenWidth > 600.dp) spacing.huge else spacing.large

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding, vertical = spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val progress = (state.currentIndex + 1).toFloat() / state.totalQuestions
            // Animated bar glide instead of a hard jump each question
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(400),
                label = "quizProgress"
            )

            Spacer(Modifier.height(spacing.medium))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape),
                color = accent,
                trackColor = accent.copy(alpha = 0.2f)
            )
            
            Spacer(Modifier.height(spacing.huge))

            if (isPortrait) {
                // Portrait Layout: Stacked vertically
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == Lang.EN) "QUESTION ${state.currentIndex + 1} of ${state.totalQuestions}" else "प्रश्न ${state.currentIndex + 1} / ${state.totalQuestions}",
                        color = accent,
                        style = MaterialTheme.typography.labelLarge,
                        letterSpacing = 2.sp
                    )
                    
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
                }

                Spacer(Modifier.height(spacing.extraLarge))

                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    // Smooth slide+fade on every question change (keyed on the question itself)
                    AnimatedContent(
                        targetState = state.currentQuestion,
                        transitionSpec = {
                            (slideInHorizontally(tween(300)) { it / 4 } + fadeIn(tween(300))) togetherWith
                                (slideOutHorizontally(tween(200)) { -it / 4 } + fadeOut(tween(200)))
                        },
                        label = "questionSlide"
                    ) { question ->
                        Text(
                            text = question.text,
                            style = if (screenWidth > 600.dp) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium,
                            color = GhostWhite,
                            textAlign = TextAlign.Center,
                            lineHeight = 42.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(Modifier.height(spacing.extraLarge))

                // Equal-size options: weight shares the column equally (fixed dp heights
                // overflowed on small screens, making lower buttons shrink/clip).
                Column(
                    modifier = Modifier.weight(2f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.medium)
                ) {
                    state.currentQuestion.options.forEach { option ->
                        val isSelected = state.selectedOptionId == option.id
                        val isCorrect = option.isCorrect

                        // Animated press feedback: green/red melts in instead of hard cut
                        val backgroundColor by animateColorAsState(
                            targetValue = when {
                                isSelected && isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                isSelected && !isCorrect -> Color(0xFFF44336).copy(alpha = 0.2f)
                                else -> CardBg
                            },
                            animationSpec = tween(250),
                            label = "optBg"
                        )

                        val borderColor by animateColorAsState(
                            targetValue = when {
                                isSelected && isCorrect -> Color(0xFF4CAF50)
                                isSelected && !isCorrect -> Color(0xFFF44336)
                                else -> GhostWhite.copy(alpha = 0.1f)
                            },
                            animationSpec = tween(250),
                            label = "optBorder"
                        )

                        Surface(
                            onClick = { viewModel.answerQuestion(option.id) },
                            enabled = !state.isTransitioning,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .heightIn(min = 64.dp)
                                .semantics {
                                    contentDescription = "Answer option: ${option.text}"
                                    role = Role.Button
                                },
                            shape = RoundedCornerShape(24.dp),
                            color = backgroundColor,
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize().padding(horizontal = spacing.large)
                            ) {
                                Text(
                                    text = option.text.uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GhostWhite,
                                    textAlign = TextAlign.Center,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            } else {
                // Landscape Layout: Side-by-side
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.large)
                ) {
                    // Left Side: Question details and text
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == Lang.EN) "QUESTION ${state.currentIndex + 1} of ${state.totalQuestions}" else "प्रश्न ${state.currentIndex + 1} / ${state.totalQuestions}",
                                color = accent,
                                style = MaterialTheme.typography.labelLarge,
                                letterSpacing = 2.sp
                            )
                            
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
                        }

                        Spacer(Modifier.height(spacing.large))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = state.currentQuestion,
                                transitionSpec = {
                                    (slideInHorizontally(tween(300)) { it / 4 } + fadeIn(tween(300))) togetherWith
                                        (slideOutHorizontally(tween(200)) { -it / 4 } + fadeOut(tween(200)))
                                },
                                label = "questionSlideLand"
                            ) { question ->
                                Text(
                                    text = question.text,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = GhostWhite,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 36.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Right Side: Options / Buttons (equal shares via weight)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(spacing.small),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        state.currentQuestion.options.forEach { option ->
                            val isSelected = state.selectedOptionId == option.id
                            val isCorrect = option.isCorrect

                            val backgroundColor by animateColorAsState(
                                targetValue = when {
                                    isSelected && isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                    isSelected && !isCorrect -> Color(0xFFF44336).copy(alpha = 0.2f)
                                    else -> CardBg
                                },
                                animationSpec = tween(250),
                                label = "optBgLand"
                            )

                            val borderColor by animateColorAsState(
                                targetValue = when {
                                    isSelected && isCorrect -> Color(0xFF4CAF50)
                                    isSelected && !isCorrect -> Color(0xFFF44336)
                                    else -> GhostWhite.copy(alpha = 0.1f)
                                },
                                animationSpec = tween(250),
                                label = "optBorderLand"
                            )

                            Surface(
                                onClick = { viewModel.answerQuestion(option.id) },
                                enabled = !state.isTransitioning,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp)
                                    .semantics {
                                        contentDescription = "Answer option: ${option.text}"
                                        role = Role.Button
                                    },
                                shape = RoundedCornerShape(20.dp),
                                color = backgroundColor,
                                border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize().padding(horizontal = spacing.large)
                                ) {
                                    Text(
                                        text = option.text.uppercase(),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = GhostWhite,
                                        textAlign = TextAlign.Center,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(spacing.large))
        }
    }
}
