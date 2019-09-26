package com.heyzeusv.financeapplication.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.heyzeusv.financeapplication.Transaction

@Database(entities = [Transaction::class], version = 1, exportSchema = true)
@TypeConverters(TransactionTypeConverters::class)
abstract class TransactionDatabase : RoomDatabase() {

    abstract fun transactionDao() : TransactionDao
}