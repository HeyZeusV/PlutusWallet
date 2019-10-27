package com.heyzeusv.financeapplication

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 *  Maintains global application state and used to call one-time operations.
 */
class FinanceApplication : Application(){

    /**
     *  This gets called only once, when application is first started.
     *  This is the perfect place to call one-time initialization operations.
     */
    override fun onCreate() {
        super.onCreate()

        context = this
        // call TransactionRepository here since we only ever need one
        TransactionRepository.initialize(this)
    }

    /**
     *  Allows us to call context from anywhere, so can get resources where needed.
     */
    companion object {

        @SuppressLint("StaticFieldLeak")
        var context : Context? = null
            private set
    }
}