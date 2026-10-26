package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ArcadePrimaryFixedDim,
    secondary = ArcadeSecondaryFixedDim,
    tertiary = ArcadeTertiaryFixedDim,
    background = ArcadeInverseSurface,
    surface = ArcadeInverseSurface,
    onPrimary = ArcadeOnPrimary,
    onSecondary = ArcadeOnSecondary,
    onBackground = ArcadeInverseOnSurface,
    onSurface = ArcadeInverseOnSurface,
    surfaceContainer = ArcadeInverseSurface,
    surfaceContainerHigh = ArcadeInverseSurface
)

private val LightColorScheme = lightColorScheme(
    primary = ArcadePrimary,
    primaryContainer = ArcadePrimaryContainer,
    secondary = ArcadeSecondary,
    secondaryContainer = ArcadeSecondaryContainer,
    tertiary = ArcadeTertiary,
    tertiaryContainer = ArcadeTertiaryContainer,
    background = ArcadeBackground,
    surface = ArcadeSurface,
    surfaceContainerLowest = ArcadeSurfaceContainerLowest,
    surfaceContainerLow = ArcadeSurfaceContainerLow,
    surfaceContainer = ArcadeSurfaceContainer,
    surfaceContainerHigh = ArcadeSurfaceContainerHigh,
    surfaceContainerHighest = ArcadeSurfaceContainerHighest,
    onPrimary = ArcadeOnPrimary,
    onPrimaryContainer = ArcadeOnPrimaryContainer,
    onSecondary = ArcadeOnSecondary,
    onTertiary = ArcadeOnTertiary,
    onBackground = ArcadeOnSurface,
    onSurface = ArcadeOnSurface,
    onSurfaceVariant = ArcadeOnSurfaceVariant,
    outline = ArcadeOutline,
    outlineVariant = ArcadeOutlineVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep arcade branded colors consistent
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

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
