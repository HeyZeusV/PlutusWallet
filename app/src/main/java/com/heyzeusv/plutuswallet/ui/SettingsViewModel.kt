package com.heyzeusv.plutuswallet.ui

import androidx.lifecycle.ViewModel
import com.heyzeusv.plutuswallet.data.model.SettingsValues
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DateFormat
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class SettingsViewModel @Inject constructor(
    var setVals: SettingsValues
) : ViewModel() {

    /**
     *  Don't need StateFlows for theme/language Settings because recreate() is called on those
     */
    private val _currencySymbol = MutableStateFlow(setVals.currencySymbol)
    val currencySymbol: StateFlow<String> get() = _currencySymbol
    fun updateCurrencySymbol(newSymbol: String) { _currencySymbol.value = newSymbol }
    
    private val _currencySymbolSide = MutableStateFlow(setVals.currencySymbolSide)
    val currencySymbolSide: StateFlow<String> get() = _currencySymbolSide
    fun updateCurrencySymbolSide(newSide: String) { _currencySymbolSide.value = newSide }

    private val _thousandsSymbol = MutableStateFlow(setVals.thousandsSymbol)
    val thousandsSymbol: StateFlow<Char> get() = _thousandsSymbol
    fun updateThousandsSymbol(newSymbol: Char) { _thousandsSymbol.value = newSymbol }

    private val _decimalSymbol = MutableStateFlow(setVals.decimalSymbol)
    val decimalSymbol: StateFlow<Char> get() = _decimalSymbol
    fun updateDecimalSymbol(newSymbol: Char) { _decimalSymbol.value = newSymbol }

    private val _decimalNumber = MutableStateFlow(setVals.decimalNumber)
    val decimalNumber: StateFlow<String> get() = _decimalNumber
    fun updateDecimalNumber(format: String) { _decimalNumber.value = format }

    private val _dateFormatter = MutableStateFlow(setVals.dateFormatter)
    val dateFormatter: StateFlow<DateFormat> get() = _dateFormatter
    fun updateDateFormatter(format: Int) {
        _dateFormatter.value = DateFormat.getDateInstance(format)
    }
}