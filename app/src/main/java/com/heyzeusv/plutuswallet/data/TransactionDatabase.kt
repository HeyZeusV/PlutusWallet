package com.heyzeusv.plutuswallet.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.heyzeusv.plutuswallet.data.daos.AccountDao
import com.heyzeusv.plutuswallet.data.daos.CategoryDao
import com.heyzeusv.plutuswallet.data.daos.TransactionDao
import com.heyzeusv.plutuswallet.data.model.Account
import com.heyzeusv.plutuswallet.data.model.Category
import com.heyzeusv.plutuswallet.data.model.Transaction

/**
 *  Database layer over the SQLite database.
 *
 *  @Database entities: entities used in this database, version: schema version (must be
 *            incremented on schema change, exportSchema: will create json file of schema.
 *  @TypeConverters database can only store certain types, need TypeConverters to convert
 *                  types into those that database can store.
 */
@Database(entities = [Account::class,
                      Category::class,
                      Transaction::class],
          version = 23,
          exportSchema = true)
@TypeConverters(TransactionTypeConverters::class)
abstract class TransactionDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
}