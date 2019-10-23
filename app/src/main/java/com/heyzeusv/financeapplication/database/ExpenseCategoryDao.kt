package com.heyzeusv.financeapplication.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.heyzeusv.financeapplication.ExpenseCategory

/*
    All the queries that can be applied to the database
    Can have as many as needed, make returnType nullable in case table is empty!
    Using LiveData signals Room to run on background thread, LiveData object will handle sending
        the crime data over to the main thread and notify any observers
*/

@Dao
abstract class ExpenseCategoryDao : BaseDao<ExpenseCategory>() {

    // return total number of categories
    @Query("SELECT COUNT(*) FROM expensecategory")
    abstract suspend fun getExpenseCategorySize() : Int?

    // return all the categories
    @Query("SELECT category FROM expensecategory")
    abstract fun getExpenseCategoryNames() : LiveData<List<String>>
}