package com.example.inscit.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

// App-wide motion language: every screen enters the same way (gentle rise + fade),
// list rows stagger behind it. One place to tune timing for the whole app.
object MotionKit {
    const val ENTER_MS = 350
    const val EXIT_MS = 200
    const val STAGGER_STEP_MS = 60
    const val PRESS_MS = 120

    fun enterSpec(): androidx.compose.animation.EnterTransition =
        fadeIn(tween(ENTER_MS)) + slideInVertically(tween(ENTER_MS)) { it / 8 }
    fun exitSpec(): androidx.compose.animation.ExitTransition = fadeOut(tween(EXIT_MS))
}

// Wrap a screen's root content: fades + rises in once on entry.
// Apply at the navigation branches in AppEngine so all screens share the motion.
@Composable
fun ScreenEnter(
    delayMs: Int = 0,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (delayMs > 0) delay(delayMs.toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = MotionKit.enterSpec(),
        exit = MotionKit.exitSpec(),
        label = "screenEnter"
    ) {
        content()
    }
}

// Staggered list/card entrance: pass the item index, rows cascade in.
@Composable
fun StaggerItem(
    index: Int,
    stepMs: Int = MotionKit.STAGGER_STEP_MS,
    content: @Composable () -> Unit
) {
    ScreenEnter(delayMs = index * stepMs, content = content)
}
