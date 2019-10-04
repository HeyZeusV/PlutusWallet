package com.heyzeusv.financeapplication.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.heyzeusv.financeapplication.Category
import com.heyzeusv.financeapplication.Transaction

@Database(entities = [Transaction::class, Category::class],
    version = 2, exportSchema = true)
@TypeConverters(TransactionTypeConverters::class)
abstract class TransactionDatabase : RoomDatabase() {

    abstract fun transactionDao() : TransactionDao

    abstract fun categoryDao() : CategoryDao
}