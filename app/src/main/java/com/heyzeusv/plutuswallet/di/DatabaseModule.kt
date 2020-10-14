package com.heyzeusv.plutuswallet.di

import android.content.Context
import androidx.room.Room
import com.heyzeusv.plutuswallet.database.Migrations
import com.heyzeusv.plutuswallet.database.TransactionDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): TransactionDatabase {

        return Room.databaseBuilder(
            appContext,
            TransactionDatabase::class.java,
            "transaction-database"
        )
            .addMigrations(Migrations.migration16to22, Migrations.migration22to23)
            .build()
    }
}