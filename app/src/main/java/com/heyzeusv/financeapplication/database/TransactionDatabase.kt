package com.heyzeusv.financeapplication.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.heyzeusv.financeapplication.database.daos.ExpenseCategoryDao
import com.heyzeusv.financeapplication.database.daos.IncomeCategoryDao
import com.heyzeusv.financeapplication.database.daos.TransactionDao
import com.heyzeusv.financeapplication.database.entities.ExpenseCategory
import com.heyzeusv.financeapplication.database.entities.IncomeCategory
import com.heyzeusv.financeapplication.database.entities.Transaction

/**
 *  Database layer over the SQLite database.
 *
 *  @Database entities: entities used in this database, version: schema version (must be
 *            incremented on schema change, exportSchema: will create json file of schema.
 *  @TypeConverters database can only store certain types, need TypeConverters to convert
 *                  types into those that database can store.
 */
@Database(entities = [Transaction::class, ExpenseCategory::class, IncomeCategory::class],
    version = 15, exportSchema = true)
@TypeConverters(TransactionTypeConverters::class)
abstract class TransactionDatabase : RoomDatabase() {

    abstract fun transactionDao    () : TransactionDao
    abstract fun expenseCategoryDao() : ExpenseCategoryDao
    abstract fun incomeCategoryDao () : IncomeCategoryDao
}