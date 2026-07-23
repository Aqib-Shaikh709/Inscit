package com.example.inscit.ui

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.example.inscit.NeonCyan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ProfileImage(
    photoUrl: Uri?,
    modifier: Modifier = Modifier,
    placeholderColor: Color = NeonCyan
) {
    val context = LocalContext.current
    var profileBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(photoUrl) {
        if (photoUrl != null) {
            withContext(Dispatchers.IO) {
                try {
                    val stream = if (photoUrl.scheme == "content" || photoUrl.scheme == "file") {
                        context.contentResolver.openInputStream(photoUrl)
                    } else {
                        java.net.URL(photoUrl.toString()).openStream()
                    }
                    stream?.use { inputStream ->
                        val b = BitmapFactory.decodeStream(inputStream)
                        profileBitmap = b?.asImageBitmap()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            profileBitmap = null
        }
    }

    Box(
        modifier = modifier.clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (profileBitmap != null) {
            Image(
                bitmap = profileBitmap!!,
                contentDescription = "Profile Picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            PersonIcon(color = placeholderColor, modifier = Modifier.fillMaxSize(0.6f))
        }
    }
}
