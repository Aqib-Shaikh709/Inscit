package com.example.inscit.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun HomeIcon(color: Color = Color.White, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.15f, h * 0.45f)
            lineTo(w * 0.5f, h * 0.15f)
            lineTo(w * 0.85f, h * 0.45f)
            lineTo(w * 0.85f, h * 0.85f)
            lineTo(w * 0.6f, h * 0.85f)
            lineTo(w * 0.6f, h * 0.6f)
            lineTo(w * 0.4f, h * 0.6f)
            lineTo(w * 0.4f, h * 0.85f)
            lineTo(w * 0.15f, h * 0.85f)
            close()
        }
        drawPath(path, color)
    }
}

@Composable
fun SettingsIcon(color: Color = Color.White, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val center = Offset(x = size.width / 2, y = size.height / 2)
        drawCircle(color, radius = size.width * 0.2f, center = center)
        repeat(8) { i ->
            val angle = (i * 45f) * (Math.PI / 180f).toFloat()
            val start = Offset(
                x = center.x + (size.width * 0.25f) * Math.cos(angle.toDouble()).toFloat(),
                y = center.y + (size.width * 0.25f) * Math.sin(angle.toDouble()).toFloat()
            )
            val end = Offset(
                x = center.x + (size.width * 0.45f) * Math.cos(angle.toDouble()).toFloat(),
                y = center.y + (size.width * 0.45f) * Math.sin(angle.toDouble()).toFloat()
            )
            drawLine(color, start, end, strokeWidth = 6f, cap = StrokeCap.Round)
        }
    }
}

@Composable
fun SearchIcon(color: Color = Color.White, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(color, radius = w * 0.3f, center = Offset(w * 0.4f, h * 0.4f), style = Stroke(width = 4f))
        drawLine(color, Offset(w * 0.65f, h * 0.65f), Offset(w * 0.85f, h * 0.85f), strokeWidth = 4f, cap = StrokeCap.Round)
    }
}

@Composable
fun NotificationIcon(color: Color = Color.White, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.15f)
            quadraticTo(w * 0.75f, h * 0.15f, w * 0.75f, h * 0.5f)
            lineTo(w * 0.85f, h * 0.75f)
            lineTo(w * 0.15f, h * 0.75f)
            lineTo(w * 0.25f, h * 0.5f)
            quadraticTo(w * 0.25f, h * 0.15f, w * 0.5f, h * 0.15f)
        }
        drawPath(path, color)
        drawCircle(color, radius = 4f, center = Offset(w * 0.5f, h * 0.85f))
    }
}

@Composable
fun StarIcon(color: Color = Color.Yellow, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.1f)
            lineTo(w * 0.62f, h * 0.38f)
            lineTo(w * 0.92f, h * 0.38f)
            lineTo(w * 0.68f, h * 0.57f)
            lineTo(w * 0.77f, h * 0.86f)
            lineTo(w * 0.5f, h * 0.68f)
            lineTo(w * 0.23f, h * 0.86f)
            lineTo(w * 0.32f, h * 0.57f)
            lineTo(w * 0.08f, h * 0.38f)
            lineTo(w * 0.38f, h * 0.38f)
            close()
        }
        drawPath(path, color)
    }
}

@Composable
fun BuildIcon(color: Color = Color.Gray, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawLine(color, Offset(w * 0.2f, h * 0.8f), Offset(w * 0.7f, h * 0.3f), strokeWidth = 8f, cap = StrokeCap.Round)
        drawCircle(color, radius = w * 0.2f, center = Offset(w * 0.75f, h * 0.25f), style = Stroke(width = 4f))
    }
}

@Composable
fun PlayIcon(color: Color = Color.Green, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.3f, h * 0.2f)
            lineTo(w * 0.8f, h * 0.5f)
            lineTo(w * 0.3f, h * 0.8f)
            close()
        }
        drawPath(path, color)
    }
}

@Composable
fun InfoIcon(color: Color = Color.Blue, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        drawCircle(color, radius = size.width * 0.45f, center = center, style = Stroke(width = 2.dp.toPx()))
        drawCircle(color, radius = 2f, center = Offset(center.x, center.y - 6f))
        drawLine(color, Offset(center.x, center.y - 2f), Offset(center.x, center.y + 10f), strokeWidth = 4f, cap = StrokeCap.Round)
    }
}

