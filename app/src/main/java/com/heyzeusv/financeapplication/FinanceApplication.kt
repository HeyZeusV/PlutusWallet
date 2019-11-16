package com.heyzeusv.financeapplication

import android.app.Application

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

        // call TransactionRepository here since we only ever need one
        TransactionRepository.initialize(this)
    }
}