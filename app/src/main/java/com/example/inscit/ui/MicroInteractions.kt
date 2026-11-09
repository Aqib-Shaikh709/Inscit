package com.example.inscit.ui

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.inscit.ui.theme.*
import com.example.inscit.ui.theme.WarmAmber
import com.example.inscit.ui.theme.WarmGhostWhite

// ═════════════════════════════════════════════════════════════════════════════════
//  METALLIC GRADIENT BRUSHES (from Colors.kt)
// ═════════════════════════════════════════════════════════════════════════════════

private val metallicGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1C1C1E),
        Color(0xFF2A2A2E),
        Color(0xFF3A3A42),
        Color(0xFF2A2A2E),
        Color(0xFF1C1C1E)
    ),
    start = Offset(0f, 0f),
    end = Offset(1f, 0f)
)

private val metallicBorderGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF4A4A50),
        Color(0xFF6A6A70),
        Color(0xFF8A8A90),
        Color(0xFF6A6A70),
        Color(0xFF4A4A50)
    ),
    start = Offset(0f, 0f),
    end = Offset(1f, 0f)
)

private val shineGradient = Brush.linearGradient(
    colors = listOf(
        Color(0x00FFFFFF),
        Color(0x22FFFFFF),
        Color(0x00FFFFFF)
    ),
    start = Offset(-0.3f, -0.3f),
    end = Offset(0.7f, 0.7f)
)

// ═════════════════════════════════════════════════════════════════════════════════
//  PRESS SCALE ANIMATION
// ═════════════════════════════════════════════════════════════════════════════════

enum class PressDepth(val scale: Float) { BUTTON(0.94f), ICON(0.88f), CARD(0.96f) }

@Composable
private fun pressScale(source: MutableInteractionSource, depth: PressDepth): Modifier {
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) depth.scale else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "pressScale"
    )
    return Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
}

// ═════════════════════════════════════════════════════════════════════════════════
//  METALLIC BEVEL — raised-metal edge (top-left light, bottom-right dark)
// ═════════════════════════════════════════════════════════════════════════════════

private fun DrawScope.bevelBorder(
    width: Dp = 1.dp,
    light: Color = MetalBevelLight,
    dark: Color = MetalBevelDark,
    shape: Shape = androidx.compose.ui.graphics.RectangleShape
) {
    val w = width.toPx()
    // top-left light rim
    drawLine(light, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = w)
    drawLine(light, Offset(0f, 0f), Offset(0f, size.height), strokeWidth = w)
    // bottom-right dark rim
    drawLine(dark, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = w)
    drawLine(dark, Offset(size.width, 0f), Offset(size.width, size.height), strokeWidth = w)
}

// ═════════════════════════════════════════════════════════════════════════════════
//  SHINE SWEEP — animated glancing light across a surface
// ═════════════════════════════════════════════════════════════════════════════════

@Composable
fun ShineSweep(
    modifier: Modifier = Modifier,
    intensity: Float = 0.35f,
    durationMs: Long = 3000,
    clip: Shape = androidx.compose.ui.graphics.RectangleShape
) {
    val transition = rememberInfiniteTransition(label = "shineSweep")
    val anim by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = durationMs.toInt(), easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "shine"
    )
    Box(
        modifier = modifier
            .clip(clip)
            .drawBehind {
                val brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x00FFFFFF),
                        Color(0xFFFFFFFF).copy(alpha = intensity),
                        Color(0x00FFFFFF)
                    ),
                    start = Offset(x = anim - 1f, y = -0.5f),
                    end = Offset(x = anim, y = 0.5f)
                )
                drawRect(brush = brush)
            }
    )
}

// ═════════════════════════════════════════════════════════════════════════════════
//  METALLIC CARD — bento card with bevel, gradient fill, optional shine
// ═════════════════════════════════════════════════════════════════════════════════

@Composable
fun MetallicCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    color: Color = WarmSurface,
    border: androidx.compose.foundation.BorderStroke? = BorderStroke(1.dp, MetalEdge),
    shine: Boolean = true,
    pressDepth: PressDepth = PressDepth.CARD,
    content: @Composable () -> Unit
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressDepth.scale else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "metallicCardPress"
    )

    val cardModifier = modifier
        .then(if (enabled && onClick != null) {
            Modifier.clickable(
                interactionSource = source,
                indication = null,
                onClick = { onClick() }
            )
        } else Modifier)
        .then(Modifier.graphicsLayer(scaleX = scale, scaleY = scale))
        .clip(shape)

    Box(modifier = cardModifier, contentAlignment = Alignment.Center) {
        // Base metal fill (brushed gradient over the surface color)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color)
                .drawBehind {
                    drawRect(brush = metallicGradient)
                }
                .clip(shape)
        )

        // Bevel edges (raised metal)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    bevelBorder(width = 1.dp, light = MetalBevelLight, dark = MetalBevelDark, shape = shape)
                }
                .clip(shape)
        )

        // Optional chrome border
        if (border != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(border, shape)
                    .clip(shape)
            )
        }

        // Sweeping shine overlay
        if (shine) {
            ShineSweep(
                modifier = Modifier.fillMaxSize().clip(shape),
                intensity = if (pressed) 0.15f else 0.3f,
                durationMs = 3200,
                clip = shape
            )
        }

        content()
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
//  SHINY BUTTON — widget-style brushed metal gradient with chrome bevel
//  Uses the same MetalGradientBrush as the home-screen widgets for a cohesive
//  shiny metallic look across buttons and cards. Bold, full-height, prominent.
//  Pass an optional gradientBrush for theme-complementary colors (quiz, settings).
// ═════════════════════════════════════════════════════════════════════════════════

