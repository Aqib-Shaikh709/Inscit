package com.example.inscit.ui

import androidx.compose.animation.*
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
    finishButtonLabel: String? = null,
    onFinish: (xpEarned: Int, score: Int, strengths: List<String>, weaknesses: List<String>) -> Unit,
    viewModel: QuizViewModel = viewModel<QuizViewModel>()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val spacing = MaterialTheme.spacing

    LaunchedEffect(Unit) {
        if (viewModel.uiState.value is QuizUiState.Loading) {
            viewModel.startQuiz(lang, customQuestionCount, difficultyFilter)
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
                ScienceResultScreen(
                    analytics = currentState.analytics,
                    lang = lang,
                    accent = accent,
                    finishButtonLabel = finishButtonLabel,
                    onRetry = viewModel::retry,
                    onFinish = {
                        onFinish(
                            viewModel.getFinalXp(),
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
            
            Spacer(Modifier.height(spacing.medium))

            LinearProgressIndicator(
                progress = { progress },
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
                    Text(
                        text = state.currentQuestion.text,
                        style = if (screenWidth > 600.dp) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium,
                        color = GhostWhite,
                        textAlign = TextAlign.Center,
                        lineHeight = 42.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(spacing.extraLarge))

                Column(
                    modifier = Modifier.weight(2f),
                    verticalArrangement = Arrangement.spacedBy(spacing.medium)
                ) {
                    state.currentQuestion.options.forEach { option ->
                        val isSelected = state.selectedOptionId == option.id
                        val isCorrect = option.isCorrect
                        
                        val backgroundColor = when {
                            isSelected && isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                            isSelected && !isCorrect -> Color(0xFFF44336).copy(alpha = 0.2f)
                            else -> CardBg
                        }
                        
                        val borderColor = when {
                            isSelected && isCorrect -> Color(0xFF4CAF50)
                            isSelected && !isCorrect -> Color(0xFFF44336)
                            else -> GhostWhite.copy(alpha = 0.1f)
                        }

                        Surface(
                            onClick = { viewModel.answerQuestion(option.id) },
                            enabled = !state.isTransitioning,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(84.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = backgroundColor,
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = spacing.large)) {
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
                            Text(
                                text = state.currentQuestion.text,
                                style = MaterialTheme.typography.headlineMedium,
                                color = GhostWhite,
                                textAlign = TextAlign.Center,
                                lineHeight = 36.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Right Side: Options / Buttons
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        state.currentQuestion.options.forEach { option ->
                            val isSelected = state.selectedOptionId == option.id
                            val isCorrect = option.isCorrect
                            
                            val backgroundColor = when {
                                isSelected && isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                isSelected && !isCorrect -> Color(0xFFF44336).copy(alpha = 0.2f)
                                else -> CardBg
                            }
                            
                            val borderColor = when {
                                isSelected && isCorrect -> Color(0xFF4CAF50)
                                isSelected && !isCorrect -> Color(0xFFF44336)
                                else -> GhostWhite.copy(alpha = 0.1f)
                            }

                            Surface(
                                onClick = { viewModel.answerQuestion(option.id) },
                                enabled = !state.isTransitioning,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = spacing.small)
                                    .height(72.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = backgroundColor,
                                border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = spacing.large)) {
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
