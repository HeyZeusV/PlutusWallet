package com.heyzeusv.financeapplication.fragments

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.heyzeusv.financeapplication.utilities.PreferenceHelper
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

    private lateinit var job : Job

    // SharedPreferences
    protected lateinit var sharedPreferences : SharedPreferences

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

    override fun onDestroy() {
        super.onDestroy()

        job.cancel()
    }
}