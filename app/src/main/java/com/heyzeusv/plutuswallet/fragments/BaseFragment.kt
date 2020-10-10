package com.heyzeusv.plutuswallet.fragments

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.heyzeusv.plutuswallet.database.entities.SettingsValues
import com.heyzeusv.plutuswallet.utilities.PreferenceHelper
import com.heyzeusv.plutuswallet.utilities.SettingsUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 *  Base Fragment that all other Fragments will extend.
 *
 *  Contains variables needed to run CoRoutines on two different Contexts and to stop
 *  any Jobs once Fragment is stopped.
 */
abstract class BaseFragment : Fragment(), CoroutineScope {

    private lateinit var job: Job

    // SharedPreferences
    protected lateinit var sharedPref: SharedPreferences

    protected var setVals: SettingsValues = SettingsValues()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        job = Job()

        sharedPref = PreferenceHelper.sharedPrefs(requireActivity())
        setVals = SettingsUtils.prepareSettingValues(sharedPref)
    }

    override fun onDestroy() {
        super.onDestroy()

        job.cancel()
    }
}