@Composable
fun ShinyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    gradientBrush: Brush? = null,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val brush = gradientBrush ?: MetalGradientBrush
    Box(
        modifier = modifier.then(pressScale(source, PressDepth.BUTTON)),
        contentAlignment = Alignment.Center
    ) {
        // Widget-style brushed metal gradient background
        Box(
            modifier = Modifier.fillMaxWidth().height(64.dp)
                .background(brush).clip(shape)
        )
        // Bevel edges (raised metal)
        Box(
            modifier = Modifier
                .fillMaxWidth().height(64.dp)
                .drawBehind {
                    bevelBorder(width = 1.dp, light = MetalBevelLight, dark = MetalBevelDark, shape = shape)
                }
                .clip(shape)
        )
        // Sweeping shine overlay
        ShineSweep(
            modifier = Modifier.fillMaxWidth().height(64.dp).clip(shape),
            intensity = if (pressed) 0.1f else 0.25f,
            durationMs = 2800,
            clip = shape
        )
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            interactionSource = source,
            content = content
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
//  METALLIC BUTTONS
// ═════════════════════════════════════════════════════════════════════════════════

@Composable
fun MetallicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    Box(
        modifier = modifier.then(pressScale(source, PressDepth.BUTTON)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(48.dp)
                .background(metallicGradient).clip(shape)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth().height(48.dp)
                .background(shineGradient).clip(shape)
                .alpha(if (pressed) 0.1f else 0.35f)
        )
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            border = BorderStroke(1.dp, MetalEdge),
            interactionSource = source,
            content = content
        )
    }
}

@Composable
fun MetallicOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    Box(
        modifier = modifier.then(pressScale(source, PressDepth.BUTTON)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth().height(48.dp)
                .background(metallicBorderGradient).clip(shape)
                .alpha(if (pressed) 0.15f else 0f)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth().height(48.dp)
                .border(BorderStroke(1.5.dp, MetalEdge), shape)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth().height(48.dp)
                .background(shineGradient).clip(shape)
                .alpha(if (pressed) 0.05f else 0.2f)
        )
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            border = BorderStroke(1.5.dp, MetalEdge),
            interactionSource = source,
            content = content
        )
    }
}

@Composable
fun MetallicTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    val source = remember { MutableInteractionSource() }
    TextButton(
        onClick = onClick,
        modifier = modifier.then(pressScale(source, PressDepth.BUTTON)),
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            containerColor = Color.Transparent,
            contentColor = WarmAmber
        ),
        interactionSource = source,
        content = content
    )
}

// ═════════════════════════════════════════════════════════════════════════════════
//  PRESSABLE BUTTONS — metallic by default (existing APIs preserved)
// ═════════════════════════════════════════════════════════════════════════════════

@Composable
fun PressableButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    border: BorderStroke? = null,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    Box(
        modifier = modifier.then(pressScale(source, PressDepth.BUTTON)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(metallicGradient).clip(shape)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(shineGradient).clip(shape)
                .alpha(if (pressed) 0.15f else 0.3f)
        )
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            border = BorderStroke(1.dp, MetalEdge),
            interactionSource = source,
            content = content
        )
    }
}

@Composable
fun PressableOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    border: BorderStroke? = null,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    Box(
        modifier = modifier.then(pressScale(source, PressDepth.BUTTON)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(metallicBorderGradient).clip(shape)
                .alpha(if (pressed) 0.15f else 0f)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(border ?: BorderStroke(1.5.dp, MetalEdge), shape)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(shineGradient).clip(shape)
                .alpha(if (pressed) 0.1f else 0.25f)
        )
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            border = BorderStroke(1.5.dp, MetalEdge),
            interactionSource = source,
            content = content
        )
    }
}

@Composable
fun PressableTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    val source = remember { MutableInteractionSource() }
    TextButton(
        onClick = onClick,
        modifier = modifier.then(pressScale(source, PressDepth.BUTTON)),
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            containerColor = Color.Transparent,
            contentColor = WarmAmber
        ),
        interactionSource = source,
        content = content
    )
}

@Composable
fun PressableIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    content: @Composable () -> Unit
) {
    val source = remember { MutableInteractionSource() }
    IconButton(
        onClick = onClick,
        modifier = modifier.then(pressScale(source, PressDepth.ICON)),
        enabled = enabled,
        colors = colors,
        interactionSource = source,
        content = content
    )
}

@Composable
fun PressableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    color: Color = Color.Unspecified,
    border: BorderStroke? = null,
    content: @Composable () -> Unit
) {
    val source = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = modifier.then(pressScale(source, PressDepth.CARD)).clip(shape),
        enabled = enabled,
        shape = shape,
        color = color,
        border = border,
        interactionSource = source,
        content = content
    )
}

@Composable
fun rememberPressScale(depth: Float = 0.95f): Pair<Modifier, MutableInteractionSource> {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) depth else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "pressScaleExt"
    )
    return Modifier.graphicsLayer(scaleX = scale, scaleY = scale) to source
}
