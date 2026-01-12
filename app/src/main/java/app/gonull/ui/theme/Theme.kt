package app.gonull.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = GoNullGreen,
    onPrimary = GoNullBlack,
    secondary = GoNullGreen,
    background = GoNullBlack,
    surface = GoNullSurface,
    onBackground = GoNullWhite,
    onSurface = GoNullWhite,
    error = GoNullRed,
    outline = GoNullBorder
)

@Composable
fun GoNullTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = GoNullTypography,
        content = content
    )
}
