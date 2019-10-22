package com.heyzeusv.financeapplication.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.heyzeusv.financeapplication.ExpenseCategory
import com.heyzeusv.financeapplication.IncomeCategory
import com.heyzeusv.financeapplication.Transaction

@Database(entities = [Transaction::class, ExpenseCategory::class, IncomeCategory::class],
    version = 13, exportSchema = true)
@TypeConverters(TransactionTypeConverters::class)
abstract class TransactionDatabase : RoomDatabase() {

    abstract fun transactionDao      () : TransactionDao
    abstract fun expenseCategoryDao  () : ExpenseCategoryDao
    abstract fun incomeCategoryDao   () : IncomeCategoryDao
}