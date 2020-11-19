package com.heyzeusv.plutuswallet.di

import android.content.Context
import androidx.room.Room
import com.heyzeusv.plutuswallet.data.Migrations
import com.heyzeusv.plutuswallet.data.Repository
import com.heyzeusv.plutuswallet.data.TransactionDatabase
import com.heyzeusv.plutuswallet.data.TransactionRepository
import com.heyzeusv.plutuswallet.data.daos.AccountDao
import com.heyzeusv.plutuswallet.data.daos.CategoryDao
import com.heyzeusv.plutuswallet.data.daos.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 *  Tells Hilt how to provide Database and DAOs.
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

    @Provides
    fun provideRepository(
        accDao: AccountDao,
        catDao: CategoryDao,
        tranDao: TransactionDao
    ): Repository {

        return TransactionRepository(accDao, catDao, tranDao)
    }
}