package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class GlobalChatCustomColors(
    val brandLime: Color,
    val darkGreen: Color,
    val lightMint: Color,
    val pastelGreen: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val screenBackground: Color,
    val messageIncoming: Color,
    val messageOutgoing: Color,
    val navBackground: Color,
    val isDark: Boolean
)

val LocalGlobalChatColors = staticCompositionLocalOf {
    GlobalChatCustomColors(
        brandLime = WiseLime,
        darkGreen = DarkGreen,
        lightMint = LightMint,
        pastelGreen = PastelGreen,
        cardBackground = CardWhite,
        cardBorder = CardBorderLight,
        textPrimary = NearBlack,
        textSecondary = GrayMuted,
        screenBackground = WarmOffWhite,
        messageIncoming = Color(0xFFF1F3ED),
        messageOutgoing = WiseLime,
        navBackground = CardWhite,
        isDark = false
    )
}

private val LightColorScheme = lightColorScheme(
    primary = WiseLime,
    onPrimary = DarkGreen,
    primaryContainer = LightMint,
    onPrimaryContainer = DarkGreen,
    secondary = DarkGreen,
    onSecondary = CardWhite,
    background = WarmOffWhite,
    onBackground = NearBlack,
    surface = CardWhite,
    onSurface = NearBlack,
    surfaceVariant = Color(0xFFF0F3EC),
    onSurfaceVariant = GrayMuted,
    outline = CardBorderLight
)

private val DarkColorScheme = darkColorScheme(
    primary = WiseLime,
    onPrimary = DarkGreen,
    primaryContainer = DarkGreen,
    onPrimaryContainer = WiseLime,
    secondary = PastelGreen,
    onSecondary = NearBlack,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkBorder
)

@Composable
fun GlobalChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val customColors = if (darkTheme) {
        GlobalChatCustomColors(
            brandLime = WiseLime,
            darkGreen = DarkGreen,
            lightMint = Color(0xFF1E3812),
            pastelGreen = PastelGreen,
            cardBackground = DarkSurface,
            cardBorder = DarkBorder,
            textPrimary = DarkTextPrimary,
            textSecondary = DarkTextMuted,
            screenBackground = DarkBackground,
            messageIncoming = DarkSurfaceElevated,
            messageOutgoing = WiseLime,
            navBackground = DarkSurface,
            isDark = true
        )
    } else {
        GlobalChatCustomColors(
            brandLime = WiseLime,
            darkGreen = DarkGreen,
            lightMint = LightMint,
            pastelGreen = PastelGreen,
            cardBackground = CardWhite,
            cardBorder = CardBorderLight,
            textPrimary = NearBlack,
            textSecondary = GrayMuted,
            screenBackground = WarmOffWhite,
            messageIncoming = Color(0xFFF0F3EC),
            messageOutgoing = WiseLime,
            navBackground = CardWhite,
            isDark = false
        )
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalGlobalChatColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}

object GlobalChatTheme {
    val colors: GlobalChatCustomColors
        @Composable
        get() = LocalGlobalChatColors.current
}
