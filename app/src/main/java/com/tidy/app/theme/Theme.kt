package com.tidy.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tidy.designsystem.TidyStyleTheme

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

/** Separate canvas, cards and nested controls without changing the palette's accent roles. */
internal fun ColorScheme.withTidySurfaces(darkTheme: Boolean): ColorScheme {
    if (darkTheme) {
        fun raised(amount: Float) = lerp(background, onBackground, amount)
        val card = raised(0.08f)
        return copy(
            surface = card,
            surfaceVariant = raised(0.18f),
            surfaceDim = background,
            surfaceBright = raised(0.23f),
            surfaceContainerLowest = raised(0.02f),
            surfaceContainerLow = raised(0.05f),
            surfaceContainer = card,
            surfaceContainerHigh = raised(0.13f),
            surfaceContainerHighest = raised(0.20f),
            surfaceTint = card
        )
    }
    return copy(
        background = TidyLightBackground,
        onBackground = TidyLightOnBackground,
        surface = TidyLightSurface,
        onSurface = TidyLightOnSurface,
        surfaceVariant = TidyLightSurfaceVariant,
        onSurfaceVariant = TidyLightOnSurfaceVariant,
        surfaceDim = TidyLightBackground,
        surfaceBright = TidyLightSurface,
        surfaceContainerLowest = Color(0xFFF8FAFC),
        surfaceContainerLow = TidyLightSurface,
        surfaceContainer = Color(0xFFE7EDF5),
        surfaceContainerHigh = TidyLightSurfaceVariant,
        surfaceContainerHighest = Color(0xFFD2DCE8),
        surfaceTint = TidyLightSurface
    )
}

@Composable
fun TidyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val settingsRepository = com.tidy.app.TidyApp.instance.settingsRepository
    val selectedThemeState =
        settingsRepository.selectedTheme.collectAsStateWithLifecycle(initialValue = "slate")
    val selectedTheme = selectedThemeState.value

    val entitlementManager = com.tidy.app.TidyApp.instance.entitlementManager
    val isPlusUnlockedState =
        entitlementManager.isPlusUnlocked.collectAsStateWithLifecycle(initialValue = false)
    val isPlusUnlocked = isPlusUnlockedState.value

    TidyTheme(darkTheme, selectedTheme, isPlusUnlocked, content)
}

/** Applies explicit theme state, allowing previews to render without an application instance. */
@Composable
fun TidyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    selectedTheme: String,
    isPlusUnlocked: Boolean,
    content: @Composable () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val colorScheme = when (selectedTheme) {
        "dynamic" -> {
            if (isPlusUnlocked && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (darkTheme) androidx.compose.material3.dynamicDarkColorScheme(context)
                else androidx.compose.material3.dynamicLightColorScheme(context)
            } else {
                if (darkTheme) DarkColorScheme else LightColorScheme
            }
        }

        "slate" -> {
            if (darkTheme) DarkColorScheme else LightColorScheme
        }

        else -> {
            (if (isPlusUnlocked) com.tidy.app.FlavorConfig.getPremiumColorScheme(
                selectedTheme,
                darkTheme
            ) else null)
                ?: (if (darkTheme) DarkColorScheme else LightColorScheme)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme.withTidySurfaces(darkTheme),
        typography = Typography
    ) {
        TidyStyleTheme(content)
    }
}
