package org.technoserve.farmcollector.ui.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


private val DarkColorScheme = darkColorScheme(
    primary = Turquoise,
    secondary = Purple,
    tertiary = Teal,
    background = DarkGray,
    surface = Teal,
    error = Red,
    onPrimary = White,
    onSecondary = White,
    onBackground = IceBlue,
    onSurface = White,
    onError = White
)

private val LightColorScheme = lightColorScheme(
    primary = Teal,
    secondary = Yellow,
    tertiary = Green,
    background = IceBlue,
    surface = Teal,
    error = Red,
    onPrimary = Black,
    onSecondary = Black,
    onBackground = Black,
    onSurface = Black,
    onError = Black
)

@Composable
fun FarmCollectorTheme(
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

    val colors = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}