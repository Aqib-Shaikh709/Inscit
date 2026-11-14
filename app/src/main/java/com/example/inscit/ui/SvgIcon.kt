package com.example.inscit.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Renders an SVG icon from `src/main/assets/icons/...` using Coil.
 *
 * Usage:
 *   SvgIcon("ranks/rank_ champ.svg", modifier = Modifier.size(24.dp))
 *
 * [assetPath] is relative to `assets/icons/` (e.g. "ranks/rank_ champ.svg").
 */
@Composable
fun SvgIcon(
    assetPath: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val context = LocalContext.current

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data("asset:///icons/$assetPath")
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
    )
}
