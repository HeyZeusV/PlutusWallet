package com.heyzeusv.plutuswallet.utilities

import android.app.Application
import com.heyzeusv.plutuswallet.BuildConfig
import com.heyzeusv.plutuswallet.database.TransactionRepository
import timber.log.Timber

/**
 *  Maintains global application state and used to call one-time operations.
 */
class PlutusWalletApp : Application() {

    /**
     *  This gets called only once, when application is first started.
     *  This is the perfect place to call one-time initialization operations.
     */
    override fun onCreate() {
        super.onCreate()

        // plant Timber tree
        if (BuildConfig.DEBUG) Timber.plant(object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                super.log(priority, "PW_$tag", message, t)
            }
        })

        // call TransactionRepository here since we only ever need one
        TransactionRepository.initialize(this)
    }
}