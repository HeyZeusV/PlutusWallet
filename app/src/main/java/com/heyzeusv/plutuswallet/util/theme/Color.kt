package com.heyzeusv.plutuswallet.util.theme

import android.annotation.SuppressLint
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.graphics.Color

/**
 *  Alpha % -> Hex
 *  100% — FF   75% — BF    50% — 80    25% — 40
 *  95% — F2    70% — B3    45% — 73    20% — 33
 *  90% — E6    65% — A6    40% — 66    15% — 26
 *  85% — D9    60% — 99    35% — 59    10% — 1A
 *  80% — CC    55% — 8C    30% — 4D    5% — 0D
 */
// Light colors
val Purple900 = Color(0xff4a148c) // Primary, AlertDialogButtonText, DatePickerHeader
val Purple900Dark = Color(0xff12005e) // Primary Dark
val Purple900Light = Color(0xff7c43bd) // colorBackground
val LightGreen900 = Color(0xff33691e) // Secondary, Button Background
val LightGreen900Alpha15 = Color(0x2633691e) // Selected Chip Background
val BlackAlpha10 = Color(0x19000000) // Unselected Chip Background
val BlackAlpha40 = Color(0x66000000) // Unselected Stroke
val BlackAlpha60 = Color(0x99000000) // Background Overlay
val ErrorLight = Color(0xffb00020) // Error
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
val GreenBaseAlpha15 = Color(0x26a5d6a7) // Selected Chip Background
val WhiteAlpha10 = Color(0x19ffffff) // Unselected Chip Background
val WhiteAlpha40 = Color(0x66ffffff) // Unselected Stroke
val ErrorDark = Color(0xffffbaba) // error
val ChartCenterHole = Color(0xff2f1c39) // chart hole color
val AlertDialogButtonTextDark = Color(0xffffb3ff)
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
    error = ErrorLight,
    onBackground = Color.White
)

@SuppressLint("ConflictingOnColor")
val DarkColors = darkColors(
    primary = PurpleBase,
    primaryVariant = PurpleDark,
    onPrimary = Color.White,
    secondary = GreenBase,
    background = PurpleDark,
    surface = PurpleBase,
    error = ErrorDark
)

val PWLightColors = PlutusWalletColors(
    backgroundOverlay = BlackAlpha60,
    alertDialogButtonText = Purple900,
    unselected = BlackAlpha40,
    chipSelectedBackground = LightGreen900Alpha15,
    chipUnselectedBackground = BlackAlpha10,
    expense = ExpenseTextLight,
    expenseChartPrimary = Red600,
    expenseChartSecondary = Yellow500,
    expenseChartTertiary = Pink400,
    expenseChartQuaternary = Orange500,
    income = IncomeTextLight,
    incomeChartPrimary = Green600,
    incomeChartSecondary = Teal600,
    incomeChartTertiary = Blue500,
    incomeChartQuaternary = DeepPurple300,
    chartCenterHole = Color.White
)

val PWDarkColors = PlutusWalletColors(
    backgroundOverlay = BlackAlpha60,
    alertDialogButtonText = AlertDialogButtonTextDark,
    unselected = WhiteAlpha40,
    chipSelectedBackground = GreenBaseAlpha15,
    chipUnselectedBackground = WhiteAlpha10,
    expense = ExpenseTextDark,
    expenseChartPrimary = Red900,
    expenseChartSecondary = Lime900,
    expenseChartTertiary = Pink800,
    expenseChartQuaternary = Brown400,
    income = IncomeTextDark,
    incomeChartPrimary = Green800,
    incomeChartSecondary = Teal800,
    incomeChartTertiary = Blue800,
    incomeChartQuaternary = DeepPurple600,
    chartCenterHole = ChartCenterHole
)

@Stable
class PlutusWalletColors(
    backgroundOverlay: Color,
    alertDialogButtonText: Color,
    unselected: Color,
    chipSelectedBackground: Color,
    chipUnselectedBackground: Color,
    expense: Color,
    expenseChartPrimary: Color,
    expenseChartSecondary: Color,
    expenseChartTertiary: Color,
    expenseChartQuaternary: Color,
    income: Color,
    incomeChartPrimary: Color,
    incomeChartSecondary: Color,
    incomeChartTertiary: Color,
    incomeChartQuaternary: Color,
    chartCenterHole: Color,
) {
    var backgroundOverlay by mutableStateOf(backgroundOverlay, structuralEqualityPolicy())
        internal set
    var alertDialogButtonText by mutableStateOf(alertDialogButtonText, structuralEqualityPolicy())
        internal set
    var unselected by mutableStateOf(unselected, structuralEqualityPolicy())
        internal set
    var chipSelectedBackground by mutableStateOf(chipSelectedBackground, structuralEqualityPolicy())
        internal set
    var chipUnselectedBackground by mutableStateOf(chipUnselectedBackground, structuralEqualityPolicy())
        internal set
    var expense by mutableStateOf(expense, structuralEqualityPolicy())
        internal set
    var expenseChartPrimary by mutableStateOf(expenseChartPrimary, structuralEqualityPolicy())
        internal set
    var expenseChartSecondary by mutableStateOf(expenseChartSecondary, structuralEqualityPolicy())
        internal set
    var expenseChartTertiary by mutableStateOf(expenseChartTertiary, structuralEqualityPolicy())
        internal set
    var expenseChartQuaternary by mutableStateOf(expenseChartQuaternary, structuralEqualityPolicy())
        internal set
    var income by mutableStateOf(income, structuralEqualityPolicy())
        internal set
    var incomeChartPrimary by mutableStateOf(incomeChartPrimary, structuralEqualityPolicy())
        internal set
    var incomeChartSecondary by mutableStateOf(incomeChartSecondary, structuralEqualityPolicy())
        internal set
    var incomeChartTertiary by mutableStateOf(incomeChartTertiary, structuralEqualityPolicy())
        internal set
    var incomeChartQuaternary by mutableStateOf(incomeChartQuaternary, structuralEqualityPolicy())
        internal set
    var chartCenterHole by mutableStateOf(chartCenterHole, structuralEqualityPolicy())
        internal set
}

val LocalPWColors = compositionLocalOf { PWLightColors }