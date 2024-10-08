package com.heyzeusv.plutuswallet.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.heyzeusv.plutuswallet.data.model.SettingsValues
import com.heyzeusv.plutuswallet.util.prepareSettingValues
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import javax.inject.Singleton

/**
 *  Hilt module that will replace AppModule when performing tests in androidTest folder.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
object TestAppModule {

    // will provide a SharedPreference that has been cleared of previous entries
    @Provides
    @Singleton
    fun provideTestSharedPreference(@ApplicationContext appContext: Context): SharedPreferences {
        val sp = PreferenceManager.getDefaultSharedPreferences(appContext)
        val editor = sp.edit()
        editor.clear()
        editor.commit()
        return sp
    }

    @Provides
    @Singleton
    fun provideTestSettingsValues(sharedPref: SharedPreferences): SettingsValues {
        return prepareSettingValues(sharedPref)
    }

    @Provides
    @Singleton
    fun provideClock(): Clock {
        return Clock.fixed(
            Instant.parse("1980-01-10T00:00:00Z"),
            ZoneOffset.systemDefault()
        )
    }
}