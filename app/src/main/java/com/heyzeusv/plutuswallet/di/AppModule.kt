package com.heyzeusv.plutuswallet.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 *  Tells Hilt how to provide SharedPreferences.
 */
@InstallIn(ApplicationComponent::class)
@Module
object AppModule {

    @Provides
    fun provideSharedPreference(@ApplicationContext appContext: Context): SharedPreferences {

        return PreferenceManager.getDefaultSharedPreferences(appContext)
    }
}