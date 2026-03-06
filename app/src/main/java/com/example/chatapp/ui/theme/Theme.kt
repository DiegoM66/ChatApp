package com.example.chatapp.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Paleta de cores escuras com o novo tema Teal Vibrante
private val DarkColorScheme = darkColorScheme(
    primary = TealPrimaryDark,
    onPrimary = BackgroundDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    primaryContainer = TealSentBubbleDark,
    onPrimaryContainer = TextPrimaryDark,
    secondaryContainer = TealReceivedBubbleDark,
    onSecondaryContainer = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onBackground = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark
)

// Paleta de cores claras com o novo tema Teal Vibrante
private val LightColorScheme = lightColorScheme(
    primary = TealDeep, // Cor principal para TopAppBar
    onPrimary = SurfaceLight,
    secondary = TealVibrant, // A nova cor vibrante como secundária/accent
    background = BackgroundLight,
    surface = SurfaceLight,
    primaryContainer = TealSentBubbleLight,
    onPrimaryContainer = TextPrimaryLight,
    secondaryContainer = SurfaceLight,
    onSecondaryContainer = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onBackground = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight
)

@Composable
fun ChatAppTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}