package com.heyzeusv.financeapplication.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.heyzeusv.financeapplication.Category

/*
    All the queries that can be applied to the database
    Can have as many as needed, make returnType nullable in case table is empty!
    Using LiveData signals Room to run on background thread, LiveData object will handle sending
        the crime data over to the main thread and notify any observers
*/

@Dao
interface CategoryDao : BaseDao<Category> {

    // return total number of categories
    @Query("SELECT COUNT(*) FROM category")
    fun getCategorySize() : LiveData<Int?>

    @Query("SELECT category FROM category")
    fun getCategoryNames() : LiveData<List<String>>
}