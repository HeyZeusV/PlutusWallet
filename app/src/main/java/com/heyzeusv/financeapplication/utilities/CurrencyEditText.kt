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
 *  Always use locale US instead of default to make DecimalFormat
 *  work well in all language
 */

class CurrencyEditText @JvmOverloads constructor(
    context : Context, attributeSet : AttributeSet,
    defStyleAttr : Int = R.attr.editTextStyle) :
    androidx.appcompat.widget.AppCompatEditText(context, attributeSet, defStyleAttr) {

    private val currencyTextWatcher = CurrencyTextWatcher(this, prefix)

    init {

        // numeric text class with decimal flag
        this.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        this.hint      = prefix
        // sets max length of EditText
        this.filters   = arrayOf<InputFilter>(InputFilter.LengthFilter(MAX_LENGTH))
    }

    override fun onFocusChanged(focused : Boolean, direction : Int, previouslyFocusRect : Rect?) {

        super.onFocusChanged(focused, direction, previouslyFocusRect)

        if (focused) {

            this.addTextChangedListener(currencyTextWatcher)
        } else {

            this.removeTextChangedListener(currencyTextWatcher)
        }
        handleCaseCurrencyEmpty(focused)
    }

    /**
     *  When currency empty
     *  When focus EditText, set the default text = prefix (ex: $)
     *  When EditText lose focus, set the default text = "", EditText will display hint (ex: $)
     */
    private fun handleCaseCurrencyEmpty(focused : Boolean) {

        if (focused) {

            if (text.toString().isEmpty()) {

                setText(prefix)
            }
        } else {

            if (text.toString() == prefix) {

                setText("")
            }
        }
    }

    private class CurrencyTextWatcher
    internal constructor(private val editText : EditText, private val prefix : String) : TextWatcher {

        private var previousCleanString : String = ""

        override fun beforeTextChanged(s : CharSequence?, start : Int, count : Int, after : Int) {}

        override fun onTextChanged(s : CharSequence?, start : Int, before : Int, count : Int) {}

        override fun afterTextChanged(editable : Editable?) {

            val string : String = editable.toString()
            // if EditText is empty
            if (string.length < prefix.length) {

                editText.setText(prefix)
                editText.setSelection(prefix.length)
                return
            }
            // do nothing if string is prefix
            if (string == prefix) {

                return
            }
            // cleanString: doesn't contain prefix and ,
            val cleanString : String = string.replace(prefix, "").replace(("[,]").toRegex(), "")
            // prevents afterTextChanged recursive call
            if (cleanString == previousCleanString || cleanString.isEmpty()) {

                return
            }
            previousCleanString = cleanString
            val formattedString : String

            formattedString = if (cleanString.contains(".")) {

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

        /*
         *  Even though Totals will always be a decimal number, formatInteger is needed
         *  for when a user first creates a new Transaction. Without this, as soon as a
         *  user enters 1 number, a decimal point is added plus "00" and will move the
         *  cursor to the end of the EditText.
         */
        private fun formatInteger(string : String ) : String {

            // BigDecimal cannot handle symbols other than "."
            val editedString : String = string.removePrefix("$")
            val parsed = BigDecimal(editedString)
            // every three numbers, a symbol will be added, which is currently set to US
            // locale ",". Plan to make settings where user can select symbols
            val formatter = DecimalFormat("$prefix#,###", DecimalFormatSymbols.getInstance(Locale.US))
            return formatter.format(parsed)
        }

        private fun formatDecimal(string : String) : String {

            // BigDecimal cannot handle symbols other than "."
            val editedString : String = string.removePrefix("$")
            if (editedString == ".") {

                return "$prefix."
            }
            val parsed = BigDecimal(editedString)
            // every three numbers, a symbol will be added, which is currently set to US
            // locale ",". Plan to make settings where user can select symbols
            val formatter = DecimalFormat(prefix + "#,###." + getDecimalPattern(string), DecimalFormatSymbols.getInstance(Locale.US))
            formatter.roundingMode = RoundingMode.DOWN
            return formatter.format(parsed)
        }

        /**
         * It will return suitable pattern for format decimal
         * For example: 10.2 -> return 0 | 10.23 -> return 00, | 10.235 -> return 000
         */
        private fun getDecimalPattern(string : String) : String {

            val decimalCount : Int = string.length - string.indexOf(".") - 1
            val decimalPattern = StringBuilder()
            var i = 0
            while (i < decimalCount && i < MAX_DECIMAL) {

                decimalPattern.append("0")
                i++
            }
            return decimalPattern.toString()
        }

        // handles where cursor goes after input
        private fun handleSelection() {

            if (editText.text.length <= MAX_LENGTH) {

                editText.setSelection(editText.text.length)
            } else {

                editText.setSelection(MAX_LENGTH)
            }
        }
    }

    companion object {
        private const val prefix = "$"
        private const val MAX_LENGTH = 15
        private const val MAX_DECIMAL = 2
    }
}