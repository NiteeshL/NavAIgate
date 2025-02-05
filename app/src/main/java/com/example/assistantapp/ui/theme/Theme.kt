package com.example.assistantapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Import colors from Color.kt
import com.example.assistantapp.ui.theme.PrimaryPurple
import com.example.assistantapp.ui.theme.OnPrimaryPurple
import com.example.assistantapp.ui.theme.SecondaryTeal
import com.example.assistantapp.ui.theme.OnSecondaryTeal
import com.example.assistantapp.ui.theme.TertiaryPink
import com.example.assistantapp.ui.theme.OnTertiaryPink
import com.example.assistantapp.ui.theme.ErrorRed
import com.example.assistantapp.ui.theme.OnErrorRed
import com.example.assistantapp.ui.theme.LightBackground
import com.example.assistantapp.ui.theme.DarkBackground
import com.example.assistantapp.ui.theme.LightSurface
import com.example.assistantapp.ui.theme.DarkSurface
import com.example.assistantapp.ui.theme.LightTextPrimary
import com.example.assistantapp.ui.theme.DarkTextPrimary
import com.example.assistantapp.ui.theme.Outline

// Light Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = OnPrimaryPurple,
    secondary = SecondaryTeal,
    onSecondary = OnSecondaryTeal,
    tertiary = TertiaryPink,
    onTertiary = OnTertiaryPink,
    error = ErrorRed,
    onError = OnErrorRed,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    outline = Outline
)

// Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    onPrimary = OnPrimaryPurple,
    secondary = SecondaryTeal,
    onSecondary = OnSecondaryTeal,
    tertiary = TertiaryPink,
    onTertiary = OnTertiaryPink,
    error = ErrorRed,
    onError = OnErrorRed,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    outline = Outline
)

@Composable
fun AssistantAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}