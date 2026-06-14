package com.example.inscit.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inscit.DeepSpace
import com.example.inscit.GhostWhite
import com.example.inscit.CardBg
import com.example.inscit.models.Lang
import com.example.inscit.models.Review
import com.example.inscit.utils.ProfanityFilter
import java.util.UUID

@Composable
fun ReviewScreen(
    accent: Color,
    txtCol: Color,
    lang: Lang,
    userName: String,
    onBack: () -> Unit
) {
    var reviewText by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(5) }
    var showProfanityAlert by remember { mutableStateOf(false) }
    var mildMessage by remember { mutableStateOf("") }

    // Initial example reviews
    val initialReviews = remember {
        mutableStateListOf(
            Review(UUID.randomUUID().toString(), "Dr. Smith", 5, "Incredible learning tool! The simulations are top-notch."),
            Review(UUID.randomUUID().toString(), "Aman Jaiswal", 4, "Great app, helps me understand physics easily."),
            Review(UUID.randomUUID().toString(), "Priya Sharma", 5, "बहुत ही शानदार ऐप है, विज्ञान को समझना आसान हो गया।"),
            Review(UUID.randomUUID().toString(), "Rahul", 3, "Decent app, but needs more biology topics."),
            Review(UUID.randomUUID().toString(), "Aqib Shaikh", 5, "great app, it just solves one of the biggest problems i face nowadays in my tenth grade.")

        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { BackIcon(txtCol) }
            Text(
                if (lang == Lang.EN) "APP REVIEWS" else "ऐप समीक्षाएं",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = txtCol
            )
        }

        Spacer(Modifier.height(24.dp))

        // Review Input Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardBg,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, GhostWhite.copy(alpha = 0.1f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    if (lang == Lang.EN) "Rate your experience" else "अपने अनुभव को रेट करें",
                    color = GhostWhite.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(5) { index ->
                        val isSelected = index < rating
                        IconButton(onClick = { rating = index + 1 }) {
                            StarIcon(
                                color = if (isSelected) accent else GhostWhite.copy(alpha = 0.2f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { 
                        Text(
                            if (lang == Lang.EN) "Tell us what you think..." else "हमें बताएं कि आप क्या सोचते हैं...",
                            color = GhostWhite.copy(alpha = 0.3f)
                        ) 
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        unfocusedBorderColor = GhostWhite.copy(alpha = 0.1f),
                        cursorColor = accent,
                        focusedTextColor = GhostWhite,
                        unfocusedTextColor = GhostWhite
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp)
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (reviewText.isNotBlank()) {
                            val (mild, masked, wasModified) = ProfanityFilter.processReview(reviewText)
                            
                            if (wasModified) {
                                mildMessage = mild
                                showProfanityAlert = true
                                // Add the MASKED version to reviews
                                initialReviews.add(0, Review(UUID.randomUUID().toString(), userName, rating, masked))
                            } else {
                                initialReviews.add(0, Review(UUID.randomUUID().toString(), userName, rating, reviewText))
                            }
                            reviewText = ""
                            rating = 5
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = DeepSpace),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (lang == Lang.EN) "POST REVIEW" else "समीक्षा पोस्ट करें", fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            if (lang == Lang.EN) "LATEST REVIEWS" else "नवीनतम समीक्षाएं",
            color = accent,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(initialReviews) { review ->
                ReviewItem(review, accent)
            }
        }
    }

    if (showProfanityAlert) {
        AlertDialog(
            onDismissRequest = { showProfanityAlert = false },
            title = { 
                Text(
                    if (lang == Lang.EN) "Review Modified" else "समीक्षा संशोधित",
                    fontWeight = FontWeight.Bold,
                    color = accent
                ) 
            },
            text = {
                Column {
                    Text(
                        if (lang == Lang.EN) 
                            "Your review has been automatically converted to a mild language without changing the context of your message."
                        else 
                            "आपकी समीक्षा को आपके संदेश के संदर्भ को बदले बिना स्वचालित रूप से एक सौम्य भाषा में परिवर्तित कर दिया गया है।",
                        color = GhostWhite
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "\"$mildMessage\"",
                        color = GhostWhite.copy(alpha = 0.6f),
                        style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showProfanityAlert = false }) {
                    Text("OK", color = accent, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun ReviewItem(review: Review, accent: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, GhostWhite.copy(alpha = 0.05f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(review.userName, fontWeight = FontWeight.Bold, color = GhostWhite)
                Row {
                    repeat(5) { i ->
                        StarIcon(
                            color = if (i < review.rating) accent else GhostWhite.copy(alpha = 0.1f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                review.comment,
                color = GhostWhite.copy(alpha = 0.8f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}
