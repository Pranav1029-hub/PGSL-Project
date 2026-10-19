package com.gadvasu.livestock.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Palette taken from the GADVASU web prototype ("paper" green look)
val Green = Color(0xFF1E6B3F)
val GreenInk = Color(0xFF155230)
val GreenSoft = Color(0xFFE1EFE5)
val Paper = Color(0xFFF3F6F1)
val CardWhite = Color(0xFFFFFFFF)
val Ink = Color(0xFF12261A)
val Muted = Color(0xFF5B6B5F)
val Line = Color(0xFFD3DFD5)
val Amber = Color(0xFFD99A16)
val AmberInk = Color(0xFF7A5300)
val AmberSoft = Color(0xFFFBF0D3)
val Red = Color(0xFFB5362C)
val RedSoft = Color(0xFFF8E4E1)

val DarkPaper = Color(0xFF0F1912)
val DarkCard = Color(0xFF182419)
val DarkInk = Color(0xFFE9F0EA)
val DarkMuted = Color(0xFF9DB0A3)
val DarkLine = Color(0xFF2A3A2F)
val DarkGreen = Color(0xFF5DBB7C)
val DarkGreenInk = Color(0xFF8BD6A3)
val DarkGreenSoft = Color(0xFF1D3325)
val DarkAmber = Color(0xFFF0B93A)
val DarkAmberInk = Color(0xFFF3CD70)
val DarkAmberSoft = Color(0xFF3A2F14)
val DarkRed = Color(0xFFF0847A)
val DarkRedSoft = Color(0xFF40201D)

private val LightColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = GreenSoft,
    onPrimaryContainer = GreenInk,
    secondary = GreenInk,
    onSecondary = Color.White,
    secondaryContainer = GreenSoft,
    onSecondaryContainer = GreenInk,
    tertiary = Amber,
    onTertiary = Color.White,
    tertiaryContainer = AmberSoft,
    onTertiaryContainer = AmberInk,
    background = Paper,
    onBackground = Ink,
    surface = CardWhite,
    onSurface = Ink,
    surfaceVariant = GreenSoft,
    onSurfaceVariant = Muted,
    outline = Line,
    outlineVariant = Line,
    error = Red,
    onError = Color.White,
    errorContainer = RedSoft,
    onErrorContainer = Red,
)

private val DarkColors = darkColorScheme(
    primary = DarkGreen,
    onPrimary = DarkPaper,
    primaryContainer = DarkGreenSoft,
    onPrimaryContainer = DarkGreenInk,
    secondary = DarkGreenInk,
    onSecondary = DarkPaper,
    secondaryContainer = DarkGreenSoft,
    onSecondaryContainer = DarkGreenInk,
    tertiary = DarkAmber,
    onTertiary = DarkPaper,
    tertiaryContainer = DarkAmberSoft,
    onTertiaryContainer = DarkAmberInk,
    background = DarkPaper,
    onBackground = DarkInk,
    surface = DarkCard,
    onSurface = DarkInk,
    surfaceVariant = DarkGreenSoft,
    onSurfaceVariant = DarkMuted,
    outline = DarkLine,
    outlineVariant = DarkLine,
    error = DarkRed,
    onError = DarkPaper,
    errorContainer = DarkRedSoft,
    onErrorContainer = DarkRed,
)

// System font: Android ships Noto Sans Devanagari / Gurmukhi, so Hindi and Punjabi render well.
private val AppTypography = Typography(
    headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 32.sp, fontWeight = FontWeight.ExtraBold),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold),
    titleSmall = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold),
    labelMedium = TextStyle(fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold),
)

@Composable
fun GadvasuTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}

/** Green for profit, red for loss, normal text colour for zero. */
@Composable
fun profitColor(n: Double): Color = when {
    n > 0 -> MaterialTheme.colorScheme.primary
    n < 0 -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurface
}
