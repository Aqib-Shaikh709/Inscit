package com.example.inscit.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.inscit.models.CustomTheme
import com.example.inscit.models.Lang
import com.example.inscit.ui.theme.*

object CustomThemeManager {
    private const val PREFS_NAME = "custom_themes_prefs"
    private const val KEY_THEMES = "saved_themes"
    private const val KEY_SELECTED_CUSTOM = "selected_custom_theme_name"

    fun saveThemes(context: Context, themes: List<CustomTheme>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val data = themes.joinToString("||") { "${it.name}|${it.primaryAccent}|${it.background}|${it.textColor}" }
        prefs.edit().putString(KEY_THEMES, data).apply()
    }

    fun loadThemes(context: Context): List<CustomTheme> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val data = prefs.getString(KEY_THEMES, "") ?: ""
        if (data.isEmpty()) return emptyList()
        return data.split("||").mapNotNull {
            val parts = it.split("|")
            if (parts.size == 4) {
                try {
                    CustomTheme(parts[0], parts[1].toInt(), parts[2].toInt(), parts[3].toInt())
                } catch (_: NumberFormatException) {
                    null
                }
            } else null
        }
    }

    fun saveSelectedCustomThemeName(context: Context, name: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SELECTED_CUSTOM, name).apply()
    }

    fun getSelectedCustomThemeName(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SELECTED_CUSTOM, null)
    }

    fun deleteTheme(context: Context, themeName: String) {
        val themes = loadThemes(context).toMutableList()
        themes.removeAll { it.name == themeName }
        saveThemes(context, themes)
        if (getSelectedCustomThemeName(context) == themeName) {
            saveSelectedCustomThemeName(context, "")
        }
    }
}

