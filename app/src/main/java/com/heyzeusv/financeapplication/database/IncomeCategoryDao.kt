package com.heyzeusv.financeapplication.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.heyzeusv.financeapplication.IncomeCategory

/**
 *  Queries that can be applied to IncomeCategory table.
 *
 *  Additional queries that can be applied specifically to this table.
 *  Can have as many as needed, make returnType nullable in case of query returning nothing!
 *  Using LiveData signals Room to run on a background thread.
 *  LiveData object will handle sending data over to main thread and notify any observers.
 */
@Dao
abstract class IncomeCategoryDao : BaseDao<IncomeCategory>() {

    /**
     *  Returns the size of table.
     *
     *  @return the size of table.
     */
    @Query("SELECT COUNT(*) FROM incomecategory")
    abstract suspend fun getIncomeCategorySize() : Int?

    /**
     *  Returns all categories in table.
     *
     *  @return LiveData object that holds List of all categories.
     */
    @Query("SELECT category FROM incomecategory")
    abstract fun getIncomeCategoryNames() : LiveData<List<String>>
}