@Composable
fun RefreshIcon(color: Color = Color.Cyan, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        drawCircle(color, radius = size.width * 0.35f, center = center, style = Stroke(width = 4f))
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.05f)
            lineTo(w * 0.7f, h * 0.15f)
            lineTo(w * 0.5f, h * 0.25f)
        }
        drawPath(path, color)
    }
}

@Composable
fun FlaskIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.35f, h * 0.1f)
            lineTo(w * 0.65f, h * 0.1f)
            lineTo(w * 0.65f, h * 0.4f)
            lineTo(w * 0.9f, h * 0.85f)
            quadraticTo(w * 0.95f, h * 0.95f, w * 0.85f, h * 0.95f)
            lineTo(w * 0.15f, h * 0.95f)
            quadraticTo(w * 0.05f, h * 0.95f, w * 0.1f, h * 0.85f)
            lineTo(w * 0.35f, h * 0.4f)
            close()
        }
        drawPath(path, color, style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
fun DNAIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        for (i in 0 until 5) {
            val y = h * (0.2f + i * 0.15f)
            val offset1 = Offset(w * 0.25f, y)
            val offset2 = Offset(w * 0.75f, y + h * 0.1f)
            drawCircle(color, radius = 4f, center = offset1)
            drawCircle(color, radius = 4f, center = offset2)
            drawLine(color, offset1, offset2, strokeWidth = 2f)
        }
    }
}

@Composable
fun AtomIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        drawCircle(color, radius = 4f, center = center)
        drawCircle(color, radius = size.width / 2.5f, center = center, style = Stroke(width = 2f))
        drawCircle(color, radius = size.width / 4f, center = center, style = Stroke(width = 2f))
    }
}

@Composable
fun MicroscopeIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.2f, h * 0.9f)
            lineTo(w * 0.8f, h * 0.9f)
            moveTo(w * 0.5f, h * 0.9f)
            lineTo(w * 0.5f, h * 0.7f)
            lineTo(w * 0.3f, h * 0.7f)
            lineTo(w * 0.3f, h * 0.2f)
            lineTo(w * 0.4f, h * 0.1f)
            lineTo(w * 0.6f, h * 0.3f)
            lineTo(w * 0.5f, h * 0.4f)
        }
        drawPath(path, color, style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
fun BrainIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(color, radius = w * 0.3f, center = Offset(w * 0.35f, h * 0.45f), style = Stroke(width = 2f))
        drawCircle(color, radius = w * 0.3f, center = Offset(w * 0.65f, h * 0.45f), style = Stroke(width = 2f))
        drawCircle(color, radius = w * 0.25f, center = Offset(w * 0.5f, h * 0.7f), style = Stroke(width = 2f))
    }
}

@Composable
fun TargetIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        drawCircle(color, radius = size.width / 2f, center = center, style = Stroke(width = 2f))
        drawCircle(color, radius = size.width / 4f, center = center, style = Stroke(width = 2f))
        drawLine(color, Offset(center.x - 10, center.y), Offset(center.x + 10, center.y), strokeWidth = 2f)
        drawLine(color, Offset(center.x, center.y - 10), Offset(center.x, center.y + 10), strokeWidth = 2f)
    }
}

@Composable
fun SpeedIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.1f, h * 0.5f)
            lineTo(w * 0.9f, h * 0.5f)
            moveTo(w * 0.7f, h * 0.3f)
            lineTo(w * 0.9f, h * 0.5f)
            lineTo(w * 0.7f, h * 0.7f)
            moveTo(w * 0.1f, h * 0.3f)
            lineTo(w * 0.3f, h * 0.3f)
            moveTo(w * 0.1f, h * 0.7f)
            lineTo(w * 0.3f, h * 0.7f)
        }
        drawPath(path, color, style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
fun FocusIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        drawCircle(color, radius = size.width / 3f, center = center, style = Stroke(width = 2f))
        drawCircle(color, radius = 6f, center = center)
    }
}

