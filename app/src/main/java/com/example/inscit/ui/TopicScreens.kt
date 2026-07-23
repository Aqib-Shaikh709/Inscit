package com.example.inscit.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inscit.*
import com.example.inscit.models.Lang
import com.example.inscit.models.TopicDetail
import com.example.inscit.models.UserNote
import com.example.inscit.syllabus.TopicSyllabus
import kotlin.math.*

import com.example.inscit.ui.theme.spacing

@Composable
fun TopicSelectionScreen(
    branch: Branch,
    lang: Lang,
    tts: TTSManager,
    accent: Color,
    txtCol: Color,
    onBack: () -> Unit,
    onTopicClick: (TopicDetail) -> Unit,
    onLangChange: (Lang) -> Unit = {}
) {
    val topics = remember(branch, lang) { TopicSyllabus.getTopics(branch, lang) }
    val spacing = MaterialTheme.spacing
    val branchName = if (lang == Lang.EN) branch.name else when(branch) {
        Branch.PHYSICS -> "भौतिकी"
        Branch.CHEMISTRY -> "रसायन विज्ञान"
        Branch.BIOLOGY -> "जीव विज्ञान"
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val horizontalPadding = if (screenWidth > 600.dp) spacing.huge else spacing.large

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding, vertical = spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { BackIcon(color = txtCol) }
                Text("$branchName TOPICS", style = MaterialTheme.typography.headlineSmall, color = txtCol, letterSpacing = 2.sp)
            }

            Spacer(Modifier.height(spacing.huge))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = CardBg.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, GhostWhite.copy(alpha = 0.05f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically, 
                    modifier = Modifier.padding(spacing.medium)
                ) {
                    Text(
                        if (lang == Lang.EN) "Select a specialized module to explore."
                        else "अन्वेषण करने के लिए एक विशेष मॉड्यूल चुनें।",
                        color = txtCol.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.weight(1f)
                    )
                    val selectText = if (lang == Lang.EN) "Select a specialized module to explore detailed scientific concepts and interactive simulations."
                    else "विस्तृत वैज्ञानिक अवधारणाओं और इंटरैक्टिव सिमुलेशन का पता लगाने के लिए एक विशेष मॉड्यूल चुनें।"
                    TtsController(selectText, tts, accent, iconSize = 24.dp)
                    Spacer(Modifier.width(4.dp))
                    LangToggleButton(currentLang = lang, accent = accent, onToggle = onLangChange)
                }
            }

            Spacer(Modifier.height(spacing.large))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(spacing.small),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = spacing.large)
            ) {
                items(topics) { topic ->
                    Surface(
                        onClick = { onTopicClick(topic) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = CardBg,
                        border = BorderStroke(1.dp, GhostWhite.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier.padding(spacing.large),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(accent.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    (topics.indexOf(topic) + 1).toString(),
                                    color = accent,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(Modifier.width(spacing.medium))
                            Text(
                                topic.title,
                                color = GhostWhite,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.weight(1f))
                            Text("→", color = GhostWhite.copy(alpha = 0.2f), style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopicDetailScreen(
    topic: TopicDetail,
    lang: Lang,
    tts: TTSManager,
    accent: Color,
    txtCol: Color,
    userNote: UserNote,
    onNoteChange: (UserNote) -> Unit,
    onBack: () -> Unit,
    onLabClick: () -> Unit,
    onLangChange: (Lang) -> Unit = {}
) {
    var showNotes by remember { mutableStateOf(false) }
    val spacing = MaterialTheme.spacing

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val horizontalPadding = if (screenWidth > 600.dp) spacing.huge else spacing.large

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding, vertical = spacing.large)
                .then(if (!showNotes) Modifier.verticalScroll(rememberScrollState()) else Modifier),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (showNotes) showNotes = false else onBack()
                }) { BackIcon(color = txtCol) }
                Text(
                    if (showNotes) (if (lang == Lang.EN) "OBSERVATIONS" else "ऑब्जर्वेशन") else topic.title.uppercase(),
                    style = MaterialTheme.typography.titleLarge, color = txtCol, letterSpacing = 1.sp,
                    modifier = Modifier.weight(1f)
                )
                LangToggleButton(currentLang = lang, accent = accent, onToggle = onLangChange)
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = { showNotes = !showNotes }) {
                    DrawingIcon(color = if (showNotes) accent else txtCol.copy(alpha = 0.5f))
                }
            }

            Spacer(Modifier.height(spacing.extraLarge))

            if (showNotes) {
                UserObservationSection(
                    branch = topic.title,
                    userNote = userNote,
                    onNoteChange = onNoteChange,
                    accent = accent,
                    txtCol = txtCol,
                    fullSpace = true,
                    lang = lang
                )
            } else {
                // Topic-Specific Interactive Diagram
                InteractiveTopicDiagram(topic, accent)

                Spacer(Modifier.height(spacing.huge))

                topic.paragraphs.forEachIndexed { index, para ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = spacing.small),
                        shape = RoundedCornerShape(24.dp),
                        color = CardBg,
                        border = BorderStroke(1.dp, GhostWhite.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(spacing.large)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    if (lang == Lang.EN) "CONCEPT ${index + 1}" else "अवधारणा ${index + 1}",
                                    color = accent,
                                    style = MaterialTheme.typography.labelMedium,
                                    letterSpacing = 2.sp
                                )
                                TtsController(para, tts, accent, iconSize = 24.dp)
                            }
                            Spacer(Modifier.height(spacing.medium))
                            Text(
                                para,
                                color = GhostWhite.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodyLarge,
                                lineHeight = 26.sp
                            )
                        }
    }
}

@Composable
fun LangToggleButton(currentLang: Lang, accent: Color, modifier: Modifier = Modifier, onToggle: (Lang) -> Unit) {
    val nextLang = if (currentLang == Lang.EN) Lang.HI else Lang.EN
    val label = if (currentLang == Lang.EN) "HI" else "EN"
    Surface(
        onClick = { onToggle(nextLang) },
        modifier = modifier.size(36.dp),
        shape = RoundedCornerShape(8.dp),
        color = accent.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.3f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accent)
        }
    }
}

                Spacer(Modifier.height(spacing.extraLarge))
                
                Button(
                    onClick = onLabClick,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent.copy(alpha = 0.12f), contentColor = accent),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.3f))
                ) {
                    Text(if (lang == Lang.EN) "ENTER RESEARCH LAB" else "अनुसंधान लैब में प्रवेश करें", style = MaterialTheme.typography.titleMedium)
                }
                
                Spacer(Modifier.height(spacing.huge))
            }
        }
    }
}


