package com.example.inscit.ui.theme

import androidx.compose.ui.graphics.Color

// ════════════════════════════════════════════════════════════════════════════════
//  METALLIC DESIGN LANGUAGE — Inscit Hybrid Metal Aesthetic
//  Dark brushed-metal base with chrome accents, chrome shine overlays,
//  and warm accent highlights. Not extreme — a refined hybrid look.
// ════════════════════════════════════════════════════════════════════════════════

// ── Base Surfaces ──
val DeepWarmSpace   = Color(0xFF0A0B0E)   // Deep background (almost black-warm)
val WarmSurface     = Color(0xFF16181C)   // Card surface — dark metal
val WarmSurface2    = Color(0xFF22242A)   // Elevated surface — slightly lighter metal
val WarmSurface3    = Color(0xFF2E3036)   // Raised surface — chrome mid-tone

// ── Metallic / Chrome tones ──
val MetalDark   = Color(0xFF141416)   // Deep metal shadow
val MetalMid    = Color(0xFF222226)   // Mid metal tone
val MetalLight  = Color(0xFF3A3A40)   // Highlight metal
val MetalEdge   = Color(0xFF5A5A60)   // Sharp metal edge / rim light
val MetalChrome = Color(0xFFE8E8EE)   // Bright chrome highlight

// Shine overlays (alpha layers for "light glancing off metal")
val MetalShine    = Color(0x4DFFFFFF)   // Strong shine
val MetalShineMid = Color(0x2AFFFFFF)   // Mid shine
val MetalShineSoft= Color(0x15FFFFFF)   // Soft / distant shine

// ── Accents (warm, keep brand identity) ──
val WarmAmber      = Color(0xFFFFA726)  // Primary accent
val WarmOrange     = Color(0xFFEF6C00)  // Secondary accent
val WarmCream      = Color(0xFFFFE082)  // Tertiary accent
val WarmCoral      = Color(0xFFFF8A65)  // Error / attention
val WarmGold       = Color(0xFFFFD54F)  // Success / done

// ── Text ──
val WarmGhostWhite = Color(0xFFF5F5F5)  // Primary text
val WarmMuted      = Color(0xFFB0B0B0)  // Secondary text
val WarmDim        = Color(0xFF6E6E6E)  // Disabled / hint text

// ── Legacy aliases (kept for existing references) ──
val CardBg = WarmSurface
val DeepSpace = DeepWarmSpace
val GhostWhite = WarmGhostWhite

// ── Metallic Gradient Brushes (defined as colors here, brushes built in MicroInteractions) ──
val MetalGradientStart = Color(0xFF1C1C1E)
val MetalGradientMid   = Color(0xFF3A3A42)
val MetalGradientEnd   = Color(0xFF1C1C1E)
