package com.heyzeusv.financeapplication.fragments

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.heyzeusv.financeapplication.utilities.*
import com.heyzeusv.financeapplication.utilities.PreferenceHelper.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 *  Base Fragment that all other Fragments will extend.
 *
 *  Contains variables needed to run CoRoutines on two different Contexts and to stop
 *  any Jobs once Fragment is stopped.
 */
abstract class BaseFragment : Fragment(), CoroutineScope {

    private lateinit var job : Job

    // SharedPreferences
    private lateinit var sharedPreferences : SharedPreferences

    // values from SharedPreferences
    protected var decimalPlaces   : Boolean = true
    protected var symbolSide      : Boolean = true
    protected var decimalSymbol   : Char    = ' '
    protected var thousandsSymbol : Char    = ' '
    protected var dateFormat      : Int     = 0
    protected var currencySymbol  : String  = ""

    // used to hold decimal/thousands symbols
    private var customSymbols = DecimalFormatSymbols(Locale.US)

    // formatters for Total
    protected var decimalFormatter = DecimalFormat("", customSymbols)
    protected var integerFormatter = DecimalFormat("", customSymbols)

    override val coroutineContext : CoroutineContext
        get() = job + Dispatchers.Main

    val ioContext : CoroutineContext
        get() = job + Dispatchers.IO

    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)

        job = Job()

        sharedPreferences = PreferenceHelper.sharedPrefs(activity!!)
    }

    override fun onResume() {
        super.onResume()

        // retrieving SharedPreferences values
        val currencySymbolKey  : String = sharedPreferences[KEY_CURRENCY_SYMBOL , "dollar"]!!
        val dateFormatKey      : String = sharedPreferences[KEY_DATE_FORMAT     , "0"     ]!!
        val decimalSymbolKey   : String = sharedPreferences[KEY_DECIMAL_SYMBOL  , "period"]!!
        val thousandsSymbolKey : String = sharedPreferences[KEY_THOUSANDS_SYMBOL, "comma" ]!!
        decimalPlaces = sharedPreferences[KEY_DECIMAL_PLACES, true]!!
        symbolSide    = sharedPreferences[KEY_SYMBOL_SIDE   , true]!!

        // converting keys to values
        currencySymbol  = Utils.getCurrencySymbol (currencySymbolKey )
        dateFormat      = Utils.getDateFormat     (dateFormatKey     )
        decimalSymbol   = Utils.getSeparatorSymbol(decimalSymbolKey  )
        thousandsSymbol = Utils.getSeparatorSymbol(thousandsSymbolKey)

        // placing symbols
        customSymbols.decimalSeparator  = decimalSymbol
        customSymbols.groupingSeparator = thousandsSymbol

        // remaking formatters with new symbols
        decimalFormatter = DecimalFormat("#,##0.00", customSymbols)
        integerFormatter = DecimalFormat("#,###"   , customSymbols)
    }

    override fun onDestroy() {
        super.onDestroy()

        job.cancel()
    }
}