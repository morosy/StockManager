package com.morosy.stockmanager.ui.theme

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = Color(0xFF381E72),
    secondary = PurpleGrey80,
    onSecondary = Color(0xFF332D41),
    tertiary = Pink80,
    background = AppBackgroundDark,
    onBackground = Color(0xFFE6E1E5),
    surface = StockCardDark,
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = PurpleSurfaceDark,
    onSurfaceVariant = Color(0xFFCAC4D0),
    secondaryContainer = PurpleSurfaceDark,
    onSecondaryContainer = Color(0xFFE8DEF8),
    errorContainer = ErrorContainerDark,
    onErrorContainer = ErrorOnContainerDark,
    outline = PurpleOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    onPrimary = Color.White,
    secondary = PurpleGrey40,
    onSecondary = Color.White,
    tertiary = Pink40,
    background = AppBackgroundLight,
    onBackground = Color(0xFF1C1B1F),
    surface = StockCardLight,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = PurpleSurface,
    onSurfaceVariant = Color(0xFF49454F),
    secondaryContainer = PurpleSurface,
    onSecondaryContainer = PurplePrimary,
    errorContainer = ErrorContainerLight,
    onErrorContainer = ErrorOnContainerLight,
    outline = PurpleOutline
)

@Composable
fun StockManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
