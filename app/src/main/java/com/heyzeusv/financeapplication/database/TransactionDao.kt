package com.heyzeusv.financeapplication.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.heyzeusv.financeapplication.CategoryTotals
import com.heyzeusv.financeapplication.Transaction
import java.util.*

/**
 *  Queries that can be applied to Transaction table.
 *
 *  Additional queries that can be applied specifically to this table.
 *  Can have as many as needed, make returnType nullable in case of query returning nothing!
 *  Using LiveData signals Room to run on a background thread.
 *  LiveData object will handle sending data over to main thread and notify any observers.
 */
@Dao
abstract class TransactionDao : BaseDao<Transaction>() {

    /**
     *  Returns Transaction with given id.
     *
     *  @param id id of Transaction to be returned.
     *  @return LiveData object that holds Transaction to be returned.
     */
    @Query("""SELECT * 
            FROM `transaction` 
            WHERE id=(:id)""")
    abstract fun getLDTransaction(id : Int) : LiveData<Transaction?>

    /**
     *  Returns all Transactions.
     *
     *  @return LiveData object that holds List of all Transactions.
     */
    @Query("""SELECT * 
            FROM `transaction`""")
    abstract fun getLDTransactions() : LiveData<List<Transaction>>

    /**
     *  Returns all Transaction with given Type
     *
     *  @param  type either "Expense" or "Income"
     *  @return LiveData object that holds List of all Transactions with given Type
     */
    @Query("""SELECT * 
            FROM `transaction` 
            WHERE type=(:type)""")
    abstract fun getLDTransactions(type : String?) : LiveData<List<Transaction>>

    /**
     *  Returns all Transactions with given Type and Category.
     *
     *  @param  type     either "Expense" or "Income".
     *  @param  category the category to be matched against.
     *  @return LiveData object that holds List of all Transactions with given Type and category.
     */
    @Query("""SELECT * 
            FROM `transaction` 
            WHERE type=(:type) AND category=(:category)""")
    abstract fun getLDTransactions(type : String?, category : String?) : LiveData<List<Transaction>>

    /**
     *  Returns all Transaction within given Dates.
     *
     *  @param  start the start Date to be compared with.
     *  @param  end   the end Date to be compared with.
     *  @return LiveData object that holds List of all Transactions within given Dates in table.
     */
    @Query("""SELECT * 
            FROM `transaction` 
            WHERE date BETWEEN :start AND :end""")
    abstract fun getLDTransactions(start : Date?, end : Date?) : LiveData<List<Transaction>>

    /**
     *  Returns all Transactions with given Type and Category and within given Dates.
     *
     *  @param  type     either "Expense" or "Income".
     *  @param  category the category to be matched against.
     *  @param  start    the start Date to be compared with.
     *  @param  end      the end Date to be compared with.
     *  @return LiveDate object that holds list of all Transactions with given Type and Category and within given Dates.
     */
    @Query("""SELECT * 
            FROM `transaction` 
            WHERE type=(:type) AND category=(:category) AND date BETWEEN :start AND :end""")
    abstract fun getLDTransactions(type : String?, category : String?, start : Date?, end : Date?) : LiveData<List<Transaction>>

    /**
     *  Returns sum of Totals of each Category in said Type.
     *
     *  @param  type the type of Transactions to be returned.
     *  @return CategoryTotal is helper object that holds Category and the sum of Totals
     */
    @Query("""SELECT category, SUM(total) AS total 
            FROM `transaction` 
            WHERE type=(:type) 
            GROUP BY category""")
    abstract fun getLDCategoryTotals(type : String?) : LiveData<List<CategoryTotals>>

    /**
     *  Returns sum of Totals of each Category in said Type.
     *
     *  @param  type the type of Transactions to be returned.
     *  @return CategoryTotal is helper object that holds Category and the sum of Totals
     */
    @Query("""SELECT category, SUM(total) AS total 
            FROM `transaction` 
            WHERE type=(:type) AND date BETWEEN :start AND :end
            GROUP BY category""")
    abstract fun getLDCategoryTotals(type : String?, start : Date?, end : Date?) : LiveData<List<CategoryTotals>>

    /**
     *  Returns all transactions where futureDate is before currentDate and futureTCreated is false.
     *
     *  @param  currentDate the Date at which this query is ran at.
     *  @return list of all Transactions with futureDate before currentDate and futureTCreated is false.
     */
    @Query("""SELECT * 
            FROM `transaction` 
            WHERE futureDate < :currentDate AND futureTCreated == 0""")
    abstract suspend fun getFutureTransactions(currentDate : Date) : List<Transaction>

}