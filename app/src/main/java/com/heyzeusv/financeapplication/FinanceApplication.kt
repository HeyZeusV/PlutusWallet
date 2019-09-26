package com.heyzeusv.financeapplication

import android.app.Application

class FinanceApplication : Application(){

    // this gets called only once, when application is first started,
    // so perfect place to call one-time initialization operations
    override fun onCreate() {
        super.onCreate()

        // call TransactionRepository here since we only ever need one
        TransactionRepository.initialize(this)
    }
}