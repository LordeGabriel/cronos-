package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private fun createCustomLightColorScheme(accent: String): ColorScheme {
    return when (accent) {
        "BLUE" -> lightColorScheme(
            primary = BluePrimaryLight,
            primaryContainer = Color(0xFFE0F2FE),
            onPrimaryContainer = Color(0xFF0369A1),
            secondary = BlueSecondaryLight,
            tertiary = BlueTertiaryLight
        )
        "RED" -> lightColorScheme(
            primary = RedPrimaryLight,
            primaryContainer = Color(0xFFFEE2E2),
            onPrimaryContainer = Color(0xFF991B1B),
            secondary = RedSecondaryLight,
            tertiary = RedTertiaryLight
        )
        "PINK" -> lightColorScheme(
            primary = PinkPrimaryLight,
            primaryContainer = Color(0xFFFCE7F3),
            onPrimaryContainer = Color(0xFF9D174D),
            secondary = PinkSecondaryLight,
            tertiary = PinkTertiaryLight
        )
        "ORANGE" -> lightColorScheme(
            primary = OrangePrimaryLight,
            primaryContainer = Color(0xFFFFEDD5),
            onPrimaryContainer = Color(0xFF9A3412),
            secondary = OrangeSecondaryLight,
            tertiary = OrangeTertiaryLight
        )
        "GREEN" -> lightColorScheme(
            primary = GreenPrimaryLight,
            primaryContainer = Color(0xFFDCFCE7),
            onPrimaryContainer = Color(0xFF166534),
            secondary = GreenSecondaryLight,
            tertiary = GreenTertiaryLight
        )
        else -> lightColorScheme( // "INDIGO" default
            primary = IndigoPrimaryLight,
            primaryContainer = Color(0xFFE0E7FF),
            onPrimaryContainer = Color(0xFF3730A3),
            secondary = IndigoSecondaryLight,
            tertiary = IndigoTertiaryLight
        )
    }
}

private fun createCustomDarkColorScheme(accent: String): ColorScheme {
    return when (accent) {
        "BLUE" -> darkColorScheme(
            primary = BluePrimaryDark,
            primaryContainer = Color(0xFF075985),
            onPrimaryContainer = Color(0xFFE0F2FE),
            secondary = BlueSecondaryDark,
            tertiary = BlueTertiaryDark
        )
        "RED" -> darkColorScheme(
            primary = RedPrimaryDark,
            primaryContainer = Color(0xFF991B1B),
            onPrimaryContainer = Color(0xFFFEE2E2),
            secondary = RedSecondaryDark,
            tertiary = RedTertiaryDark
        )
        "PINK" -> darkColorScheme(
            primary = PinkPrimaryDark,
            primaryContainer = Color(0xFF9D174D),
            onPrimaryContainer = Color(0xFFFCE7F3),
            secondary = PinkSecondaryDark,
            tertiary = PinkTertiaryDark
        )
        "ORANGE" -> darkColorScheme(
            primary = OrangePrimaryDark,
            primaryContainer = Color(0xFF9A3412),
            onPrimaryContainer = Color(0xFFFFEDD5),
            secondary = OrangeSecondaryDark,
            tertiary = OrangeTertiaryDark
        )
        "GREEN" -> darkColorScheme(
            primary = GreenPrimaryDark,
            primaryContainer = Color(0xFF166534),
            onPrimaryContainer = Color(0xFFDCFCE7),
            secondary = GreenSecondaryDark,
            tertiary = GreenTertiaryDark
        )
        else -> darkColorScheme( // "INDIGO" default
            primary = IndigoPrimaryDark,
            primaryContainer = Color(0xFF3730A3),
            onPrimaryContainer = Color(0xFFE0E7FF),
            secondary = IndigoSecondaryDark,
            tertiary = IndigoTertiaryDark
        )
    }
}

@Composable
fun MyApplicationTheme(
    themeMode: String = "SYSTEM",
    themeAccent: String = "INDIGO",
    darkTheme: Boolean = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    },
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        createCustomDarkColorScheme(themeAccent)
    } else {
        createCustomLightColorScheme(themeAccent)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
