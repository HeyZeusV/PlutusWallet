package com.heyzeusv.financeapplication.utilities

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import android.widget.EditText
import androidx.appcompat.R
import com.heyzeusv.financeapplication.utilities.PreferenceHelper.get

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

    // SharedPreference
    private val sharedPreferences  : SharedPreferences = PreferenceHelper.sharedPrefs(context)

    // keys from separator symbols
    private var decimalSymbolKey   : String = sharedPreferences[KEY_DECIMAL_SYMBOL  , "period"]!!
    private var thousandsSymbolKey : String = sharedPreferences[KEY_THOUSANDS_SYMBOL, "comma" ]!!

    init {

        // retrieves separator symbols from keys
        decimalSymbol   = Utils.getSeparatorSymbol(decimalSymbolKey)
        thousandsSymbol = Utils.getSeparatorSymbol(thousandsSymbolKey)
        // chars that this will accept
        var accepted = "0123456789"
        accepted += decimalSymbol
        // forces numpad
        this.setRawInputType(Configuration.KEYBOARD_QWERTY)
        // only allows char in accepted to be pressed
        this.keyListener = DigitsKeyListener.getInstance(accepted)
        // numeric text class with decimal flag
        this.hint      = context.getString(com.heyzeusv.financeapplication.R.string.transaction_total_hint)
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

            // cleanString: doesn't contain thousands separator
            val cleanString : String = string.replace(("[$thousandsSymbol]").toRegex(), "")

            // prevents afterTextChanged recursive call
            if (cleanString == previousCleanString || cleanString.isEmpty()) {

                return
            }

            previousCleanString = cleanString
            // formats string depending if there is decimal places
            val formattedString : String =
                if (cleanString.contains(decimalSymbol)) {

                    Utils.formatDecimal(cleanString, thousandsSymbol, decimalSymbol)
                } else {

                    Utils.formatInteger(cleanString, thousandsSymbol)
                }
            editText.removeTextChangedListener(this)
            editText.setText(formattedString)
            // sets location of cursor
            handleSelection()
            editText.addTextChangedListener(this)
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
        private var decimalSymbol   : Char = '.'
        private var thousandsSymbol : Char = ','
    }
}