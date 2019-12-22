package com.heyzeusv.plutuswallet.database.daos

import androidx.room.Dao
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
}