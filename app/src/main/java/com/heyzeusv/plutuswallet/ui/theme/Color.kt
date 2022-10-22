package com.heyzeusv.plutuswallet.ui.theme

import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

// Light colors
val Purple900 = Color(0xff4a148c) // Primary, AlertDialogButtonText, DatePickerHeader
val Purple900Dark = Color(0xff12005e) // Primary Dark
val Purple900Light = Color(0xff7c43bd) // colorBackground
val LightGreen900 = Color(0xff33691e) // Secondary, Button Background
val ErrorLight = Color(0xffb00020) // Error
val ButtonUnselectedLight = Color(0x60000000)
val ExpenseTextLight = Color(0xffad0000)
val IncomeTextLight = Color(0xff006300)
// Expense Chart Colors
val Red600 = Color(0xffe53935)
val Yellow500 = Color(0xffffeb3b)
val Pink400 = Color(0xffec407a)
val Orange500 = Color(0xffff9800)
// Income Chart Colors
val Green600 = Color(0xff43a047)
val Teal600 = Color(0xff00897b)
val Blue500 = Color(0xff2196f3)
val DeepPurple300 = Color(0xff9575cd)

// Dark colors
val PurpleBase = Color(0xff24102f) // primary
val PurpleDark = Color(0xff1b1120) // primary Dark, background
val GreenBase = Color(0xffa5d6a7) // secondary
val ErrorDark = Color(0xffffbaba) // error
val ButtonTextDark = Color(0xff2f1c39) // chart hole color, date picker header
val ButtonUnselectedDark = Color(0x60ffffff)
val FilterBackgroundDark = Color(0xff362340)
val AlertDialogButtonText = Color(0xffffb3ff)
val ExpenseTextDark = Color(0xffff9494)
val IncomeTextDark = Color(0xff85c700)
// Expense Chart Colors
val Red900 = Color(0xffb71c1c)
val Lime900 = Color(0xff827717)
val Pink800 = Color(0xffad1457)
val Brown400 = Color(0xff8d6e63)
// Income Chart Colors
val Green800 = Color(0xff2e7d32)
val Teal800 = Color(0xff00695c)
val Blue800 = Color(0xff1565c0)
val DeepPurple600 = Color(0xff5e35b1)

val LightColors = lightColors(
    primary = Purple900,
    primaryVariant = Purple900Dark,
    onPrimary = Color.Black,
    secondary = LightGreen900,
    background = Purple900Light,
    error = ErrorLight
)

val DarkColors = darkColors(
    primary = PurpleBase,
    primaryVariant = PurpleDark,
    onPrimary = Color.White,
    secondary = GreenBase,
    error = ErrorDark
)