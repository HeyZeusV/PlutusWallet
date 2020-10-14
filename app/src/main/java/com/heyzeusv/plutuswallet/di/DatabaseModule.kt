package com.heyzeusv.plutuswallet.di

import android.content.Context
import androidx.room.Room
import com.heyzeusv.plutuswallet.database.Migrations
import com.heyzeusv.plutuswallet.database.TransactionDatabase
import com.heyzeusv.plutuswallet.database.daos.AccountDao
import com.heyzeusv.plutuswallet.database.daos.CategoryDao
import com.heyzeusv.plutuswallet.database.daos.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

/**
 *  Hilt Module that tells Hilt how to provide Database and DAOs where they are injected.
 */
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

    @Provides
    fun provideTranDao(database: TransactionDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideCatDao(database: TransactionDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideAccDao(database: TransactionDatabase): AccountDao = database.accountDao()
}