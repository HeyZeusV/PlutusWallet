package com.heyzeusv.plutuswallet

import org.junit.Assert.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 *  Unit tests for functions used by CurrencyEditText
 */
class CurrencyEditTextTest {

    @Test
    @DisplayName("Format string using correct DecimalFormatter")
    fun formatAmount() {

        // set up decimal/thousands symbol
        val symbols = DecimalFormatSymbols(Locale.US)
        symbols.decimalSeparator  = '-'
        symbols.groupingSeparator = ' '

        // formatters using custom symbols
        val decimal0Formatter = DecimalFormat("#,##0."  , symbols)
        val decimal1Formatter = DecimalFormat("#,##0.#" , symbols)
        val decimal2Formatter = DecimalFormat("#,##0.##", symbols)
        val integerFormatter  = DecimalFormat("#,###"   , symbols)

        decimal0Formatter.roundingMode = RoundingMode.HALF_UP
        decimal1Formatter.roundingMode = RoundingMode.HALF_UP
        decimal2Formatter.roundingMode = RoundingMode.HALF_UP
        integerFormatter.roundingMode  = RoundingMode.HALF_UP

        // test strings, would go though editSymbols first to format to use by BigDecimal
        val tString1 = ""
        val tString2 = "1234567.89"
        val tString3 = "122.5"
        val tString4 = "12."
        val tString5 = "987654"
        val tStringList : MutableList<String> =
            mutableListOf(tString1, tString2, tString3, tString4, tString5)
        val nStringList : MutableList<String> = mutableListOf()
        val bool : List<Boolean> = listOf(true, false)

        // bool will be replaced by decimalPlaces setting
        for (b : Boolean in bool) {

            for (s: String in tStringList) {

                var amount = BigDecimal("0")
                var empty = false
                when (s.isEmpty()) {
                    true -> empty = true
                    else -> amount = BigDecimal(s)
                }
                // uses decimal formatter depending on number of decimal places entered
                nStringList.add(
                    when {
                        empty -> ""
                        b && s.contains(Regex("(\\.)\\d{2}")) ->
                            decimal2Formatter.format(amount)
                        b && s.contains(Regex("(\\.)\\d")) ->
                            decimal1Formatter.format(amount)
                        b && s.contains(".") ->
                            decimal0Formatter.format(amount)
                        else -> integerFormatter.format(amount)
                    }
                )
            }
        }
        val expected : List<String> = listOf("", "1 234 567-89", "122-5", "12-", "987 654",
            "", "1 234 568", "123", "12", "987 654")
        assertEquals(expected, nStringList)
    }

    @Test
    @DisplayName("Remove Thousands Symbols and ensures decimal symbol is '.'")
    fun editSymbols() {

        val decimalSymbol = '-'
        var decimalPlaces = true

        // Never have to worry about strings with letters due to filter
        val tString1 = "12 322 214-23"
        val tString2 = "-30"
        val tString3 = "4 542-32"

        val tStringList : List<String> = listOf(tString1, tString2, tString3)
        val nStringList : MutableList<String> = mutableListOf()

        for (s : String in tStringList) {

            // test that decimal places are not added if turned off in settings
            if (s == tString3) decimalPlaces = false
            var newString = ""
            for (c : Char in s) {

                // ensures only 1 decimal symbol exists and returns if decimal setting is off and
                // decimal symbol is detected
                when {
                    c.isDigit() -> newString += c
                    decimalPlaces && c == decimalSymbol && !newString.contains(".") -> newString += "."
                    !decimalPlaces && c == decimalSymbol -> {
                        nStringList.add(newString)
                        break
                    }
                }
            }
            nStringList.add(newString)
        }

        // won't need double entry in real function since it will return a single string
        // so string will be return directly from when rather than using break
        val expected : List<String> = listOf("12322214.23", ".30", "4542", "4542")
        assertEquals(expected, nStringList)
    }

    @Test
    @DisplayName("Calculate new location of cursor")
    fun getNewCursorPos() {

        // characters to right of cursor
        val rCount1 = 3
        val rCount2 = 8
        val rCount3 = 0
        // formatted string with correct symbols
        val tString1 = "123,456.78"
        val tString2 = "9,876,543,210"
        val tString3 = "382,010,832"

        val tStringList : List<String>     = listOf(tString1, tString2, tString3)
        val rIntList    : List<Int>        = listOf(rCount1, rCount2, rCount3)
        val posList     : MutableList<Int> = mutableListOf()

        for (i : Int in rIntList) {

            for (s : String in tStringList) {

                var rightCount : Int = i
                var rightOffset      = 0
                // thousands symbols increases offset, but doesn't decrease characters to right
                for (c : Char in s.reversed()) {

                    if (rightCount == 0) break
                    if (c.isDigit() || c == '.') rightCount--
                    rightOffset++
                }
                posList.add(s.length - rightOffset)
            }
        }

        val expected : List<Int> = listOf(7, 10, 8, 1, 3, 1, 10, 13, 11)
        assertEquals(expected, posList)
    }

    @Test
    @DisplayName("Calculate length without thousands symbols")
    fun getNumberOfChars() {

        val decimalSymbol = '.'
        var decimalPlaces = true

        // test strings
        val tString1 = "123,456,789"
        val tString2 = "5,647,382.91"
        val tString3 = "98,765.43"

        val tStringList : List<String>     = listOf(tString1, tString2, tString3)
        val sizeList    : MutableList<Int> = mutableListOf()

        for (s : String in tStringList) {

            // test that decimal places are not counted if turned off in settings
            if (s == tString3) decimalPlaces = false
            var count = 0
            for (c : Char in s) {

                // ends early if decimal symbol is detected, but turned off in settings
                when {
                    c.isDigit() || (decimalPlaces && c == decimalSymbol) -> count++
                    !decimalPlaces && c == decimalSymbol                 -> break
                }
            }
            sizeList.add(count)
        }

        val expected : List<Int> = listOf(9, 10, 5)
        assertEquals(expected, sizeList)
    }
}