package com.heyzeusv.plutuswallet.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.heyzeusv.plutuswallet.database.daos.AccountDao
import com.heyzeusv.plutuswallet.database.daos.CategoryDao
import com.heyzeusv.plutuswallet.database.daos.ExpenseCategoryDao
import com.heyzeusv.plutuswallet.database.daos.IncomeCategoryDao
import com.heyzeusv.plutuswallet.database.daos.TransactionDao
import com.heyzeusv.plutuswallet.database.entities.Account
import com.heyzeusv.plutuswallet.database.entities.Category
import com.heyzeusv.plutuswallet.database.entities.ExpenseCategory
import com.heyzeusv.plutuswallet.database.entities.IncomeCategory
import com.heyzeusv.plutuswallet.database.entities.Transaction

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
                      Transaction::class,
                      ExpenseCategory::class,
                      IncomeCategory::class],
          version = 21,
          exportSchema = true)
@TypeConverters(TransactionTypeConverters::class)
abstract class TransactionDatabase : RoomDatabase() {

    abstract fun accountDao        () : AccountDao
    abstract fun categoryDao       () : CategoryDao
    abstract fun transactionDao    () : TransactionDao
    abstract fun expenseCategoryDao() : ExpenseCategoryDao
    abstract fun incomeCategoryDao () : IncomeCategoryDao
}