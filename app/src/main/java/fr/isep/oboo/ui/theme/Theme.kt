package fr.isep.oboo.ui.theme

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
    primary = Emerald700,
    onPrimary = Color.White,
    primaryContainer = Emerald600,
    onPrimaryContainer = Color.White,
    inversePrimary = Emerald600,
    secondary = Emerald700,
    onSecondary = Color.White,
    secondaryContainer = Emerald600,
    onSecondaryContainer = Color.White,
    tertiary = Blue400,
    onTertiary = Color.White,
    tertiaryContainer = Blue300,
    onTertiaryContainer = Color.White,
    background = Zinc900,
    onBackground = Color.Black,
    surface = Zinc900,
    onSurface = Color.White,
    surfaceVariant = Gray200,
    onSurfaceVariant = Gray200,
    // surfaceTint = ,
    inverseSurface = Zinc200,
    inverseOnSurface = Color.White,
    error = Red600,
    onError = Color.White,
    errorContainer = Red500,
    onErrorContainer = Red800,
    // outline = ,
    // outlineVariant = ,
    // scrim = ,
    // surfaceBright = ,
    surfaceContainer = Zinc800,
    surfaceContainerHigh = Zinc700,
    surfaceContainerHighest = Zinc600,
    surfaceContainerLow = Zinc900,
    surfaceContainerLowest = Zinc950,
    // surfaceDim = ,
)

private val LightColorScheme = lightColorScheme(
    primary = Emerald700,
    onPrimary = Color.White,
    primaryContainer = Emerald600,
    onPrimaryContainer = Color.White,
    inversePrimary = Emerald600,
    secondary = Emerald700,
    onSecondary = Color.White,
    secondaryContainer = Emerald600,
    onSecondaryContainer = Color.White,
    tertiary = Blue400,
    onTertiary = Color.White,
    tertiaryContainer = Blue300,
    onTertiaryContainer = Color.White,
    background = Gray50,
    onBackground = Color.Black,
    surface = Gray100,
    onSurface = Color.Black,
    surfaceVariant = Gray200,
    onSurfaceVariant = Color.Black,
    // surfaceTint = ,
    inverseSurface = Zinc800,
    inverseOnSurface = Color.White,
    error = Red600,
    onError = Color.White,
    errorContainer = Red500,
    onErrorContainer = Red800,
    // outline = ,
    // outlineVariant = ,
    // scrim = ,
    // surfaceBright = ,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Zinc200,
    surfaceContainerHighest = Zinc300,
    surfaceContainerLow = Zinc100,
    surfaceContainerLowest = Zinc50,
    // surfaceDim = ,
)

@Composable
fun ObooTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    // Here, we manually disable dynamic colors, which is enabled by default
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}