package com.heyzeusv.plutuswallet.data.daos

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
     *  Inserts [obj] into table and returns row id or -1 if there is a conflict.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(obj: T): Long

    /**
     *  Inserts [objs] into table and returns row id or -1 if there is a conflict.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(objs: List<T>): List<Long>

    /**
     *  Updates [obj].
     */
    @Update
    abstract suspend fun update(obj: T)

    /**
     *  Updates [objs].
     */
    @Update
    abstract suspend fun update(objs: List<T>)

    /**
     *  Deletes [obj]
     */
    @Delete
    abstract suspend fun delete(obj: T)

    /**
     *  Inserts [obj] into table if it doesn't exist.
     *  Updates [obj] from table if it does exist.
     */
    @Transaction
    open suspend fun upsert(obj: T) {

        val id: Long = insert(obj)
        if (id == -1L) {
            update(obj)
        }
    }

    /**
     *  Inserts [objs] into table if they don't exist.
     *  Updates [objs] from table if they do exist.
     */
    @Transaction
    open suspend fun upsert(objs: List<T>) {

        val insertResult: List<Long> = insert(objs)
        val updateList = ArrayList<T>()

        for (i: Int in insertResult.indices) {
            if (insertResult[i] == -1L) {
                updateList.add(objs[i])
            }
        }

        if (updateList.isNotEmpty()) {
            update(updateList)
        }
    }
}