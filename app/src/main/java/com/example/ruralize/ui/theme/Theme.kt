// AppTheme.kt - arquivo NOVO e limpo
package com.example.ruralize.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorPalette = lightColorScheme(
    primary = Color(0xFF2F5D39),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2F5D39),
    onPrimaryContainer = Color.White,

    secondary = Color(0xFF3D7C4A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3D7C4A),
    onSecondaryContainer = Color.White,

    tertiary = Color(0xFFA8C7A1),
    onTertiary = Color(0xFF2F5D39),

    background = Color(0xFFF7F5E8),
    onBackground = Color(0xFF2F5D39),

    surface = Color(0xFFF7F5E8),
    onSurface = Color(0xFF2F5D39),

    outline = Color(0xFF3D7C4A),
    outlineVariant = Color(0xFFA8C7A1)
)

private val DarkColorPalette = darkColorScheme(
    primary = Color(0xFFA8C7A1),
    onPrimary = Color(0xFF2F5D39),
    primaryContainer = Color(0xFFA8C7A1),
    onPrimaryContainer = Color(0xFF2F5D39),

    secondary = Color(0xFF3D7C4A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3D7C4A),
    onSecondaryContainer = Color.White,

    tertiary = Color(0xFFF7F5E8),
    onTertiary = Color(0xFF2F5D39),

    background = Color(0xFF2F5D39),
    onBackground = Color.White,

    surface = Color(0xFF2F5D39),
    onSurface = Color.White,

    outline = Color(0xFFA8C7A1),
    outlineVariant = Color(0xFF3D7C4A)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}