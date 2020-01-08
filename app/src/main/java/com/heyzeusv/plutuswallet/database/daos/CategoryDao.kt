package com.heyzeusv.plutuswallet.database.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.heyzeusv.plutuswallet.database.entities.Category

/**
 *  Queries that can be applied to Category table.
 *
 *  Additional queries that can be applied specifically to this table.
 *  Can have as many as needed, make returnType nullable in case of query returning nothing!
 *  Using LiveData signals Room to run on a background thread.
 *  LiveData object will handle sending data over to main thread and notify any observers.
 */
@Dao
abstract class CategoryDao : BaseDao<Category>() {

    /**
     *  @return LiveData object that holds list that holds all Categories.
     */
    @Query("""SELECT *
              FROM category
              WHERE type=(:type)
              ORDER BY category ASC""")
    abstract fun getLDCategoriesByType(type : String) : LiveData<List<Category>>

    /**
     *  @return list of all Categories of given Type.
     */
    @Query("""SELECT category
              FROM category
              WHERE type=(:type)
              ORDER BY category ASC""")
    abstract suspend fun getCategoriesByType(type : String) : List<String>

    /**
     *  @return the size of table or null if empty.
     */
    @Query("""SELECT COUNT(*) 
              FROM category""")
    abstract suspend fun getCategorySize() : Int?

}