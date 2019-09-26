package com.heyzeusv.financeapplication.database

import androidx.room.Dao
import androidx.room.Query
import com.heyzeusv.financeapplication.Transaction

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
    fun getTransactions() : List<Transaction>

    @Query("SELECT * FROM `transaction` WHERE id=(:id)")
    fun getTransaction(id : Int) : Transaction?
}