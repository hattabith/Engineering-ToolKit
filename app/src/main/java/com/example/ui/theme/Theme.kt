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
import com.example.data.repository.AppThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = Teal80,
    onPrimary = Color(0xFF003544),
    primaryContainer = Color(0xFF004D63),
    onPrimaryContainer = Color(0xFFBBE9FF),
    secondary = Amber80,
    onSecondary = Color(0xFF412D00),
    secondaryContainer = Color(0xFF5D4200),
    onSecondaryContainer = Color(0xFFFFDEA3),
    tertiary = TealGrey80,
    background = DarkBackground,
    onBackground = Color(0xFFE2E7ED),
    surface = DarkSurface,
    onSurface = Color(0xFFE2E7ED),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFB4C1CD),
    error = Color(0xFFFF6B6B),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = Teal40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBE9FF),
    onPrimaryContainer = Color(0xFF001F2A),
    secondary = Amber40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDEA3),
    onSecondaryContainer = Color(0xFF261900),
    tertiary = TealGrey40,
    background = LightBackground,
    onBackground = Color(0xFF191C1E),
    surface = LightSurface,
    onSurface = Color(0xFF191C1E),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF43474E),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun EngineeringToolkitTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    dynamicColor: Boolean = false, // Use our calibrated engineering palette by default
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

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
