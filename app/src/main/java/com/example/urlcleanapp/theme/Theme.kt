package com.example.urlcleanapp.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val DarkColorScheme = darkColorScheme(
    primary = TidyDarkPrimary,
    onPrimary = TidyDarkOnPrimary,
    secondary = TidyDarkSecondary,
    onSecondary = TidyDarkOnSecondary,
    tertiary = TidyDarkTertiary,
    onTertiary = TidyDarkOnTertiary,
    background = TidyDarkBackground,
    surface = TidyDarkSurface,
    surfaceVariant = TidyDarkSurfaceVariant,
    onBackground = TidyDarkOnBackground,
    onSurface = TidyDarkOnSurface,
    onSurfaceVariant = TidyDarkOnSurfaceVariant,
    primaryContainer = TidyDarkPrimaryContainer,
    onPrimaryContainer = TidyDarkOnPrimaryContainer,
    secondaryContainer = TidyDarkSecondaryContainer,
    onSecondaryContainer = TidyDarkOnSecondaryContainer,
    tertiaryContainer = TidyDarkTertiaryContainer,
    onTertiaryContainer = TidyDarkOnTertiaryContainer,
    error = TidyDarkError,
    errorContainer = TidyDarkErrorContainer,
    onError = TidyDarkOnError,
    onErrorContainer = TidyDarkOnErrorContainer,
    outline = TidyDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = TidyLightPrimary,
    onPrimary = TidyLightOnPrimary,
    secondary = TidyLightSecondary,
    onSecondary = TidyLightOnSecondary,
    tertiary = TidyLightTertiary,
    onTertiary = TidyLightOnTertiary,
    background = TidyLightBackground,
    surface = TidyLightSurface,
    surfaceVariant = TidyLightSurfaceVariant,
    onBackground = TidyLightOnBackground,
    onSurface = TidyLightOnSurface,
    onSurfaceVariant = TidyLightOnSurfaceVariant,
    primaryContainer = TidyLightPrimaryContainer,
    onPrimaryContainer = TidyLightOnPrimaryContainer,
    secondaryContainer = TidyLightSecondaryContainer,
    onSecondaryContainer = TidyLightOnSecondaryContainer,
    tertiaryContainer = TidyLightTertiaryContainer,
    onTertiaryContainer = TidyLightOnTertiaryContainer,
    error = TidyLightError,
    errorContainer = TidyLightErrorContainer,
    onError = TidyLightOnError,
    onErrorContainer = TidyLightOnErrorContainer,
    outline = TidyLightOutline
)

@Composable
fun UrlCleanAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val settingsRepository = com.example.urlcleanapp.UrlCleanApp.instance.settingsRepository
    val selectedThemeState = settingsRepository.selectedTheme.collectAsStateWithLifecycle(initialValue = "slate")
    val selectedTheme = selectedThemeState.value

    val colorScheme = if (selectedTheme == "slate") {
        if (darkTheme) DarkColorScheme else LightColorScheme
    } else {
        com.example.urlcleanapp.FlavorConfig.getPremiumColorScheme(selectedTheme, darkTheme)
            ?: (if (darkTheme) DarkColorScheme else LightColorScheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
