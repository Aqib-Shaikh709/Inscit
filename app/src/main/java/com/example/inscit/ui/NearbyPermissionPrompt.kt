package com.example.inscit.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inscit.CardBg
import com.example.inscit.DeepSpace
import com.example.inscit.GhostWhite
import com.example.inscit.PowerRed

// Shown at the top of every Nearby screen (transfer send/receive, versus host/join)
// until the runtime radio permissions are granted. Without these, advertising and
// discovery fail silently and both device lists stay empty forever.
@Composable
fun NearbyPermissionPrompt(
    accent: Color,
    onGrant: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, PowerRed.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "📡 NEARBY ACCESS NEEDED",
                color = PowerRed, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 1.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Bluetooth + Wi-Fi Direct are used to find the other phone. No internet is used. " +
                    "Allow Nearby devices / Bluetooth and Location when asked.",
                color = GhostWhite.copy(alpha = 0.7f), fontSize = 13.sp, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            PressableButton(
                onClick = onGrant,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = DeepSpace),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("GRANT ACCESS", fontWeight = FontWeight.Black) }
            Spacer(Modifier.height(8.dp))
            Text(
                "Still empty after granting? Turn Location services ON, keep BT + Wi-Fi on, " +
                    "stay close, or allow permissions in Settings → Apps → Inscit → Permissions.",
                color = GhostWhite.copy(alpha = 0.45f), fontSize = 11.sp, textAlign = TextAlign.Center
            )
        }
    }
}
