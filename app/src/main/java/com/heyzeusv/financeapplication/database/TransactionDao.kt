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

    // returns transaction with specific id
    @Query("SELECT * FROM `transaction` WHERE id=(:id)")
    fun getTransaction(id : Int) : Transaction

    // returns transaction with specific id
    @Query("SELECT * FROM `transaction` WHERE id=(:id)")
    fun getLDTransaction(id : Int) : LiveData<Transaction?>

    // returns all transactions
    @Query("SELECT * FROM `transaction`")
    fun getLDTransactions() : LiveData<List<Transaction>>

    // returns all transactions with given category
    @Query("SELECT * FROM `transaction` WHERE type=(:type) AND category=(:category)")
    fun getLDTransactions(type : String?, category : String?) : LiveData<List<Transaction>>

    // returns all transactions within given dates
    @Query("SELECT * FROM `transaction` WHERE date BETWEEN :start AND :end")
    fun getLDTransactions(start : Date?, end : Date?) : LiveData<List<Transaction>>

    // returns all transactions with given category and within given dates
    @Query("SELECT * FROM `transaction` WHERE type=(:type) AND category=(:category) AND date BETWEEN :start AND :end")
    fun getLDTransactions(type : String?, category : String?, start : Date?, end : Date?) : LiveData<List<Transaction>>

    // returns all transactions where futureDate is before currentDate and futureTCreated is false
    @Query("SELECT * FROM `transaction` WHERE futureDate < :currentDate AND futureTCreated == 0")
    suspend fun getFutureTransactions(currentDate : Date) : List<Transaction>

    // returns the highest id in database
    @Query("SELECT MAX(id) FROM `transaction`")
    suspend fun getMaxId() : Int?

    // returns LiveData of highest id in database
    @Query("SELECT MAX(id) from `transaction`")
    fun getMaxLDId() : LiveData<Int?>
}