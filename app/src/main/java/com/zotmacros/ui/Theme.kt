package com.zotmacros.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Gold = Color(0xFFFECC07)
val BlueBackground = Color(0xFF103B67)
val BlueSurface = Color(0xFF255799)
val ProteinColor = Color(0xFF65D7FF)
val FatColor = Color(0xFF38E1ED)
private val colors = darkColorScheme(
    primary = Gold, onPrimary = Color(0xFF102A45), primaryContainer = Color(0xFF715900), onPrimaryContainer = Gold,
    secondary = ProteinColor, onSecondary = BlueBackground, secondaryContainer = Gold,
    onSecondaryContainer = Color(0xFF102A45), tertiary = FatColor,
    background = BlueBackground, onBackground = Color(0xFFF5F8FF),
    surface = BlueSurface, onSurface = Color(0xFFF5F8FF), surfaceVariant = Color(0xFF1D4D81),
    onSurfaceVariant = Color(0xFFD4E5FF), outline = Color(0xFF8AAFD7), outlineVariant = Color(0xFF3B6DA5),
    surfaceContainer = Color(0xFF1D4D81), surfaceContainerHigh = BlueSurface,
    surfaceContainerHighest = Color(0xFF2D609F), surfaceContainerLow = Color(0xFF184573),
    error = Color(0xFFFFD4D4), onError = Color(0xFF690005)
)
@Composable fun ZotTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = Typography(
        displayLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 56.sp),
        displayMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 44.sp),
        headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 30.sp),
        headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 26.sp),
        titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 22.sp),
        titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
        bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp),
        bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp),
        labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    ), content = content)
}
