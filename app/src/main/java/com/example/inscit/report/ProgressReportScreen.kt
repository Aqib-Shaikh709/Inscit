package com.example.inscit.report

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inscit.DeepSpace
import com.example.inscit.GhostWhite
import com.example.inscit.models.UserDocument
import com.example.inscit.ui.BackIcon

@Composable
fun ProgressReportScreen(
    userDocument: UserDocument,
    accent: Color,
    onBack: () -> Unit,
    viewModel: ProgressReportViewModel = viewModel()
) {
    val generatedReport by viewModel.generatedReport.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                BackIcon(color = GhostWhite)
            }
            Text(
                "Send Progress Report",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = GhostWhite,
                letterSpacing = 1.sp
            )
        }

        Spacer(Modifier.height(40.dp))

        if (generatedReport == null) {
            Text(
                "Generate a detailed report of your learning progress to share with your parent or guardian. This report includes your XP, quiz scores, and usage statistics.",
                color = GhostWhite.copy(alpha = 0.7f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = { viewModel.generateReport(userDocument) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = DeepSpace
                )
            ) {
                Text("GENERATE REPORT", fontWeight = FontWeight.ExtraBold)
            }
        } else {
            Text(
                "Report Generated Successfully!",
                color = accent,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Preview (Non-editable)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(DeepSpace.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .border(BorderStroke(1.dp, GhostWhite.copy(alpha = 0.1f)), RoundedCornerShape(16.dp))
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = generatedReport!!,
                    color = GhostWhite,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { viewModel.clearReport() },
                    modifier = Modifier.weight(1f).height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GhostWhite.copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GhostWhite)
                ) {
                    Text("REGENERATE")
                }
                
                Spacer(Modifier.width(16.dp))

                Button(
                    onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, generatedReport)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share Progress Report")
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier.weight(1f).height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = DeepSpace
                    )
                ) {
                    Text("SHARE REPORT", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
    }
}
