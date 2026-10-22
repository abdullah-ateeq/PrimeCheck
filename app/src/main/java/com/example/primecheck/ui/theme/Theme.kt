package com.example.primecheck.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.primecheck.model.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = MathPrimaryDark,
    onPrimary = MathOnPrimaryDark,
    primaryContainer = MathPrimaryContainerDark,
    onPrimaryContainer = MathOnPrimaryContainerDark,
    secondary = MathSecondaryDark,
    onSecondary = MathOnSecondaryDark,
    secondaryContainer = MathSecondaryContainerDark,
    onSecondaryContainer = MathOnSecondaryContainerDark,
    background = MathBackgroundDark,
    surface = MathSurfaceDark,
    surfaceVariant = MathSurfaceVariantDark,
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    outline = MathOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = MathPrimaryLight,
    onPrimary = MathOnPrimaryLight,
    primaryContainer = MathPrimaryContainerLight,
    onPrimaryContainer = MathOnPrimaryContainerLight,
    secondary = MathSecondaryLight,
    onSecondary = MathOnSecondaryLight,
    secondaryContainer = MathSecondaryContainerLight,
    onSecondaryContainer = MathOnSecondaryContainerLight,
    background = MathBackgroundLight,
    surface = MathSurfaceLight,
    surfaceVariant = MathSurfaceVariantLight,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    outline = MathOutlineLight
)

@Composable
fun PrimeCheckTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val isDark = when (appTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
