package com.heyzeusv.plutuswallet.util.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

// replacing a few default TextStyles that comes with MaterialTheme
val PlutusWalletTypography = Typography(
    subtitle1 = TextStyle(
        fontSize = 18.sp
    ),
    subtitle2 = TextStyle(
        fontSize = 14.sp,
        letterSpacing = 0.1.sp
    ),
    body1 = TextStyle(
        fontSize = 18.sp
    )
)

/**
 *  Custom TextStyles used for specific Composables.
 */
val chipTextStyle = TextStyle(
    fontSize = 18.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = 1.sp,
    textAlign = TextAlign.Center
)

val alertDialogButton = TextStyle(
    fontSize = 14.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = 1.sp,
)