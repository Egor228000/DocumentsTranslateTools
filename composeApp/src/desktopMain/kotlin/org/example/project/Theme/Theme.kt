package org.example.project.Theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightColors = lightColorScheme(
    primary = Color(0xFF3A86FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF001C3B),
    secondary = Color(0xFFFDFBFF),
    onSecondary = Color(0xFF1A1C1E),
    secondaryContainer = Color(0xFFE0E2EC),
    onSecondaryContainer = Color(0xFF44474E),
    tertiary = Color(0xFFFB5607),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDBCB),
    onTertiaryContainer = Color(0xFF380F00),
    background = Color(0xFFFDFBFF),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFDFBFF),
    onSurface = Color(0xFF1A1C1E),
    error = Color(0xFFB3261E),
    onError = Color.White
)

 val DarkColors = darkColorScheme(
    primary = Color(0xFFA9C7FF),
    onPrimary = Color(0xFF00315F),
    primaryContainer = Color(0xFF1F477A),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = Color(0xFF1A1C1E),
    onSecondary = Color(0xFFE2E2E6),
    secondaryContainer = Color(0xFF2E3033),
    onSecondaryContainer = Color(0xFFC8C6CF),
    tertiary = Color(0xFFFFB598),
    onTertiary = Color(0xFF5B1A00),
    tertiaryContainer = Color(0xFF7B2F00),
    onTertiaryContainer = Color(0xFFFFDBCB),
    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE2E2E6),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410)
)


@Composable
fun MyAppTheme(
    colorScheme: ColorScheme,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