@Composable
fun BackIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.7f, h * 0.2f)
            lineTo(w * 0.3f, h * 0.5f)
            lineTo(w * 0.7f, h * 0.8f)
        }
        drawPath(path, color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
fun NoteIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawRoundRect(color, topLeft = Offset(w * 0.2f, h * 0.15f), size = Size(w * 0.6f, h * 0.7f), cornerRadius = CornerRadius(4f, 4f), style = Stroke(width = 2.dp.toPx()))
        drawLine(color, Offset(w * 0.35f, h * 0.35f), Offset(w * 0.65f, h * 0.35f), strokeWidth = 2f)
        drawLine(color, Offset(w * 0.35f, h * 0.5f), Offset(w * 0.65f, h * 0.5f), strokeWidth = 2f)
        drawLine(color, Offset(w * 0.35f, h * 0.65f), Offset(w * 0.55f, h * 0.65f), strokeWidth = 2f)
    }
}

@Composable
fun PauseIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawLine(color, Offset(w * 0.35f, h * 0.2f), Offset(w * 0.35f, h * 0.8f), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.65f, h * 0.2f), Offset(w * 0.65f, h * 0.8f), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
fun StopIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawRoundRect(color, topLeft = Offset(w * 0.25f, h * 0.25f), size = Size(w * 0.5f, h * 0.5f), cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()))
    }
}

@Composable
fun SpeakerIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.3f, h * 0.4f)
            lineTo(w * 0.5f, h * 0.4f)
            lineTo(w * 0.75f, h * 0.2f)
            lineTo(w * 0.75f, h * 0.8f)
            lineTo(w * 0.5f, h * 0.6f)
            lineTo(w * 0.3f, h * 0.6f)
            close()
        }
        drawPath(path, color)
        drawPath(Path().apply {
            moveTo(w * 0.85f, h * 0.35f)
            quadraticTo(w * 0.95f, h * 0.5f, w * 0.85f, h * 0.65f)
        }, color, style = Stroke(width = 2f, cap = StrokeCap.Round))
    }
}

@Composable
fun SaveIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.2f, h * 0.15f)
            lineTo(w * 0.7f, h * 0.15f)
            lineTo(w * 0.85f, h * 0.3f)
            lineTo(w * 0.85f, h * 0.85f)
            lineTo(w * 0.15f, h * 0.85f)
            close()
        }
        drawPath(path, color, style = Stroke(width = 2.dp.toPx()))
        drawRect(color, topLeft = Offset(w * 0.35f, h * 0.15f), size = Size(w * 0.3f, h * 0.25f), style = Stroke(width = 1.dp.toPx()))
        drawRect(color, topLeft = Offset(w * 0.3f, h * 0.55f), size = Size(w * 0.4f, h * 0.3f), style = Stroke(width = 1.dp.toPx()))
    }
}

@Composable
fun DrawingIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.2f, h * 0.8f)
            lineTo(w * 0.4f, h * 0.8f)
            lineTo(w * 0.8f, h * 0.4f)
            lineTo(w * 0.6f, h * 0.2f)
            lineTo(w * 0.2f, h * 0.6f)
            close()
            moveTo(w * 0.7f, h * 0.3f)
            lineTo(w * 0.8f, h * 0.4f)
        }
        drawPath(path, color, style = Stroke(width = 2.dp.toPx()))
        drawLine(color, Offset(w * 0.15f, h * 0.85f), Offset(w * 0.85f, h * 0.85f), strokeWidth = 2f)
    }
}

@Composable
fun ExportIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawLine(color, Offset(w * 0.5f, h * 0.1f), Offset(w * 0.5f, h * 0.6f), strokeWidth = 2.dp.toPx())
        val path = Path().apply {
            moveTo(w * 0.35f, h * 0.45f)
            lineTo(w * 0.5f, h * 0.6f)
            lineTo(w * 0.65f, h * 0.45f)
        }
        drawPath(path, color, style = Stroke(width = 2.dp.toPx()))
        val box = Path().apply {
            moveTo(w * 0.2f, h * 0.5f)
            lineTo(w * 0.2f, h * 0.85f)
            lineTo(w * 0.8f, h * 0.85f)
            lineTo(w * 0.8f, h * 0.5f)
        }
        drawPath(box, color, style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
fun ShareIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(color, radius = 5f, center = Offset(w * 0.75f, h * 0.25f))
        drawCircle(color, radius = 5f, center = Offset(w * 0.75f, h * 0.75f))
        drawCircle(color, radius = 5f, center = Offset(w * 0.25f, h * 0.5f))
        drawLine(color, Offset(w * 0.3f, h * 0.45f), Offset(w * 0.7f, h * 0.3f), strokeWidth = 2f)
        drawLine(color, Offset(w * 0.3f, h * 0.55f), Offset(w * 0.7f, h * 0.7f), strokeWidth = 2f)
    }
}

