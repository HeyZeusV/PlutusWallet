package com.heyzeusv.plutuswallet.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn


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
    fun provideTestSharedPreference(@ApplicationContext appContext: Context): SharedPreferences {

        val sp = PreferenceManager.getDefaultSharedPreferences(appContext)
        val editor = sp.edit()
        editor.clear()
        editor.commit()
        return sp
    }
}
