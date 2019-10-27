package com.heyzeusv.financeapplication.utilities

import android.os.Bundle
import androidx.fragment.app.Fragment
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

    override val coroutineContext : CoroutineContext
        get() = job + Dispatchers.Main

    val ioContext : CoroutineContext
        get() = job + Dispatchers.IO

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)

        job = Job()
    }

    override fun onDestroy() {
        super.onDestroy()

        job.cancel()
    }
}