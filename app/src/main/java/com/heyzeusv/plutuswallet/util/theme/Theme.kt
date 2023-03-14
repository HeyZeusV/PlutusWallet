package com.heyzeusv.plutuswallet.util.theme

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 *  Theme Composable of entire app. [theme] is used to determine if a theme has been manually
 *  selected in Settings screen, if not then we default to system setting. Default set to 1 in
 *  order for previews to use light theme. This is also where we provide our custom
 *  [PlutusWalletColors], so it can be used wherever. [content] what is to be displayed.
 */
@Composable
fun PlutusWalletTheme(
    theme: Int = 1,
    content: @Composable () -> Unit
) {
    val pwColors: PlutusWalletColors
    when (theme) {
        1 -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            pwColors = PWLightColors
        }
        2 -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            pwColors = PWDarkColors
        }
        else -> {
            pwColors = if (isSystemInDarkTheme()) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                PWDarkColors
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                PWLightColors
            }
        }
    }
    CompositionLocalProvider(LocalPWColors provides pwColors) {
        MaterialTheme(
            colors = when (theme) {
                1 -> LightColors
                2 -> DarkColors
                else -> if (isSystemInDarkTheme()) DarkColors else LightColors
            },
            typography = PlutusWalletTypography,
            content = content
        )
    }
}