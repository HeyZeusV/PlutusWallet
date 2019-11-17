package com.heyzeusv.financeapplication.database.daos

import androidx.room.*

/**
 *  Queries that can be applied to all tables
 *
 *  DAO used to interact with tables, essentially queries.
 *  They support inheritance, this is a base.
 *  All EntityDaos must extent this class.
 *
 *  @property T the type of Entity to be used.
 */
@Dao
abstract class BaseDao<T> {

    /**
     *  Insert an object in the database.
     *
     *  @param  obj the object to be inserted.
     *  @return The SQLite row id.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(obj : T) : Long

    /**
     *  Insert an array of objects in the database.
     *
     *  @param  obj the objects to be inserted.
     *  @return The SQLite row ids.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(obj : List<T>) : List<Long>

    /**
     *  Update an object from the database.
     *
     *  @param obj the object to be updated.
     */
    @Update
    abstract suspend fun update(obj : T)

    /**
     *  Update an array of objects from the database.
     *
     *  @param obj the objects to be updated.
     */
    @Update
    abstract suspend fun update(obj : List<T>)

    /**
     *  Delete an up from the database.
     *
     *  @param obj the object to be deleted.
     */
    @Delete
    abstract suspend fun delete(obj : T)

    /**
     *  Inserts object into database if it doesn't exist.
     *  Updates object from database if it does exist.
     *
     *  @param obj the object to be inserted/updated.
     */
    @androidx.room.Transaction
    open suspend fun upsert(obj : T) {

        val id : Long = insert(obj)
        if (id == -1L) {

            update(obj)
        }
    }

    /**
     *  Inserts objects from an array into database if they don't exist.
     *  Updates objects from an array from database if they do exist.
     *
     *  @param obj the array of objects to be inserted/updated.
     */
    @Transaction
    open suspend fun upsert(obj : List<T>) {

        val insertResult : List<Long> = insert(obj)
        val updateList                = ArrayList<T>()

        for (i : Int in insertResult.indices) {

            if (insertResult[i] == -1L) {

                updateList.add(obj[i])
            }
        }

        if (updateList.isNotEmpty()) {

            update(updateList)
        }
    }
}