package com.heyzeusv.financeapplication.database

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update

/*
    DAO are used to interact with tables, essentially queries
    They support inheritance, so use this as a base
    Needed for every Entity
*/
interface BaseDao<T> {

    // Insert an object in the database.
    @Insert
    fun insert(obj: T)

    // Insert an array of objects in the database.
    @Insert
    fun insert(vararg obj: T)

    // Update an object from the database.
    @Update
    fun update(obj: T)

    // Delete an object from the database
    @Delete
    fun delete(obj: T)
}