package com.example.inscit.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

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
