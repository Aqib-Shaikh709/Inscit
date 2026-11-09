package com.example.inscit.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.inscit.ui.theme.MetalEdge
import com.example.inscit.ui.theme.WarmAmber

// Micro-interaction language, matched to MotionKit: an obvious-but-minimal dip on
// press with a soft spring back (gentle overshoot = bounce feel, no slideshow).
// Drop-in replacements forward their own press InteractionSource, so ripples keep
// working and clicks are untouched.
private enum class PressDepth(val scale: Float) { BUTTON(0.93f), ICON(0.85f), CARD(0.95f) }

@Composable
private fun pressScale(source: MutableInteractionSource, depth: PressDepth): Modifier {
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) depth.scale else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 500f),
        label = "pressScale"
    )
    return Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
}

/**
 * Metallic gradient brush used for button backgrounds.
 * Simulates brushed metal: dark edges, bright center highlight.
 */
private val metallicGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1C1C1E), // dark left edge
        Color(0xFF2A2A2E), // mid left
        Color(0xFF3A3A42), // center highlight
        Color(0xFF2A2A2E), // mid right
        Color(0xFF1C1C1E)  // dark right edge
    ),
    start = Offset(0f, 0f),
    end = Offset(1f, 0f)
)

/**
 * Metallic border gradient for outlined buttons.
 */
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

/**
 * Shine overlay brush: diagonal white-transparent gradient that gives the metal
 * a "shine" — simulated light glancing off the surface.
 */
private val shineGradient = Brush.linearGradient(
    colors = listOf(
        Color(0x00FFFFFF), // transparent
        Color(0x22FFFFFF), // subtle shine
        Color(0x00FFFFFF)  // transparent
    ),
    start = Offset(-0.3f, -0.3f),
    end = Offset(0.7f, 0.7f)
)

/**
 * Metallic button: brushed-metal gradient background + diagonal shine overlay.
 * The metal gradient and shine sit behind a transparent Material Button so the
 * content lambda keeps its RowScope receiver.
 */
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
        // Metallic gradient background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(metallicGradient)
                .clip(shape)
        )
        // Shine overlay — subtle diagonal highlight over metal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(shineGradient)
                .clip(shape)
                .alpha(if (pressed) 0.15f else 0.3f)
        )
        // Transparent button for interaction + content
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

/**
 * Metallic outlined button: metallic gradient border + shine, transparent fill.
 */
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
        // Metallic border gradient fill (subtle)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(metallicBorderGradient)
                .clip(shape)
                .alpha(if (pressed) 0.15f else 0f)
        )
        // Border stroke
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(BorderStroke(1.5.dp, MetalEdge), shape)
        )
        // Shine overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(shineGradient)
                .clip(shape)
                .alpha(if (pressed) 0.1f else 0.25f)
        )
        // Transparent outlined button for interaction + content
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

/**
 * Metallic text button: text-only with metallic accent color.
 */
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
    Button(
        onClick = onClick,
        modifier = modifier.then(pressScale(source, PressDepth.BUTTON)),
        enabled = enabled,
        shape = shape,
        colors = colors,
        border = border,
        interactionSource = source,
        content = content
    )
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
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.then(pressScale(source, PressDepth.BUTTON)),
        enabled = enabled,
        shape = shape,
        colors = colors,
        border = border,
        interactionSource = source,
        content = content
    )
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
        modifier = modifier
            .then(pressScale(source, PressDepth.CARD))
            .clip(shape),
        enabled = enabled,
        shape = shape,
        color = color,
        border = border,
        interactionSource = source,
        content = content
    )
}

// For raw Modifier.clickable sites (profile photo, text links, custom pills):
// observes the same press source the clickable reports to, so scale and ripple stay in sync.
@Composable
fun rememberPressScale(depth: Float = 0.95f): Pair<Modifier, MutableInteractionSource> {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) depth else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 500f),
        label = "pressScaleExt"
    )
    return Modifier.graphicsLayer(scaleX = scale, scaleY = scale) to source
}
