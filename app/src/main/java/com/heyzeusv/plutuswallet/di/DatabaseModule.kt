package com.heyzeusv.plutuswallet.di

import android.content.Context
import androidx.room.Room
import com.heyzeusv.plutuswallet.data.Migrations
import com.heyzeusv.plutuswallet.data.PWRepositoryInterface
import com.heyzeusv.plutuswallet.data.PWDatabase
import com.heyzeusv.plutuswallet.data.PWRepository
import com.heyzeusv.plutuswallet.data.daos.AccountDao
import com.heyzeusv.plutuswallet.data.daos.CategoryDao
import com.heyzeusv.plutuswallet.data.daos.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.annotation.AnnotationRetention.RUNTIME

/**
 *  Tells Hilt how to provide Database and DAOs.
 */
@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Qualifier
    @Retention(RUNTIME)
    annotation class TranDao

    @Qualifier
    @Retention(RUNTIME)
    annotation class CatDao

    @Qualifier
    @Retention(RUNTIME)
    annotation class AccDao

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): PWDatabase {

        return Room.databaseBuilder(
            appContext,
            PWDatabase::class.java,
            "transaction-database"
        )
            .addMigrations(
                Migrations.migration16to22,
                Migrations.migration22to23,
                Migrations.migration23to24)
            .build()
    }

    @TranDao
    @Provides
    fun provideTranDao(database: PWDatabase): TransactionDao = database.transactionDao()

    @CatDao
    @Provides
    fun provideCatDao(database: PWDatabase): CategoryDao = database.categoryDao()

    @AccDao
    @Provides
    fun provideAccDao(database: PWDatabase): AccountDao = database.accountDao()
}

/**
 * The binding for Repository is on its own module so that we can replace it easily in tests.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideRepository(
        @DatabaseModule.AccDao accDao: AccountDao,
        @DatabaseModule.CatDao catDao: CategoryDao,
        @DatabaseModule.TranDao tranDao: TransactionDao
    ): PWRepositoryInterface {

        return PWRepository(accDao, catDao, tranDao)
    }
}