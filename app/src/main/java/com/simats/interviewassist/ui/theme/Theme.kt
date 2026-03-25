package com.simats.interviewassist.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

// Semantic App Colors Extension
data class AppColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val surfaceHighlight: Color,
    val divider: Color,
    val borderLight: Color,
    
    val textTitle: Color,
    val textBody: Color,
    val textSecondary: Color,
    
    val iconTint: Color,
    
    val primary: Color,
    val primaryHighlight: Color,
    
    val success: Color,
    val successBg: Color,
    val warning: Color,
    val warningBg: Color,
    val error: Color,
    val errorBg: Color,
    val purple: Color,
    val purpleBg: Color
)

val LightAppColors = AppColors(
    background = Color(0xFFF8FAFC), // Slate 50
    surface = Color.White,
    surfaceVariant = Color(0xFFF1F5F9), // Slate 100
    surfaceHighlight = Color(0xFFEFF6FF), // Blue 50
    divider = Color(0xFFE2E8F0), // Slate 200
    borderLight = Color(0xFFCBD5E1), // Slate 300
    textTitle = Color(0xFF0F172A), // Slate 900
    textBody = Color(0xFF334155), // Slate 700
    textSecondary = Color(0xFF64748B), // Slate 500
    iconTint = Color(0xFF94A3B8), // Slate 400
    primary = Color(0xFF2563EB), // Blue 600
    primaryHighlight = Color(0xFFDBEAFE), // Blue 100
    success = Color(0xFF10B981), // Emerald 500
    successBg = Color(0xFFECFDF5), // Emerald 50
    warning = Color(0xFFF59E0B), // Amber 500
    warningBg = Color(0xFFFFFBEB), // Amber 50
    error = Color(0xFFEF4444), // Red 500
    errorBg = Color(0xFFFEF2F2), // Red 50
    purple = Color(0xFF8B5CF6), // Violet 500
    purpleBg = Color(0xFFF5F3FF) // Violet 50
)

val DarkAppColors = AppColors(
    background = Color(0xFF0F172A), // Navy Dark
    surface = Color(0xFF1E293B), // Slate 800
    surfaceVariant = Color(0xFF334155), // Slate 700
    surfaceHighlight = Color(0xFF1E3A8A), // Blue 900
    divider = Color(0xFF334155), // Slate 700
    borderLight = Color(0xFF475569), // Slate 600
    textTitle = Color(0xFFF8FAFC), // Slate 50
    textBody = Color(0xFFCBD5E1), // Slate 300
    textSecondary = Color(0xFF94A3B8), // Slate 400
    iconTint = Color(0xFF64748B), // Slate 500
    primary = Color(0xFF60A5FA), // Blue 400
    primaryHighlight = Color(0xFF1D4ED8), // Blue 700
    success = Color(0xFF34D399), // Emerald 400
    successBg = Color(0xFF064E3B), // Emerald 900
    warning = Color(0xFFFBBF24), // Amber 400
    warningBg = Color(0xFF78350F), // Amber 900
    error = Color(0xFFF87171), // Red 400
    errorBg = Color(0xFF7F1D1D), // Red 900
    purple = Color(0xFFA78BFA), // Violet 400
    purpleBg = Color(0xFF4C1D95) // Violet 900
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

@Composable
fun InterviewAssistTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}