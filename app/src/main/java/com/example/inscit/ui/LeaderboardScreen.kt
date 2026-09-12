package com.example.inscit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inscit.*

import com.example.inscit.ui.theme.spacing

@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    val spacing = MaterialTheme.spacing

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(DeepSpace)) {
        val screenWidth = maxWidth
        val horizontalPadding = if (screenWidth > 600.dp) spacing.huge else spacing.large

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.small)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Leaderboard", style = MaterialTheme.typography.headlineMedium, color = NeonCyan, modifier = Modifier.weight(1f))
                    IconButton(onClick = onBack, modifier = Modifier.background(GhostWhite.copy(alpha = 0.05f), CircleShape).semantics { contentDescription = "Close" }) {
                        Text("✕", color = GhostWhite, style = MaterialTheme.typography.titleMedium)
                    }
                }
                Spacer(Modifier.height(spacing.extraLarge))
            }

            item {
                Box(modifier = Modifier.fillMaxWidth().height(320.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔭", fontSize = 64.sp)
                        Spacer(Modifier.height(spacing.medium))
                        Text(
                            "Leaderboard requires an internet connection",
                            style = MaterialTheme.typography.titleMedium,
                            color = GhostWhite.copy(alpha = 0.5f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(Modifier.height(spacing.large))
                        Button(
                            onClick = onBack,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepSpace)
                        ) {
                            Text("GO BACK", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
