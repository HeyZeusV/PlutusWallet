package com.heyzeusv.plutuswallet.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.heyzeusv.plutuswallet.data.model.SettingsValues
import com.heyzeusv.plutuswallet.util.SettingsUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

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

    @Provides
    fun provideSettingsValues(@ApplicationContext appContext: Context): SettingsValues {

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext)
        return SettingsUtils.prepareSettingValues(sharedPref)
    }
}