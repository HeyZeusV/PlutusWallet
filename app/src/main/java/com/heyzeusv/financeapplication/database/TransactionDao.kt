package com.heyzeusv.financeapplication.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.heyzeusv.financeapplication.Transaction
import java.util.*

/*
    All the queries that can be applied to the database
    Can have as many as needed, make returnType nullable in case table is empty!
    Using LiveData signals Room to run on background thread, LiveData object will handle sending
        the crime data over to the main thread and notify any observers
*/

@Dao
interface TransactionDao : BaseDao<Transaction> {

    // returns all transactions
    @Query("SELECT * FROM `transaction`")
    fun getTransactions() : LiveData<List<Transaction>>

    // returns all transactions with given category
    @Query("SELECT * FROM `transaction` WHERE category=(:category)")
    fun getTransactions(category : String) : LiveData<List<Transaction>>

    // returns all transactions within given dates
    @Query("SELECT * FROM `transaction` WHERE date BETWEEN :start AND :end")
    fun getTransactions(start : Date, end : Date) : LiveData<List<Transaction>>

    // returns all transactions with given category and within given dates
    @Query("SELECT * FROM `transaction` WHERE category=(:category) AND date BETWEEN :start AND :end")
    fun getTransactions(category : String, start : Date, end : Date) : LiveData<List<Transaction>>

    // returns transaction with specific id
    @Query("SELECT * FROM `transaction` WHERE id=(:id)")
    fun getTransaction(id : Int) : LiveData<Transaction?>

    // returns the highest id in database
    @Query("SELECT MAX(id) FROM `transaction`")
    suspend fun getMaxId() : Int?
}