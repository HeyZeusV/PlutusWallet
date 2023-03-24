package com.heyzeusv.plutuswallet.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SettingsUtilsTest {

    @Nested
    @DisplayName("Given BigDecimal")
    inner class BigDecimalFormat {

        private lateinit var formatter1 : DecimalFormat
        private lateinit var formatter2 : DecimalFormat

        @BeforeEach
        fun setUp() {

            val customSymbols = DecimalFormatSymbols(Locale.US)
            customSymbols.groupingSeparator = '-'
            formatter1 = DecimalFormat("#,###", customSymbols)
            formatter1.roundingMode = RoundingMode.HALF_UP
            customSymbols.groupingSeparator = ' '
            customSymbols.decimalSeparator  = ','
            formatter2 = DecimalFormat("#,###.00", customSymbols)
            formatter2.roundingMode = RoundingMode.HALF_UP
        }

        @Nested
        @DisplayName("When Thousands symbol is '-'")
        inner class Thousands {

            @Test
            @DisplayName("Then BigDecimal formatted to Integer")
            fun bigDecimalInteger() {

                val num1 = BigDecimal("12346.67")
                val num2 = BigDecimal("4381812")
                val num3 = BigDecimal("9828.33")

                assertEquals("12-347", formatter1.format(num1))
                assertEquals("4-381-812", formatter1.format(num2))
                assertEquals("9-828", formatter1.format(num3))
            }
        }

        @Nested
        @DisplayName("When Thousands symbol is ' ' and Decimal symbol is ','")
        inner class ThousandsDecimal {

            @Test
            @DisplayName("Then BigDecimal formatted to Whole Number")
            fun bigDecimalWhole() {

                val num1 = BigDecimal("12345.67")
                val num2 = BigDecimal("328910.2")
                val num3 = BigDecimal("9876")

                assertEquals("12 345,67", formatter2.format(num1))
                assertEquals("328 910,20", formatter2.format(num2))
                assertEquals("9 876,00", formatter2.format(num3))
            }
        }
    }
}