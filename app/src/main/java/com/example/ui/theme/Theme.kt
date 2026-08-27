package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = OrangeMoneyOrange,
    onPrimary = Color.White,
    primaryContainer = OrangeMoneyDark,
    onPrimaryContainer = Color.White,
    secondary = OrangeMoneyAccent,
    onSecondary = Color.White,
    secondaryContainer = OrangeCharcoal,
    onSecondaryContainer = Color.White,
    tertiary = CashEmerald,
    onTertiary = Color.White,
    background = OrangeBlack,
    onBackground = Color(0xFFF9FAFB),
    surface = OrangeCharcoal,
    onSurface = Color(0xFFF9FAFB),
    surfaceVariant = OrangeSurfaceDark,
    onSurfaceVariant = Color(0xFFD1D5DB),
    outline = Color(0xFF374151),
    error = StatusError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = OrangeMoneyOrange,
    onPrimary = Color.White,
    primaryContainer = OrangeMoneyLight,
    onPrimaryContainer = OrangeMoneyDark,
    secondary = OrangeBlack,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3F4F6),
    onSecondaryContainer = TextPrimary,
    tertiary = CashEmerald,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = TextPrimary,
    surface = LightSurface,
    onSurface = TextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = LightBorder,
    error = StatusError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep tailored brand palette for high recognition
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
