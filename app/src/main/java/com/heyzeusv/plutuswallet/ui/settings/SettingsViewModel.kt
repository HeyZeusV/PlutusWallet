package com.heyzeusv.plutuswallet.ui.settings

import androidx.lifecycle.ViewModel
import com.heyzeusv.plutuswallet.data.model.SettingsValues
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.RoundingMode
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val decimalPattern = "#,###.00"
private const val integerPattern = "#,###"

@HiltViewModel
class SettingsViewModel @Inject constructor(
    settingsValues: SettingsValues
) : ViewModel() {

    private val customSymbols = DecimalFormatSymbols(Locale.US)

    private val _setVals = MutableStateFlow(settingsValues)
    val setVals: StateFlow<SettingsValues> get() = _setVals
    private fun updateSetVals(setVals: SettingsValues) { _setVals.value = setVals }
    /**
     *  Don't need StateFlows for theme/language Settings because recreate() is called on those
     */

    fun updateCurrencySymbol(newSymbol: String) {
        updateSetVals(_setVals.value.copy(currencySymbol = newSymbol))
    }

    fun updateCurrencySymbolSide(newSide: String) {
        updateSetVals(_setVals.value.copy(currencySymbolSide = newSide))
    }

    fun updateNumberSymbols(newThousands: Char, newDecimal: Char) {
        customSymbols.groupingSeparator = newThousands
        customSymbols.decimalSeparator = newDecimal
        updateSetVals(_setVals.value.copy(
            thousandsSymbol = newThousands,
            decimalSymbol = newDecimal,
            decimalFormatter = DecimalFormat(decimalPattern, customSymbols).apply {
                roundingMode = RoundingMode.HALF_UP
            },
            integerFormatter = DecimalFormat(integerPattern, customSymbols).apply {
                roundingMode = RoundingMode.HALF_UP
            }
        ))
    }

    fun updateThousandsSymbol(newSymbol: Char) {
        customSymbols.groupingSeparator = newSymbol
        updateSetVals(_setVals.value.copy(
            thousandsSymbol = newSymbol,
            decimalFormatter = DecimalFormat(decimalPattern, customSymbols).apply {
                roundingMode = RoundingMode.HALF_UP
            },
            integerFormatter = DecimalFormat(integerPattern, customSymbols).apply {
                roundingMode = RoundingMode.HALF_UP
            }
        ))
    }

    fun updateDecimalSymbol(newSymbol: Char) {
        customSymbols.decimalSeparator = newSymbol
        updateSetVals(_setVals.value.copy(
            decimalSymbol = newSymbol,
            decimalFormatter = DecimalFormat(decimalPattern, customSymbols).apply {
                roundingMode = RoundingMode.HALF_UP
            },
            integerFormatter = DecimalFormat(integerPattern, customSymbols).apply {
                roundingMode = RoundingMode.HALF_UP
            }
        ))
    }

    fun updateDecimalNumber(format: String) {
        updateSetVals(_setVals.value.copy(decimalNumber = format))
    }

    fun updateDateFormatter(format: Int) {
        updateSetVals(_setVals.value.copy(dateFormatter = DateFormat.getDateInstance(format)))
    }
}