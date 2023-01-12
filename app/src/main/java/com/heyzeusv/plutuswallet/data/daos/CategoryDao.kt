package com.heyzeusv.plutuswallet.data.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.heyzeusv.plutuswallet.data.model.Category
import kotlinx.coroutines.flow.Flow

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
     *  Returns list of Category names of [type] in alphabetical order.
     */
    @Query("""SELECT name
              FROM category
              WHERE type=(:type)
              ORDER BY name ASC""")
    abstract suspend fun getCategoryNamesByTypeAsync(type: String): MutableList<String>

    /**
     *  Returns list of Category names of [type] in alphabetical order.
     */
    @Query("""SELECT name
              FROM category
              WHERE type=(:type)
              ORDER BY name ASC""")
    abstract fun getCategoryNamesByType(type: String): Flow<List<String>>

    /**
     *  Returns list of Categories used by a Transaction
     */
    @Query("""SELECT DISTINCT `category`.id, `category`.name, `category`.type
              FROM `category`
              INNER JOIN `transaction` ON `transaction`.category = `category`.name
              ORDER BY `category`.name ASC""")
    abstract fun getCategoriesUsed(): Flow<List<Category>>

    /**
     *  Returns the size of table.
     */
    @Query("""SELECT COUNT(*) 
              FROM category""")
    abstract suspend fun getCategorySize(): Int

    /**
     *  Returns flow that emits list of Categories of [type] in order of name.
     */
    @Query("""SELECT *
              FROM category
              WHERE type=(:type)
              ORDER BY name ASC""")
    abstract fun getCategoriesByType(type: String): Flow<List<Category>>

    /**
     *  Returns LD of list that holds all Categories of [type] in order of name.
     */
    @Query("""SELECT *
              FROM category
              WHERE type=(:type)
              ORDER BY name ASC""")
    abstract fun getLDCategoriesByType(type: String): LiveData<List<Category>>
}