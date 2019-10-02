package com.heyzeusv.financeapplication

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.heyzeusv.financeapplication.database.TransactionDatabase
import java.lang.IllegalStateException
import java.util.concurrent.Executors

private const val DATABASE_NAME = "transaction-database"

class TransactionRepository private constructor(context : Context){

    // Context Object, Database class, Database Name
    private val database : TransactionDatabase = Room.databaseBuilder(
        context.applicationContext,
        TransactionDatabase::class.java,
        DATABASE_NAME
    ).build()

    private val transactionDao = database.transactionDao()
    // starts background thread to run update and insert
    private val executor = Executors.newSingleThreadExecutor()

    // need one for each function in DAO
    fun getTransactions() : LiveData<List<Transaction>> = transactionDao.getTransactions()

    fun getTransaction(id : Int) : LiveData<Transaction?> = transactionDao.getTransaction(id)

    fun update(transaction : Transaction) {

        executor.execute {

            transactionDao.update(transaction)
        }
    }

    fun insert(transaction : Transaction) {

        executor.execute {

            transactionDao.insert(transaction)
        }
    }

    fun delete(transaction : Transaction) {

        executor.execute {

            transactionDao.delete(transaction)
        }
    }

    companion object {

        private var INSTANCE : TransactionRepository? = null

        // needed to make repository a singleton (only ever one instance)
        fun initialize(context : Context) {

            if (INSTANCE == null) {

                INSTANCE = TransactionRepository(context)
            }
        }

        fun get() : TransactionRepository {

            return INSTANCE ?:
                    throw IllegalStateException("TransactionRepository must be " +
                            "initialized")
        }
    }
}