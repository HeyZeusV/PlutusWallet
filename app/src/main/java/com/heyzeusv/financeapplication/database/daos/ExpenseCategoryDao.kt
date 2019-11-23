package com.heyzeusv.financeapplication.database.daos

import androidx.room.Dao
import androidx.room.Query
import com.heyzeusv.financeapplication.database.entities.ExpenseCategory

/**
 *  Queries that can be applied to ExpenseCategory table.
 *
 *  Additional queries that can be applied specifically to this table.
 *  Can have as many as needed, make returnType nullable in case of query returning nothing!
 *  Using LiveData signals Room to run on a background thread.
 *  LiveData object will handle sending data over to main thread and notify any observers.
 */
@Dao
abstract class ExpenseCategoryDao : BaseDao<ExpenseCategory>() {

    /**
     *  Returns all Categories.
     *
     *  @return List object that holds List of all Categories.
     */
    @Query("""SELECT category 
            FROM expensecategory""")
    abstract suspend fun getExpenseCategoryNames() : List<String>

    /**
     *  Returns the size of table.
     *
     *  @return the size of table.
     */
    @Query("""SELECT COUNT(*) 
            FROM expensecategory""")
    abstract suspend fun getExpenseCategorySize() : Int?
}