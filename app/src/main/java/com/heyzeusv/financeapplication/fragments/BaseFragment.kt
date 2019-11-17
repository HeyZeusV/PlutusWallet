package com.heyzeusv.financeapplication.fragments

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

const val KEY_CURRENCY_SYMBOL = "key_currency_symbol"
const val KEY_DECIMAL_PLACES  = "key_decimal_places"
const val KEY_DECIMAL_SYMBOL   = "key_decimal_symbol"
const val KEY_MAX_ID          = "key_max_id"
const val KEY_SYMBOL_SIDE     = "key_symbol_side"
const val KEY_THOUSANDS_SYMBOL = "key_thousands_symbol"

/**
 *  Base Fragment that all other Fragments will extend.
 *
 *  Contains variables needed to run CoRoutines on two different Contexts and to stop
 *  any Jobs once Fragment is stopped.
 */
abstract class BaseFragment : Fragment(), CoroutineScope {

    private lateinit var job : Job

    // SharedPreferences
    protected lateinit var sharedPreferences : SharedPreferences
    protected lateinit var editor            : SharedPreferences.Editor

    override val coroutineContext : CoroutineContext
        get() = job + Dispatchers.Main

    val ioContext : CoroutineContext
        get() = job + Dispatchers.IO

    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)

        job = Job()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        editor            = sharedPreferences.edit()
    }

    override fun onDestroy() {
        super.onDestroy()

        job.cancel()
    }
}