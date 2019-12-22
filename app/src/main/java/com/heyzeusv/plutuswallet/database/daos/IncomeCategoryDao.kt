package com.heyzeusv.plutuswallet.database.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.heyzeusv.plutuswallet.database.entities.IncomeCategory

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
     *  Returns LiveData object containing all Categories.
     *
     *  @return LiveData object that holds list of all Categories.
     */
    @Query("""SELECT category
                   FROM incomecategory""")
    abstract fun getLDIncomeCategoryNames() : LiveData<List<String>>

    /**
     *  Returns all categories in table.
     *
     *  @return List object that holds List of all categories.
     */
    @Query("""SELECT category 
            FROM incomecategory""")
    abstract suspend fun getIncomeCategoryNames() : List<String>

    /**
     *  Returns the size of table.
     *
     *  @return the size of table.
     */
    @Query("""SELECT COUNT(*) 
            FROM incomecategory""")
    abstract suspend fun getIncomeCategorySize() : Int?
}