@Composable
fun ProfileIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(color, radius = w * 0.25f, center = Offset(w * 0.5f, h * 0.35f), style = Stroke(width = 2.dp.toPx()))
        val path = Path().apply {
            moveTo(w * 0.2f, h * 0.85f)
            quadraticTo(w * 0.5f, h * 0.65f, w * 0.8f, h * 0.85f)
        }
        drawPath(path, color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
        drawRoundRect(color, topLeft = Offset(0f, 0f), size = Size(w, h), cornerRadius = CornerRadius(w * 0.5f, h * 0.5f), style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
fun PersonIcon(color: Color, modifier: Modifier = Modifier.size(60.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(color, radius = w * 0.3f, center = Offset(w * 0.5f, h * 0.3f), style = Stroke(width = 2.dp.toPx()))
        val path = Path().apply {
            moveTo(w * 0.1f, h * 0.9f)
            quadraticTo(w * 0.5f, h * 0.5f, w * 0.9f, h * 0.9f)
        }
        drawPath(path, color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
fun MenuIcon(color: Color = Color.White, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawLine(color, Offset(w * 0.2f, h * 0.3f), Offset(w * 0.8f, h * 0.3f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.2f, h * 0.5f), Offset(w * 0.8f, h * 0.5f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.2f, h * 0.7f), Offset(w * 0.8f, h * 0.7f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
fun EmailIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val rect = Path().apply {
            moveTo(w * 0.1f, h * 0.25f)
            lineTo(w * 0.9f, h * 0.25f)
            lineTo(w * 0.9f, h * 0.75f)
            lineTo(w * 0.1f, h * 0.75f)
            close()
        }
        drawPath(rect, color, style = Stroke(width = 2.dp.toPx()))
        val line = Path().apply {
            moveTo(w * 0.1f, h * 0.25f)
            lineTo(w * 0.5f, h * 0.55f)
            lineTo(w * 0.9f, h * 0.25f)
        }
        drawPath(line, color, style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
fun PhoneIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.3f, h * 0.15f)
            lineTo(w * 0.7f, h * 0.15f)
            quadraticTo(w * 0.8f, h * 0.15f, w * 0.8f, h * 0.25f)
            lineTo(w * 0.8f, h * 0.75f)
            quadraticTo(w * 0.8f, h * 0.85f, w * 0.7f, h * 0.85f)
            lineTo(w * 0.3f, h * 0.85f)
            quadraticTo(w * 0.2f, h * 0.85f, w * 0.2f, h * 0.75f)
            lineTo(w * 0.2f, h * 0.25f)
            quadraticTo(w * 0.2f, h * 0.15f, w * 0.3f, h * 0.15f)
        }
        drawPath(path, color, style = Stroke(width = 2.dp.toPx()))
        drawCircle(color, radius = 4f, center = Offset(w * 0.5f, h * 0.75f))
        drawLine(color, Offset(w * 0.4f, h * 0.2f), Offset(w * 0.6f, h * 0.2f), strokeWidth = 2f)
    }
}

@Composable
fun WebIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        drawCircle(color, radius = size.width * 0.4f, center = center, style = Stroke(width = 2.dp.toPx()))
        drawOval(color, topLeft = Offset(size.width * 0.3f, size.height * 0.1f), size = Size(size.width * 0.4f, size.height * 0.8f), style = Stroke(width = 2.dp.toPx()))
        drawLine(color, Offset(size.width * 0.1f, center.y), Offset(size.width * 0.9f, center.y), strokeWidth = 2f)
    }
}

@Composable
fun TrophyIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.3f, h * 0.2f)
            lineTo(w * 0.7f, h * 0.2f)
            lineTo(w * 0.7f, h * 0.5f)
            quadraticTo(w * 0.7f, h * 0.7f, w * 0.5f, h * 0.7f)
            quadraticTo(w * 0.3f, h * 0.7f, w * 0.3f, h * 0.5f)
            close()
        }
        drawPath(path, color)
        drawLine(color, Offset(w * 0.5f, h * 0.7f), Offset(w * 0.5f, h * 0.85f), strokeWidth = 4f)
        drawLine(color, Offset(w * 0.35f, h * 0.85f), Offset(w * 0.65f, h * 0.85f), strokeWidth = 4f)
        
        // Handles
        drawPath(Path().apply {
            moveTo(w * 0.3f, h * 0.3f)
            quadraticTo(w * 0.15f, h * 0.3f, w * 0.15f, h * 0.45f)
            quadraticTo(w * 0.15f, h * 0.6f, w * 0.3f, h * 0.55f)
        }, color, style = Stroke(width = 2.dp.toPx()))
        drawPath(Path().apply {
            moveTo(w * 0.7f, h * 0.3f)
            quadraticTo(w * 0.85f, h * 0.3f, w * 0.85f, h * 0.45f)
            quadraticTo(w * 0.85f, h * 0.6f, w * 0.7f, h * 0.55f)
        }, color, style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
fun LockIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawRoundRect(color, topLeft = Offset(w * 0.25f, h * 0.45f), size = Size(w * 0.5f, h * 0.4f), cornerRadius = CornerRadius(4f, 4f))
        drawPath(Path().apply {
            moveTo(w * 0.35f, h * 0.45f)
            lineTo(w * 0.35f, h * 0.3f)
            quadraticTo(w * 0.35f, h * 0.15f, w * 0.5f, h * 0.15f)
            quadraticTo(w * 0.65f, h * 0.15f, w * 0.65f, h * 0.3f)
            lineTo(w * 0.65f, h * 0.45f)
        }, color, style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
fun CheckIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.2f, h * 0.5f)
            lineTo(w * 0.45f, h * 0.75f)
            lineTo(w * 0.8f, h * 0.25f)
        }
        drawPath(path, color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    }
}

