package com.example.inscit.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.inscit.*
import com.example.inscit.models.Lang
import com.example.inscit.quiz.*
import kotlin.math.cos
import kotlin.math.sin

import com.example.inscit.ui.theme.spacing

@Composable
fun ScienceResultScreen(
    analytics: ScienceAnalytics,
    lang: Lang,
    accent: Color,
    finishButtonLabel: String? = null,
    onRetry: () -> Unit,
    onFinish: () -> Unit
) {
    val spacing = MaterialTheme.spacing
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val horizontalPadding = if (screenWidth > 600.dp) spacing.huge else spacing.large

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(Modifier.height(spacing.medium))
                Text(
                    text = if (lang == Lang.EN) "LAB REPORT" else "लैब रिपोर्ट",
                    style = MaterialTheme.typography.labelLarge,
                    color = accent,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "${analytics.overallScore}%",
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = if (screenWidth > 600.dp) 120.sp else 80.sp),
                    color = GhostWhite
                )
                Text(
                    text = if (lang == Lang.EN) analytics.scienceTypeEn else analytics.scienceTypeHi,
                    style = MaterialTheme.typography.headlineMedium,
                    color = accent
                )
                Spacer(Modifier.height(spacing.huge))
            }

            item {
                Surface(
                    modifier = Modifier
                        .size(if (screenWidth > 600.dp) 400.dp else 320.dp)
                        .padding(spacing.medium),
                    color = CardBg,
                    shape = RoundedCornerShape(32.dp),
                    border = BorderStroke(1.dp, GhostWhite.copy(alpha = 0.05f))
                ) {
                    ScienceRadarChart(
                        data = analytics.radarData,
                        accent = accent,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(spacing.large)
                    )
                }
                Spacer(Modifier.height(spacing.huge))
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
                    title = if (lang == Lang.EN) "AVERAGE" else "औसत",
                    traits = if (lang == Lang.EN) analytics.averageEn else analytics.averageHi,
                    accent = accent.copy(alpha = 0.6f),
                    isPositive = true,
                    emptyText = if (lang == Lang.EN) "Consistent performance" else "संतुलित प्रदर्शन"
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
                Text(
                    text = if (lang == Lang.EN) "SCIENTIFIC EXPLANATIONS" else "वैज्ञानिक व्याख्या",
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = accent,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(spacing.medium))
            }

            items(analytics.explanations) { (question, explanation) ->
                ResultExplanationCard(question, explanation, accent)
            }

            item {
                Spacer(Modifier.height(spacing.huge))
                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.Black)
                ) {
                    Text(if (lang == Lang.EN) "RETRY QUIZ" else "पुनः प्रयास करें", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(spacing.medium))
                OutlinedButton(
                    onClick = onFinish,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(2.dp, accent),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accent)
                ) {
                    Text(
                        text = finishButtonLabel ?: (if (lang == Lang.EN) "BACK TO HUB" else "हब पर वापस जाएं"),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(Modifier.height(spacing.huge))
            }
        }
    }
}

@Composable
fun ScienceRadarChart(data: List<DomainScore>, accent: Color, modifier: Modifier = Modifier) {
    val outlineColor = GhostWhite.copy(alpha = 0.1f)

    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2.2f
        val sides = data.size
        val angleStep = 2 * Math.PI / sides

        // Draw background web
        for (i in 1..4) {
            val r = radius * (i / 4f)
            val path = Path()
            for (j in 0 until sides) {
                val angle = j * angleStep - Math.PI / 2
                val x = center.x + r * cos(angle).toFloat()
                val y = center.y + r * sin(angle).toFloat()
                if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, outlineColor, style = Stroke(width = 1.dp.toPx()))
        }

        // Draw Axis
        for (j in 0 until sides) {
            val angle = j * angleStep - Math.PI / 2
            drawLine(
                color = outlineColor,
                start = center,
                end = Offset(
                    center.x + radius * cos(angle).toFloat(),
                    center.y + radius * sin(angle).toFloat()
                ),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw actual data
        val dataPath = Path()
        data.forEachIndexed { index, score ->
            val angle = index * angleStep - Math.PI / 2
            val r = radius * score.score.coerceIn(0.15f, 1f)
            val x = center.x + r * cos(angle).toFloat()
            val y = center.y + r * sin(angle).toFloat()
            if (index == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
        }
        dataPath.close()

        drawPath(dataPath, accent.copy(alpha = 0.2f))
        drawPath(dataPath, accent, style = Stroke(width = 2.dp.toPx()))

        // Draw domain labels at each vertex
        data.forEachIndexed { i, ds ->
            val angle = i * angleStep - Math.PI / 2
            val labelR = radius + 20.dp.toPx()
            val lx = center.x + labelR * cos(angle).toFloat()
            val ly = center.y + labelR * sin(angle).toFloat()
            val textLayoutResult = textMeasurer.measure(
                text = AnnotatedString(ds.domain.displayNameEn),
                style = TextStyle(color = GhostWhite.copy(alpha = 0.7f), fontSize = 10.sp)
            )
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(lx - textLayoutResult.size.width / 2f, ly - textLayoutResult.size.height / 2f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultTraitsSection(title: String, traits: List<String>, accent: Color, isPositive: Boolean, emptyText: String = if (isPositive) "Analyzing..." else "No major weaknesses") {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, color = accent, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (traits.isEmpty()) {
                Text(
                    text = emptyText,
                    color = GhostWhite.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            } else {
                traits.forEach { trait ->
                    Surface(
                        color = accent.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            trait,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = accent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResultExplanationCard(question: String, explanation: String, accent: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, GhostWhite.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = question,
                style = MaterialTheme.typography.titleSmall,
                color = accent,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = GhostWhite.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
        }
    }
}
