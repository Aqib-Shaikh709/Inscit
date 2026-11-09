package com.example.inscit.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset

// ═════════════════════════════════════════════════════════════════════════════════
//  METALLIC DESIGN LANGUAGE — Inscit Hybrid Metal Aesthetic
//  Dark brushed-metal base with chrome accents, chrome shine overlays,
//  bevel edges and warm accent highlights. Refined hybrid look — shiny but
//  not extreme. Layout stays bento; only the look changes.
// ═════════════════════════════════════════════════════════════════════════════════

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

// Bevel edges for raised-metal look (top-left light, bottom-right dark)
val MetalBevelLight = Color(0xFF4A4A50)   // Top / left bevel highlight
val MetalBevelDark  = Color(0xFF0A0A0C)   // Bottom / right bevel shadow
val MetalRimLight   = Color(0xFFB0B0B8)   // Subtle rim light on raised edges

// Inner glow / ambient occlusion inside cards
val MetalInnerGlow  = Color(0x1AFFFFFF)

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

// Accent glows (tinted overlays for shiny metal highlights)
val WarmAmberGlow  = Color(0x4DFFA726)
val WarmGoldGlow   = Color(0x4DFFD54F)
val WarmCreamGlow  = Color(0x2FFFFFE0)

// ── Text ──
val WarmGhostWhite = Color(0xFFF5F5F5)  // Primary text
val WarmMuted      = Color(0xFFB0B0B0)  // Secondary text
val WarmDim        = Color(0xFF6E6E6E)  // Disabled / hint text

// ── Legacy aliases (kept for existing references) ──
val CardBg = WarmSurface
val DeepSpace = DeepWarmSpace
val GhostWhite = WarmGhostWhite

// ── Metallic gradient core colors (declared before brushes that use them) ──
val MetalGradientStart = Color(0xFF1C1C1E)
val MetalGradientMid   = Color(0xFF3A3A42)
val MetalGradientEnd   = Color(0xFF1C1C1E)

// ── Metallic Gradient Brushes ──
// Brushed metal: dark -> mid -> light -> mid -> dark (horizontal)
val MetalGradientBrush = Brush.linearGradient(
    colors = listOf(
        MetalGradientStart,
        MetalGradientMid,
        Color(0xFF4A4A50),
        MetalGradientMid,
        MetalGradientStart
    ),
    start = Offset(0f, 0f),
    end = Offset(1f, 0f)
)

// Vertical brushed metal (top highlight, bottom shadow)
val MetalGradientVertical = Brush.linearGradient(
    colors = listOf(
        Color(0xFF2A2A2E),
        MetalGradientMid,
        Color(0xFF1C1C1E)
    ),
    start = Offset(0f, 0f),
    end = Offset(0f, 1f)
)

// Chrome border gradient (bright edges, darker center)
val ChromeBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF6A6A70),
        Color(0xFF8A8A90),
        Color(0xFFA8A8B0),
        Color(0xFF8A8A90),
        Color(0xFF6A6A70)
    ),
    start = Offset(0f, 0f),
    end = Offset(1f, 0f)
)

// Diagonal shine sweep (transparent -> white -> transparent)
val ShineSweepBrush = Brush.linearGradient(
    colors = listOf(
        Color(0x00FFFFFF),
        Color(0x22FFFFFF),
        Color(0x00FFFFFF)
    ),
    start = Offset(-0.4f, -0.4f),
    end = Offset(0.8f, 0.8f)
)

// Warm accent glow brush (amber)
val AmberGlowBrush = Brush.linearGradient(
    colors = listOf(
        Color(0x00FFA726),
        Color(0x33FFA726),
        Color(0x00FFA726)
    ),
    start = Offset(0f, 0f),
    end = Offset(1f, 1f)
)

// Convenience: semi-transparent metal surface tints
val MetalSurfaceTint = Color(0x20FFFFFF)   // faint gloss over metal
val MetalShadowTint  = Color(0x33000000)   // deep shadow under raised metal
