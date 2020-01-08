package com.heyzeusv.plutuswallet.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import androidx.room.Update

/**
 *  Queries that can be applied to all tables.
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
     *  @param  obj the object to be inserted.
     *  @return row id or -1 if there is conflict.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(obj : T) : Long

    /**
     *  @param  obj list of objects to be inserted.
     *  @return list of row ids or -1 where there is conflict.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(obj : List<T>) : List<Long>

    /**
     *  @param obj the object to be updated.
     */
    @Update
    abstract suspend fun update(obj : T)

    /**
     *  @param obj list of objects to be updated.
     */
    @Update
    abstract suspend fun update(obj : List<T>)

    /**
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