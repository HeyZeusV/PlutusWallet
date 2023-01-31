 package com.heyzeusv.plutuswallet.ui.base

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 *  Base Fragment that all other Fragments will extend.
 *
 *  Contains variables needed to run CoRoutines on two different Contexts and to stop
 *  any Jobs once Fragment is stopped.
 */
@AndroidEntryPoint
abstract class BaseFragment : Fragment(), CoroutineScope {

    private lateinit var job: Job

    // SharedPreferences
    @Inject protected lateinit var sharedPref: SharedPreferences

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        job = Job()
    }

    override fun onDestroy() {
        super.onDestroy()

        job.cancel()
    }
}