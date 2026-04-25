package com.greenopal.zargon.ui.theme

import androidx.compose.ui.graphics.Color

// ── GOLD SCALE ────────────────────────────────────────────────────────────
val GoldBright   = Color(0xFFF0C040)   // headlines, hover, equipped
val Gold         = Color(0xFFC9A227)   // primary borders, labels
val GoldDark     = Color(0xFF7A5808)   // outer border ring, dimmed frames
val GoldDeep     = Color(0xFF4A3806)   // outermost stroke

// ── EMBER (exit / leave / danger actions) ─────────────────────────────────
val EmberBright  = Color(0xFFFF8C40)
val Ember        = Color(0xFFE05A00)
val EmberDark    = Color(0xFF3A1500)

// ── PARCHMENT (text) ──────────────────────────────────────────────────────
val Parchment    = Color(0xFFE8D5A3)   // main body text
val ParchmentDim = Color(0xFFB8A878)   // secondary / flavor text
val TextDim      = Color(0xFFA08858)   // disabled, labels

// ── STONE (backgrounds) ───────────────────────────────────────────────────
val StoneBg      = Color(0xFF0E0B06)   // screen background
val StoneDark    = Color(0xFF0A0804)   // deepest shadow
val PanelBg      = Color(0xFF1C1208)   // card/panel fill center
val PanelMid     = Color(0xFF241608)   // panel fill edge
val PanelLight   = Color(0xFF2E1E0E)   // panel fill highlight corner

// ── STATUS BARS ───────────────────────────────────────────────────────────
val HpRed        = Color(0xFFC0241A)
val HpRedDark    = Color(0xFF3A0A08)
val HpRedBright  = Color(0xFFFF4040)
val MpBlue       = Color(0xFF1A4A8A)
val MpBlueBright = Color(0xFF4090FF)
val XpGreen      = Color(0xFF206800)
val XpGreenBright= Color(0xFF60E020)

// ── SPECIAL ───────────────────────────────────────────────────────────────
val DebugGreen   = Color(0xFF00C853)

// ── COMPAT ALIASES (for files not yet migrated) ───────────────────────────
val DarkStone    = StoneBg
val MidStone     = PanelMid
val EmberOrange  = Ember