@Composable
fun ColorPickerOverlay(
    lang: Lang,
    initialAccent: Color,
    initialBg: Color,
    onDismiss: () -> Unit,
    onApply: (CustomTheme) -> Unit
) {
    val context = LocalContext.current
    var themeName by remember { mutableStateOf("") }
    var accentHsv by remember { 
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(initialAccent.toArgb(), hsv)
        mutableStateOf(hsv)
    }
    
    // Simplified: Background is derived from accent (darker version) or kept static
    // Based on "overwhelming" feedback, let's focus on the Accent and auto-generate BG/Text
    val currentAccent = Color(android.graphics.Color.HSVToColor(accentHsv))
    val currentBg = Color(android.graphics.Color.HSVToColor(floatArrayOf(accentHsv[0], accentHsv[1].coerceAtMost(0.3f), 0.15f)))
    val currentTxt = Color.White

    var showOverwriteDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1A1C1E),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (lang == Lang.EN) "CREATE CUSTOM THEME" else "कस्टम थीम बनाएं",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )

                Spacer(Modifier.height(24.dp))

                // Preview Area
                Surface(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = currentBg,
                    border = BorderStroke(2.dp, currentAccent)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("PREVIEW / पूर्वावलोकन", color = currentTxt, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {},
                                colors = ButtonDefaults.buttonColors(containerColor = currentAccent),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("ACCENT", color = if (currentAccent.luminance() > 0.5f) Color.Black else Color.White)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                ThemeTextField(
                    value = themeName,
                    onValueChange = { themeName = it },
                    label = if (lang == Lang.EN) "THEME NAME" else "थीम का नाम",
                    accent = currentAccent
                )

                Spacer(Modifier.height(24.dp))

                // Visual Color Picker
                SaturationValueBox(
                    hue = accentHsv[0],
                    saturation = accentHsv[1],
                    value = accentHsv[2],
                    onSelection = { s, v -> accentHsv = floatArrayOf(accentHsv[0], s, v) }
                )
                
                Spacer(Modifier.height(16.dp))
                
                HueBar(
                    hue = accentHsv[0],
                    onHueChange = { accentHsv = floatArrayOf(it, accentHsv[1], accentHsv[2]) }
                )

                Spacer(Modifier.height(40.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Text(if (lang == Lang.EN) "CANCEL" else "रद्द करें", color = Color.White)
                    }
                    Button(
                        onClick = {
                            if (themeName.isNotBlank()) {
                                val existingThemes = CustomThemeManager.loadThemes(context)
                                if (existingThemes.any { it.name.equals(themeName, ignoreCase = true) }) {
                                    showOverwriteDialog = true
                                } else {
                                    onApply(CustomTheme(themeName, currentAccent.toArgb(), currentBg.toArgb(), currentTxt.toArgb()))
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = currentAccent),
                        enabled = themeName.isNotBlank()
                    ) {
                        Text(
                            if (lang == Lang.EN) "SAVE" else "सहेजें",
                            color = if (currentAccent.luminance() > 0.5f) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showOverwriteDialog) {
        AlertDialog(
            onDismissRequest = { showOverwriteDialog = false },
            title = { Text(if (lang == Lang.EN) "Theme Exists" else "थीम पहले से मौजूद है") },
            text = { Text(if (lang == Lang.EN) "A theme with this name already exists. Do you want to replace it?" else "इस नाम की थीम पहले से मौजूद है। क्या आप इसे बदलना चाहते हैं?") },
            confirmButton = {
                TextButton(onClick = {
                    showOverwriteDialog = false
                    onApply(CustomTheme(themeName, currentAccent.toArgb(), currentBg.toArgb(), currentTxt.toArgb()))
                }) {
                    Text(if (lang == Lang.EN) "REPLACE" else "बदलें")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverwriteDialog = false }) {
                    Text(if (lang == Lang.EN) "CANCEL" else "रद्द करें")
                }
            }
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SaturationValueBox(hue: Float, saturation: Float, value: Float, onSelection: (Float, Float) -> Unit) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
            .clip(RoundedCornerShape(12.dp))
            .pointerInput(hue) {
                detectDragGestures(onDrag = { change, _ ->
                    val s = (change.position.x / size.width).coerceIn(0f, 1f)
                    val v = 1f - (change.position.y / size.height).coerceIn(0f, 1f)
                    onSelection(s, v)
                })
            }
            .pointerInput(hue) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val pos = event.changes.first().position
                        val s = (pos.x / size.width).coerceIn(0f, 1f)
                        val v = 1f - (pos.y / size.height).coerceIn(0f, 1f)
                        onSelection(s, v)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background Hue
            drawRect(color = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f))))
            
            // Saturation gradient (white to transparent)
            drawRect(
                brush = Brush.horizontalGradient(listOf(Color.White, Color.Transparent))
            )
            
            // Value gradient (transparent to black)
            drawRect(
                brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black))
            )
            
            // Selection cursor
            val cursorX = saturation * size.width
            val cursorY = (1f - value) * size.height
            drawCircle(
                color = Color.White,
                radius = 8.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(cursorX, cursorY),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun HueBar(hue: Float, onHueChange: (Float) -> Unit) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectDragGestures(onDrag = { change, _ ->
                    val h = (change.position.x / size.width).coerceIn(0f, 1f) * 360f
                    onHueChange(h)
                })
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val pos = event.changes.first().position
                        val h = (pos.x / size.width).coerceIn(0f, 1f) * 360f
                        onHueChange(h)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val colors = List(361) { Color(android.graphics.Color.HSVToColor(floatArrayOf(it.toFloat(), 1f, 1f))) }
            drawRect(brush = Brush.horizontalGradient(colors))
            
            val cursorX = (hue / 360f) * size.width
            drawRect(
                color = Color.White,
                topLeft = androidx.compose.ui.geometry.Offset(cursorX - 2.dp.toPx(), 0f),
                size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height)
            )
        }
    }
}

@Composable
fun ThemeTextField(value: String, onValueChange: (String) -> Unit, label: String, accent: Color) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(8.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(16.dp),
            cursorBrush = Brush.verticalGradient(listOf(accent, accent))
        )
    }
}
