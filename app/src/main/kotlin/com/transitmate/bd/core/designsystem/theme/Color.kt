package com.transitmate.bd.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// ==========================================
// Dark Theme Tokens — Neutral Graphite
// ==========================================
val DarkBackground = Color(0xFF111315)
val DarkSurface = Color(0xFF191C1F)
val DarkSurfaceVariant = Color(0xFF22262A)
val DarkSurfaceHighest = Color(0xFF2A2F35)
val DarkOutline = Color(0xFF2E343B)
val DarkOutlineVariant = Color(0xFF252A30)
val DarkTextPrimary = Color(0xFFF1F3F5)
val DarkTextSecondary = Color(0xFF9AA2AC)

val DarkPrimary = Color(0xFF2FA497)
val DarkOnPrimary = Color(0xFF072623)
val DarkPrimaryContainer = Color(0xFF1B3B38)
val DarkOnPrimaryContainer = Color(0xFFBCE8E2)

val DarkSecondary = Color(0xFF5B8FB9)
val DarkOnSecondary = Color(0xFF0B2235)
val DarkSecondaryContainer = Color(0xFF1C3144)
val DarkOnSecondaryContainer = Color(0xFFCCE1F2)

val DarkSuccess = Color(0xFF34A874)
val DarkSuccessContainer = Color(0xFF153326)
val DarkOnSuccessContainer = Color(0xFFA8E4C5)

val DarkWarning = Color(0xFFD99B26)
val DarkWarningContainer = Color(0xFF362811)
val DarkOnWarningContainer = Color(0xFFF5D693)

val DarkError = Color(0xFFD95F59)
val DarkOnError = Color(0xFFFFFFFF)
val DarkErrorContainer = Color(0xFF381B1A)
val DarkOnErrorContainer = Color(0xFFF6B8B5)

// ==========================================
// Light Theme Tokens — Warm Off-White
// ==========================================
val LightBackground = Color(0xFFF6F7F8)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFECEFF2)
val LightSurfaceHighest = Color(0xFFE3E7EC)
val LightOutline = Color(0xFFDCE1E7)
val LightOutlineVariant = Color(0xFFE6EAEF)
val LightTextPrimary = Color(0xFF16191D)
val LightTextSecondary = Color(0xFF5C6570)

val LightPrimary = Color(0xFF12746A)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFDDF2EE)
val LightOnPrimaryContainer = Color(0xFF0B4740)

val LightSecondary = Color(0xFF31628D)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFE3EEF7)
val LightOnSecondaryContainer = Color(0xFF1A3D5C)

val LightSuccess = Color(0xFF1E8255)
val LightSuccessContainer = Color(0xFFDCF4E7)
val LightOnSuccessContainer = Color(0xFF0F4F32)

val LightWarning = Color(0xFFB87A14)
val LightWarningContainer = Color(0xFFFDF3DC)
val LightOnWarningContainer = Color(0xFF6E4706)

val LightError = Color(0xFFC2413B)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFCE8E7)
val LightOnErrorContainer = Color(0xFF781E1A)

@Immutable
data class TransitSemanticColors(
    val success: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val info: Color,
    val infoContainer: Color,
    val onInfoContainer: Color
)
