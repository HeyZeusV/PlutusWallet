package com.heyzeusv.plutuswallet.util.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun PlutusWalletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    MaterialTheme(
        colors = if (darkTheme) DarkColors else LightColors,
        typography = PlutusWalletTypography,
        content = content
    )
}