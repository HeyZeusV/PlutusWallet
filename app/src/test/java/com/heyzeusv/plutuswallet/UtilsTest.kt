package com.heyzeusv.plutuswallet

import io.mockk.junit5.MockKExtension
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale

@ExtendWith(MockKExtension::class)
internal class UtilsTest {

    @Nested
    @DisplayName("Given LanguageCode is set to 'en'")
    inner class LanguageCodeEn {

        @Nested
        @DisplayName("When user Changes Language")
        inner class LanguageChange {

            @Test
            @DisplayName("Then JVM Locale is changed to English")
            fun changeLanguageEnglish() {

                val newLocale = Locale("en")
                Locale.setDefault(newLocale)

                assertEquals(Locale.ENGLISH, Locale.getDefault())
            }
        }
    }

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

    @Nested
    @DisplayName("Given Date")
    inner class DateTest {

        @Nested
        @DisplayName("When Called")
        inner class DateFunctionCalled {

            @Test
            @DisplayName("Change Date to start of its set day")
            fun dateStartOfDay() {

                val calendar = GregorianCalendar()
                calendar.timeInMillis = Date().time
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE     , 0)
                calendar.set(Calendar.SECOND     , 0)
                calendar.set(Calendar.MILLISECOND, 0)

                // 0 in Date() is not the start of a day, 25,200,000 is the start of day for GMT -8
                assertEquals(25200000, (calendar.timeInMillis % 86400000))
            }
        }
    }
}