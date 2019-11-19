package com.heyzeusv.financeapplication.utilities

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Rect
import android.preference.PreferenceManager
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.EditText
import androidx.appcompat.R
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

// for future localization use: https://stackoverflow.com/a/33496285/9825089

// USED THIS: https://stackoverflow.com/a/45299775/9825089

// SharedPreferences Keys
private const val KEY_DECIMAL_SYMBOL   = "key_decimal_symbol"
private const val KEY_THOUSANDS_SYMBOL = "key_thousands_symbol"

/**
 *  Custom EditText to handle currency.
 *
 *  Adds thousands separators automatically, and limits the number of decimal places.
 *  Always use locale US instead of default to make DecimalFormat work well in all language.
 *
 *  @constructor standard EditText constructor
 */
class CurrencyEditText @JvmOverloads constructor(
    context : Context, attributeSet : AttributeSet,
    defStyleAttr : Int = R.attr.editTextStyle) :
    androidx.appcompat.widget.AppCompatEditText(context, attributeSet, defStyleAttr) {

    private val currencyTextWatcher = CurrencyTextWatcher(this)

    private val sharedPreferences : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val decimalSymbol   : String = sharedPreferences.getString(KEY_DECIMAL_SYMBOL  , "period")!!
    private val thousandsSymbol : String = sharedPreferences.getString(KEY_THOUSANDS_SYMBOL, "comma" )!!

    init {

        // numeric text class with decimal flag
        this.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        this.hint      = context.getString(com.heyzeusv.financeapplication.R.string.transaction_total_hint)

        customSymbols.decimalSeparator  = Utils.getSeparatorSymbol(decimalSymbol)
        customSymbols.groupingSeparator = Utils.getSeparatorSymbol(thousandsSymbol)
    }

    /**
     *  Used to tell when this View is focused by user.
     *
     *  @param focused true when user selects this view, false when deselected.
     */
    override fun onFocusChanged(focused : Boolean, direction : Int, previouslyFocusRect : Rect?) {

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
    private class CurrencyTextWatcher
    internal constructor(private val editText : EditText) : TextWatcher {

        private var previousCleanString : String = ""

        override fun beforeTextChanged(s : CharSequence?, start : Int, count : Int, after : Int) {}

        override fun onTextChanged(s : CharSequence?, start : Int, before : Int, count : Int) {}

        /**
         *  Notifies when there has ben a changed in editable.
         *
         *  Will react according to what the text is changed to.
         *
         *  @param editable the text to be changed.
         */
        override fun afterTextChanged(editable : Editable?) {

            val string : String = editable.toString()

            // cleanString: doesn't contain ','
            val cleanString : String = string.replace(("[" + customSymbols.groupingSeparator +"]").toRegex(), "")

            // prevents afterTextChanged recursive call
            if (cleanString == previousCleanString || cleanString.isEmpty()) {

                return
            }

            previousCleanString = cleanString
            // formats string depending if there is decimal places
            val formattedString : String =
                if (cleanString.contains(customSymbols.decimalSeparator)) {

                    formatDecimal(cleanString)
                } else {

                    formatInteger(cleanString)
                }
            editText.removeTextChangedListener(this)
            editText.setText(formattedString)
            // sets location of cursor
            handleSelection()
            editText.addTextChangedListener(this)
        }

        /**
         *  Will format string with thousands separators.
         *
         *  Even though Totals will always be a decimal number, formatInteger is needed
         *  for when a user first creates a new Transaction. Without this, as soon as a
         *  user enters 1 number, a decimal point is added plus "00" and will move the
         *  cursor to the end of the EditText.
         *
         *  @param  string the string to be formatted .
         *  @return the formatted string.
         */
        private fun formatInteger(string : String ) : String {

            val parsed = BigDecimal(string)
            // every three numbers, a symbol will be added, which is currently set to US
            // locale ",". Plan to make settings where user can select symbols
            val formatter = DecimalFormat("#,###", customSymbols)
            return formatter.format(parsed)
        }

        /**
         *  Will format string with thousands separators.
         *
         *  @param  string the string to be formatted.
         *  @return the formatted string.
         */
        private fun formatDecimal(string : String) : String {

            if (string == customSymbols.decimalSeparator.toString()) {

                return customSymbols.decimalSeparator.toString()
            }
            val parsed = BigDecimal(string)
            // every three numbers, a symbol will be added, which is currently set to US
            // locale ",". Plan to make settings where user can select symbols
            val formatter = DecimalFormat("#,###." + getDecimalPattern(string), customSymbols)
            formatter.roundingMode = RoundingMode.DOWN
            return formatter.format(parsed)
        }

        /**
         *  It will return suitable pattern for format decimal.
         *  For example: 10.2 -> return 0 | 10.23 -> return 00, | 10.235 -> return 000
         */
        private fun getDecimalPattern(string : String) : String {

            // returns number of characters after decimal point
            val decimalCount : Int = string.length - string.indexOf(customSymbols.decimalSeparator) - 1
            val decimalPattern     = StringBuilder()
            var i = 0
            while (i < decimalCount && i < 2) {

                decimalPattern.append("0")
                i++
            }
            return decimalPattern.toString()
        }

        /**
         *  Handles where cursor goes after input.
         *
         *  As long as length is less than MAX_LENGTH, cursor will move to the end of the string.
         */
        private fun handleSelection() {

            if (editText.text.length <= MAX_LENGTH) {

                editText.setSelection(editText.text.length)
            } else {

                editText.setSelection(MAX_LENGTH)
            }
        }
    }

    companion object {

        private const val MAX_LENGTH = 15
        private val customSymbols    = DecimalFormatSymbols(Locale.US)
    }
}