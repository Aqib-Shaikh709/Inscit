package com.example.inscit.parentalcontrol

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inscit.DeepSpace
import com.example.inscit.GhostWhite
import com.example.inscit.models.UserDocument
import com.example.inscit.ui.BackIcon
import com.example.inscit.ui.CheckIcon

@Composable
fun ParentalControlScreen(
    userDocument: UserDocument,
    onUpdateUser: (UserDocument) -> Unit,
    accent: Color,
    onBack: () -> Unit,
    viewModel: ParentalControlViewModel = viewModel()
) {
    val email by viewModel.email.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()
    
    var localEmail by remember { mutableStateOf(userDocument.settings.parentEmail) }

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
                "Link Parent Account",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = GhostWhite,
                letterSpacing = 1.sp
            )
        }

        Spacer(Modifier.height(40.dp))

        Text(
            "Enter the email address where you'd like to receive daily learning reports.",
            color = GhostWhite.copy(alpha = 0.7f),
            fontSize = 14.sp,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = localEmail,
            onValueChange = { 
                localEmail = it
                viewModel.onEmailChange(it)
            },
            label = { Text("Parent Email Address", color = accent) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accent,
                unfocusedBorderColor = GhostWhite.copy(alpha = 0.2f),
                cursorColor = accent,
                focusedTextColor = GhostWhite,
                unfocusedTextColor = GhostWhite
            ),
            singleLine = true
        )

        if (isSuccess) {
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CheckIcon(accent, Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Email saved successfully!", color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                if (viewModel.validateEmail(localEmail)) {
                    viewModel.saveEmail(localEmail)
                    onUpdateUser(userDocument.copy(
                        settings = userDocument.settings.copy(parentEmail = localEmail)
                    ))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accent,
                contentColor = DeepSpace
            )
        ) {
            Text("SAVE PARENT ACCOUNT", fontWeight = FontWeight.ExtraBold)
        }
        
        Spacer(Modifier.height(24.dp))
    }
}