@Composable
fun HeartIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.8f)
            cubicTo(w * 0.1f, h * 0.5f, w * 0.1f, h * 0.1f, w * 0.5f, h * 0.3f)
            cubicTo(w * 0.9f, h * 0.1f, w * 0.9f, h * 0.5f, w * 0.5f, h * 0.8f)
        }
        drawPath(path, color)
    }
}

@Composable
fun PlusIcon(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawLine(color, Offset(w * 0.5f, h * 0.25f), Offset(w * 0.5f, h * 0.75f), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.25f, h * 0.5f), Offset(w * 0.75f, h * 0.5f), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
fun PencilIcon(color: Color = Color.White, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Simple pencil drawing
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.2f, h * 0.2f),
            size = Size(w * 0.6f, h * 0.1f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
        // Body
        drawRect(
            color = color,
            topLeft = Offset(w * 0.2f, h * 0.35f),
            size = Size(w * 0.6f, h * 0.4f)
        )
        // Tip
        val path = Path().apply {
            moveTo(w * 0.2f, h * 0.75f)
            lineTo(w * 0.8f, h * 0.75f)
            lineTo(w * 0.5f, h * 0.95f)
            close()
        }
        drawPath(path, color)
    }
}

@Composable
fun ParentalIcon(color: Color = Color.White, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Shield shape
        val path = Path().apply {
            moveTo(w * 0.2f, h * 0.15f)
            lineTo(w * 0.8f, h * 0.15f)
            lineTo(w * 0.8f, h * 0.6f)
            quadraticTo(w * 0.8f, h * 0.85f, w * 0.5f, h * 0.95f)
            quadraticTo(w * 0.2f, h * 0.85f, w * 0.2f, h * 0.6f)
            close()
        }
        drawPath(path, color, style = Stroke(width = 2.dp.toPx()))
        // Small heart inside
        val heart = Path().apply {
            val hw = w * 0.2f
            val hh = h * 0.2f
            val cx = w * 0.5f
            val cy = h * 0.45f
            moveTo(cx, cy + hh * 0.5f)
            cubicTo(cx - hw, cy - hh * 0.3f, cx - hw, cy - hh, cx, cy - hh * 0.5f)
            cubicTo(cx + hw, cy - hh, cx + hw, cy - hh * 0.3f, cx, cy + hh * 0.5f)
        }
        drawPath(heart, color)
    }
}
