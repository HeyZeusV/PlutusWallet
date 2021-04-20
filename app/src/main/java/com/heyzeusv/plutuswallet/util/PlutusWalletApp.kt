package com.heyzeusv.plutuswallet.util

import android.app.Application
import com.heyzeusv.plutuswallet.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 *  Maintains global application state and used to call one-time operations.
 */
@HiltAndroidApp
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

        val dateTest = Date()
        val dateToZone = ZonedDateTime.ofInstant(dateTest.toInstant(), ZoneId.systemDefault())
        val zonedTest = ZonedDateTime.now(ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - hh:mm:ss a Z")
        val zonedString = zonedTest.format(formatter)
        val dateString = dateToZone.format(formatter)


        Timber.d("Instant: ${Instant.ofEpochMilli(System.currentTimeMillis())}")
        Timber.d("Date to Long: ${Date().time}")
        Timber.d("System: ${System.currentTimeMillis()}")
        Timber.d("Zoned to Instant to Long: ${zonedTest.toInstant().toEpochMilli()}")
        Timber.d("date to Zoned: $dateString")
        Timber.d("Zoned: $zonedString")
    }
}