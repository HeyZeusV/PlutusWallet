package com.heyzeusv.plutuswallet

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication
import timber.log.Timber

class CustomTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {

        // plant Timber tree
        if (BuildConfig.DEBUG) Timber.plant(object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                super.log(priority, "PW_$tag", message, t)
            }
        })

        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}