package com.heyzeusv.plutuswallet.ui.transaction

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.EditText
import androidx.appcompat.R
import com.google.android.material.textfield.TextInputEditText
import com.heyzeusv.plutuswallet.util.Key
import com.heyzeusv.plutuswallet.util.PreferenceHelper.get
import com.heyzeusv.plutuswallet.util.SettingsUtils
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.inject.Inject

/**
 *  Custom EditText to handle currency.
 *
 *  Adds thousands separators automatically, and limits the number of decimal places.
 *  Always use locale US instead of default to make DecimalFormat work well in all language.
 *
 *  @constructor standard EditText constructor
 */
@AndroidEntryPoint
class CurrencyEditText @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet,
    defStyleAttr: Int = R.attr.editTextStyle
) : TextInputEditText(context, attributeSet, defStyleAttr) {

    private val currencyTextWatcher = CurrencyTextWatcher(this)

    // SharedPreference
    @Inject lateinit var sharedPref: SharedPreferences

    // keys from separator symbols
    private val decimalKey: String = sharedPref[Key.KEY_DECIMAL_SYMBOL, "period"]
    private val thousandsKey: String = sharedPref[Key.KEY_THOUSANDS_SYMBOL, "comma"]
    private val decimalPlaces: Boolean = sharedPref[Key.KEY_DECIMAL_PLACES, true]

    // formatters
    private var decimalFormatter: DecimalFormat = DecimalFormat()
    private var integerFormatter: DecimalFormat = DecimalFormat()

    // symbols used
    private var decimalSymbol: Char = '.'
    private var thousandsSymbol: Char = ','

    init {

        // retrieves separator symbols from keys
        decimalSymbol = SettingsUtils.getSeparatorSymbol(decimalKey)
        thousandsSymbol = SettingsUtils.getSeparatorSymbol(thousandsKey)

        // set up decimal/thousands symbol
        val customSymbols = DecimalFormatSymbols(Locale.US)
        customSymbols.decimalSeparator = decimalSymbol
        customSymbols.groupingSeparator = thousandsSymbol

        // formatters using custom symbols
        decimalFormatter = DecimalFormat("#,##0.00", customSymbols)
        integerFormatter = DecimalFormat("#,##0", customSymbols)
        decimalFormatter.roundingMode = RoundingMode.HALF_UP
        integerFormatter.roundingMode = RoundingMode.HALF_UP

        // forces numpad
        this.setRawInputType(Configuration.KEYBOARD_QWERTY)
    }

    /**
     *  Used to tell when this View is [focused] by user.
     */
    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusRect)

        if (focused) {
            this.addTextChangedListener(currencyTextWatcher)
        } else {
            this.removeTextChangedListener(currencyTextWatcher)
        }
    }

    /**
     *  Watches for when text is changed.
     *
     *  Also handles formatting the string and the cursor position.
     *  @constructor sets EditText to be watched.
     */
    private inner class CurrencyTextWatcher constructor(private val editText: EditText) :
        TextWatcher {

        private var beforeText: String = ""

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            beforeText = s.toString()
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            if (s == null) return

            val initCursorPos: Int = start + before

            val numCharsRightCursor: Int =
                getNumberOfChars(beforeText.substring(initCursorPos, beforeText.length))
            val newAmount: String = formatAmount(s.toString())
            editText.removeTextChangedListener(this)
            editText.setText(newAmount)
            editText.setSelection(getNewCursorPos(numCharsRightCursor, newAmount))
            editText.addTextChangedListener(this)
        }

        override fun afterTextChanged(editable: Editable?) {}

        /**
         *  Takes in the [amount] entered and returns formatted string with correct
         *  thousands/decimal symbol according to settings.
         */
        private fun formatAmount(amount: String): String {

            val result: String = removeSymbols(amount)
            val amt: BigDecimal
            if (result.isEmpty()) return "" else amt = BigDecimal(result)

            // uses decimal formatter depending on number of decimal places entered
            return when {
                decimalPlaces -> decimalFormatter.format(amt)
                else -> integerFormatter.format(amt)
            }
        }

        /**
         *  Removes all symbols from [numString]. Returns a formatted string depending on Settings
         *  and content of symbol-stripped string.
         */
        private fun removeSymbols(numString: String): String {

            var chars = ""
            // retrieves only numbers in numString
            for (i: Char in numString) {
                if (i.isDigit()) chars += i
            }

            return when {
                // doesn't allow string to be empty
                decimalPlaces && chars == "" -> "0.00"
                // divides numbers by 100 in order to easily get decimal places
                decimalPlaces -> BigDecimal(chars)
                    .divide(BigDecimal(100), 2, RoundingMode.HALF_UP).toString()
                // doesn't allow string to be empty
                chars == "" -> "0"
                // returns just a string of numbers
                else -> chars
            }
        }

        /**
         *  Uses [numCharsRightCursor] to calculate the new location of cursor within [numString].
         */
        private fun getNewCursorPos(numCharsRightCursor: Int, numString: String): Int {

            var rightOffset = 0
            var rightCount: Int = numCharsRightCursor

            // thousands symbols increases offset, but doesn't decrease characters to right
            for (i: Char in numString.reversed()) {
                if (rightCount == 0) break
                if (i.isDigit() || i == decimalSymbol) rightCount--
                rightOffset++
            }
            return numString.length - rightOffset
        }

        /**
         *  Returns the number of digits and decimal symbol in [text].
         */
        private fun getNumberOfChars(text: String): Int {

            var count = 0
            for (i: Char in text) {
                // ends early if decimal symbol is detected, but turned off in settings
                when {
                    i.isDigit() || (decimalPlaces && i == decimalSymbol) -> count++
                    !decimalPlaces && i == decimalSymbol -> return count
                }
            }
            return count
        }
    }
}