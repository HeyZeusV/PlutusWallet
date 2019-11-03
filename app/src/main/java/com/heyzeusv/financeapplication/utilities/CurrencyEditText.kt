package com.heyzeusv.financeapplication.utilities

import android.content.Context
import android.graphics.Rect
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.EditText
import androidx.appcompat.R
import java.lang.StringBuilder
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

// for future localization use: https://stackoverflow.com/a/33496285/9825089

// USED THIS: https://stackoverflow.com/a/45299775/9825089

/**
 *  Custom EditText to handle currency.
 *
 *  Adds currency symbol prefix, adds thousands separators automatically, and limits the
 *  number of decimal places.
 *  Always use locale US instead of default to make DecimalFormat work well in all language.
 *
 *  @constructor standard EditText constructor
 */
class CurrencyEditText @JvmOverloads constructor(
    context : Context, attributeSet : AttributeSet,
    defStyleAttr : Int = R.attr.editTextStyle) :
    androidx.appcompat.widget.AppCompatEditText(context, attributeSet, defStyleAttr) {

    private val currencyTextWatcher = CurrencyTextWatcher(this)

    init {

        // numeric text class with decimal flag
        this.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        this.hint      = context.getString(com.heyzeusv.financeapplication.R.string.transaction_total_hint)
        // sets max length
        this.filters   = arrayOf<InputFilter>(InputFilter.LengthFilter(MAX_LENGTH))
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
     *  @constructor sets EditText to be watched and its prefix.
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
            val cleanString : String = string.replace(("[,]").toRegex(), "")

            // prevents afterTextChanged recursive call
            if (cleanString == previousCleanString || cleanString.isEmpty()) {

                return
            }

            previousCleanString = cleanString
            // formats string depending if there is decimal places
            val formattedString : String =
                if (cleanString.contains(".")) {

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
         *  Will format string with thousands separators and prefix.
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
            val formatter = DecimalFormat("#,###", DecimalFormatSymbols.getInstance(Locale.US))
            return formatter.format(parsed)
        }

        /**
         *  Will format string with thousands separators and prefix.
         *
         *  @param  string the string to be formatted.
         *  @return the formatted string.
         */
        private fun formatDecimal(string : String) : String {

            if (string == ".") {

                return "."
            }
            val parsed = BigDecimal(string)
            // every three numbers, a symbol will be added, which is currently set to US
            // locale ",". Plan to make settings where user can select symbols
            val formatter = DecimalFormat("#,###." + getDecimalPattern(string), DecimalFormatSymbols.getInstance(Locale.US))
            formatter.roundingMode = RoundingMode.DOWN
            return formatter.format(parsed)
        }

        /**
         *  It will return suitable pattern for format decimal.
         *  For example: 10.2 -> return 0 | 10.23 -> return 00, | 10.235 -> return 000
         */
        private fun getDecimalPattern(string : String) : String {

            // returns number of characters after decimal point
            val decimalCount : Int = string.length - string.indexOf(".") - 1
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
    }
}