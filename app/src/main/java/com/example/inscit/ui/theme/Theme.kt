package com.example.inscit.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Spacing(
    val default: Dp = 0.dp,
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val huge: Dp = 48.dp,
    val massive: Dp = 64.dp
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }

val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFA726), // Warm Amber
    secondary = Color(0xFFEF6C00), // Warm Orange
    tertiary = Color(0xFFFFE082), // Warm Cream
    background = Color(0xFF0A0B0E), // Deep Warm Space
    surface = Color(0xFF1A1C1E), // Card Bg
    onPrimary = Color(0xFF0A0B0E),
    onSecondary = Color.White,
    onTertiary = Color(0xFF0A0B0E),
    onBackground = Color(0xFFF5F5F5), // Warm Ghost White
    onSurface = Color(0xFFF5F5F5),
    error = Color(0xFFFF8A65) // Warm Coral
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFF57C00), // Warm Orange
    secondary = Color(0xFFE65100), // Deep Warm Orange
    tertiary = Color(0xFFFFF3E0), // Warm Cream Light
    background = Color(0xFFFFF8F1), // Warm Off White
    surface = Color(0xFFFFF3E0),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color(0xFF3E2723),
    onBackground = Color(0xFF3E2723),
    onSurface = Color(0xFF3E2723)
)

@Composable
fun InscitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}