package com.example.ui.theme

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

fun getPaletteColors(paletteKey: String): Triple<Color, Color, Color> {
    return when (paletteKey) {
        "ROYAL_VIOLET" -> Triple(RoyalVioletPrimary, RoyalVioletSecondary, RoyalVioletAccent)
        "EMERALD_GLAM" -> Triple(EmeraldGlamPrimary, EmeraldGlamSecondary, EmeraldGlamAccent)
        "GOLDEN_LUXURY" -> Triple(GoldenLuxuryPrimary, GoldenLuxurySecondary, GoldenLuxuryAccent)
        "SUNSET_COPPER" -> Triple(SunsetCopperPrimary, SunsetCopperSecondary, SunsetCopperAccent)
        "OCEAN_BREEZE" -> Triple(OceanBreezePrimary, OceanBreezeSecondary, OceanBreezeAccent)
        "MIDNIGHT_LUX" -> Triple(MidnightLuxuryPrimary, MidnightLuxurySecondary, MidnightLuxuryAccent)
        "SAGE_MINDFUL" -> Triple(SageMindfulPrimary, SageMindfulSecondary, SageMindfulAccent)
        else -> Triple(RoseGoldPrimary, RoseGoldSecondary, RoseGoldAccent)
    }
}

@Composable
fun DynamicSalonTheme(
    paletteKey: String = "ROSE_GOLD",
    darkModeOption: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (darkModeOption) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    val context = LocalContext.current
    val (primary, secondary, accent) = getPaletteColors(paletteKey)

    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> {
            darkColorScheme(
                primary = accent,
                onPrimary = Color.Black,
                primaryContainer = secondary,
                onPrimaryContainer = Color.White,
                secondary = accent,
                background = DarkBackground,
                surface = DarkSurface,
                surfaceVariant = DarkSurfaceVariant,
                tertiary = primary
            )
        }
        else -> {
            lightColorScheme(
                primary = primary,
                onPrimary = Color.White,
                primaryContainer = RoseGoldBackgroundLight,
                onPrimaryContainer = primary,
                secondary = secondary,
                onSecondary = Color.White,
                tertiary = accent,
                background = RoseGoldBackgroundLight,
                surface = RoseGoldSurfaceLight
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
