package com.heyzeusv.plutuswallet.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.heyzeusv.plutuswallet.data.model.SettingsValues
import com.heyzeusv.plutuswallet.util.prepareSettingValues
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

/**
 *  Tells Hilt how to provide SharedPreferences.
 */
@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Provides
    fun provideSharedPreference(@ApplicationContext appContext: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(appContext)
    }

    @Singleton
    @Provides
    fun provideSettingsValues(sharedPref: SharedPreferences): SettingsValues {
        return prepareSettingValues(sharedPref)
    }

    @Singleton
    @Provides
    fun provideClock(): Clock {
        return Clock.systemDefaultZone()
    }
}